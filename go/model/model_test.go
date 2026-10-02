package model

import (
	"embed"
	"encoding/json"
	"testing"

	"github.com/stretchr/testify/require"
)

//go:embed testdata/*.json
var testdata embed.FS

// A real v1 document (the spec gate's movies example) round-trips through
// the generated model: unmarshal, marshal, unmarshal again — the two model
// values must be identical, including carried-untouched extension payloads
// and owner-defined tool fields (see extras_test.go for the byte-equal
// assertions on those open surfaces).
func TestOntologySpecV1RoundTripJSON(t *testing.T) {
	raw, err := testdata.ReadFile("testdata/ontology-spec-example.json")
	require.NoError(t, err)

	var graph GraphModel
	require.NoError(t, json.Unmarshal(raw, &graph))

	// v1 shape spot checks: id-keyed nodes, type tokens, endpoint
	// cardinality, core tools.
	require.Equal(t, "3f8a4c2e-9b1d-4e7a-a5c3-2d8f1b6e9a04", graph.ID)
	require.Equal(t, 3, graph.Version)
	require.Len(t, graph.Nodes, 5)
	actor := graph.Nodes["Actor"]
	require.Equal(t, "Actor", *actor.Label)
	require.Equal(t, "STRING", *actor.Properties["id"].Type)
	embedding := graph.Nodes["Movie"].Properties["embedding"]
	require.Equal(t, "VECTOR<FLOAT>", *embedding.Type)
	require.Equal(t, 1042, *embedding.Dimension)
	actedIn := graph.Relationships["ACTED_IN"]
	require.Equal(t, "ACTED_IN", actedIn.Type)
	require.Equal(t, "Actor", actedIn.From.Node)
	require.Equal(t, 1, *actedIn.To.MaxCount)
	require.Len(t, actedIn.Tools, 2)
	require.Equal(t, "canonicalQuery", actedIn.Tools[1].Type)

	out, err := json.Marshal(graph)
	require.NoError(t, err)

	// Semantically identical to the input (whitespace/HTML-escaping aside:
	// encoding/json compacts and HTML-escapes Marshaler output, so raw
	// extras bytes normalise on the first marshal).
	require.JSONEq(t, string(raw), string(out))

	// ...and the marshalled form is a fixed point: a second round trip is
	// byte-identical, carried extras included.
	var again GraphModel
	require.NoError(t, json.Unmarshal(out, &again))
	out2, err := json.Marshal(again)
	require.NoError(t, err)
	require.Equal(t, string(out), string(out2))
}
