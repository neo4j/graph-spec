package model

import (
	"bytes"
	"encoding/json"
	"testing"

	"github.com/stretchr/testify/require"
)

// openSurfacesDoc exercises both open surfaces in one document: a named
// ui:display extension on the document and on a node (ExtensionsMap
// extras), and a custom extension with extra keys (Extension extras).
//
// Object keys are ordered to match this package's marshal order (generated
// struct fields in declaration order, then extras sorted by key) so the
// byte-equal assertion is exact: any dropped or re-ordered content fails.
const openSurfacesDoc = `{
  "extensions": {
    "custom": [
      {
        "type": "acme:lineage",
        "owner": "data-platform",
        "priority": 3
      }
    ],
    "ui:display": { "theme": "dark" }
  },
  "id": "round-trip-test",
  "nodes": {
    "person": {
      "extensions": {
        "ui:display": { "color": "red" }
      },
      "label": "Person"
    }
  },
  "schema": "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
  "version": 1
}`

func compacted(t *testing.T, raw string) []byte {
	t.Helper()
	var buf bytes.Buffer
	require.NoError(t, json.Compact(&buf, []byte(raw)))
	return buf.Bytes()
}

// The format's carried-untouched invariant: a document using both open
// surfaces round-trips through the model byte-equal.
func TestOpenSurfacesRoundTripByteEqual(t *testing.T) {
	want := compacted(t, openSurfacesDoc)

	var graph GraphModel
	require.NoError(t, json.Unmarshal(want, &graph))

	out, err := json.Marshal(graph)
	require.NoError(t, err)
	require.Equal(t, string(want), string(out))

	// Idempotent: a second round trip changes nothing.
	var again GraphModel
	require.NoError(t, json.Unmarshal(out, &again))
	out2, err := json.Marshal(again)
	require.NoError(t, err)
	require.Equal(t, string(want), string(out2))
}

func TestExtensionCarriesUnknownFields(t *testing.T) {
	var ext Extension
	require.NoError(t, json.Unmarshal(
		[]byte(`{"type":"acme:lineage","definition":{"x":1},"owner":"data-platform"}`), &ext))

	require.Equal(t, "acme:lineage", ext.Type)
	require.JSONEq(t, `"data-platform"`, string(ext.extra["owner"]))
	// definition is a known field, not an extra.
	_, leaked := ext.extra["definition"]
	require.False(t, leaked)

	out, err := json.Marshal(ext)
	require.NoError(t, err)
	require.JSONEq(t,
		`{"type":"acme:lineage","definition":{"x":1},"owner":"data-platform"}`,
		string(out))
}

func TestExtensionsMapCarriesNamedExtensions(t *testing.T) {
	var m ExtensionsMap
	require.NoError(t, json.Unmarshal(
		[]byte(`{"ui:display":{"color":"red"},"custom":[{"type":"acme:lineage"}]}`), &m))

	require.Len(t, m.Custom, 1)
	require.JSONEq(t, `{"color":"red"}`, string(m.extra["ui:display"]))

	out, err := json.Marshal(m)
	require.NoError(t, err)
	require.JSONEq(t,
		`{"ui:display":{"color":"red"},"custom":[{"type":"acme:lineage"}]}`,
		string(out))
}

// The InlineExtras collision rule: unreachable from JSON input, but a
// programmatically built value may carry an extra whose key collides with a
// known field — the known field wins, the extras entry is dropped.
func TestKnownKeyCollisionKnownFieldWins(t *testing.T) {
	ext := Extension{Type: "acme:lineage", extra: map[string]json.RawMessage{
		"type":  json.RawMessage(`"evil"`),
		"owner": json.RawMessage(`"data-platform"`),
	}}

	out, err := json.Marshal(ext)
	require.NoError(t, err)
	require.Equal(t, `{"type":"acme:lineage","owner":"data-platform"}`, string(out))
}

// Absent means none: no extras, no trace of the catch-all on the wire.
func TestAbsentExtrasStayAbsent(t *testing.T) {
	out, err := json.Marshal(Extension{Type: "acme:lineage"})
	require.NoError(t, err)
	require.Equal(t, `{"type":"acme:lineage"}`, string(out))

	var ext Extension
	require.NoError(t, json.Unmarshal([]byte(`{"type":"acme:lineage"}`), &ext))
	require.Nil(t, ext.extra)
}
