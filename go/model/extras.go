package model

// Hand-written companions for the format's open surfaces — NOT generated:
// this file survives regeneration of model.go (go/scripts/generate-go-models.sh).
//
// schemancer (pinned v1.2.0) drops `additionalProperties: true` from the
// generated structs: ExtensionsMap would lose named neo4j:* keys, Tool its
// owner-defined fields (cypher, url, ...), and Extension the custom
// envelope's unknown fields. The format's invariant is that unknown content
// is carried untouched, never rejected (design rule 9; ADR-0005, ADR-0006).
//
// The generate script injects the unexported `extra` field these methods
// use; keys outside each type's known-field set are collected into it on
// decode and inlined back on encode — the same extras-map pattern as the
// Kotlin model's InlineExtras helper
// (src/commonMain/kotlin/model/extension/InlineExtras.kt), including its
// known-key collision rule: on marshal the known field wins and the
// colliding extras entry is dropped (unreachable from JSON input, where a
// decoded `extra` never contains known keys; reachable for programmatically
// built values).

import (
	"bytes"
	"encoding/json"
	"sort"
)

// knownKeys lists each open surface's schema-defined fields; every other key
// is extras. Keep in sync with ontology-graph-spec.schema.json ($defs/tool,
// $defs/extension, $defs/extensionsMap).
var (
	toolKnownKeys          = []string{"type", "name", "description"}
	extensionKnownKeys     = []string{"type", "$schema", "name", "definition"}
	extensionsMapKnownKeys = []string{"custom"}
)

// extractExtras collects every object key outside known into a fresh extras
// map, preserving the raw bytes of each value. Returns nil when there are no
// extras, so absent stays absent (the format never writes empty maps).
func extractExtras(data []byte, known []string) (map[string]json.RawMessage, error) {
	var all map[string]json.RawMessage
	if err := json.Unmarshal(data, &all); err != nil {
		return nil, err
	}
	for _, k := range known {
		delete(all, k)
	}
	if len(all) == 0 {
		return nil, nil
	}
	return all, nil
}

// inlineExtras merges extras into the marshalled known-fields object:
// known keys first (struct declaration order), then extras sorted by key for
// deterministic output. Extra values are inlined byte-faithfully. A key
// colliding with a known field is dropped — the known field always wins
// (the InlineExtras rule, owned here once for all three open surfaces).
func inlineExtras(known []byte, extra map[string]json.RawMessage, knownKeys []string) ([]byte, error) {
	if len(extra) == 0 {
		return known, nil
	}
	knownSet := make(map[string]struct{}, len(knownKeys))
	for _, k := range knownKeys {
		knownSet[k] = struct{}{}
	}
	keys := make([]string, 0, len(extra))
	for k := range extra {
		if _, collision := knownSet[k]; collision {
			continue
		}
		keys = append(keys, k)
	}
	if len(keys) == 0 {
		return known, nil
	}
	sort.Strings(keys)

	var buf bytes.Buffer
	trimmed := bytes.TrimSuffix(known, []byte("}"))
	buf.Write(trimmed)
	// An empty known object leaves "{" behind; its first extra needs no comma.
	needsComma := !bytes.HasSuffix(trimmed, []byte("{"))
	for _, k := range keys {
		if needsComma {
			buf.WriteByte(',')
		}
		needsComma = true
		key, err := json.Marshal(k)
		if err != nil {
			return nil, err
		}
		buf.Write(key)
		buf.WriteByte(':')
		buf.Write(extra[k])
	}
	buf.WriteByte('}')
	return buf.Bytes(), nil
}

func (t Tool) MarshalJSON() ([]byte, error) {
	type alias Tool
	known, err := json.Marshal(alias(t))
	if err != nil {
		return nil, err
	}
	return inlineExtras(known, t.extra, toolKnownKeys)
}

func (t *Tool) UnmarshalJSON(data []byte) error {
	type alias Tool
	var a alias
	if err := json.Unmarshal(data, &a); err != nil {
		return err
	}
	extra, err := extractExtras(data, toolKnownKeys)
	if err != nil {
		return err
	}
	a.extra = extra
	*t = Tool(a)
	return nil
}

func (e Extension) MarshalJSON() ([]byte, error) {
	type alias Extension
	known, err := json.Marshal(alias(e))
	if err != nil {
		return nil, err
	}
	return inlineExtras(known, e.extra, extensionKnownKeys)
}

func (e *Extension) UnmarshalJSON(data []byte) error {
	type alias Extension
	var a alias
	if err := json.Unmarshal(data, &a); err != nil {
		return err
	}
	extra, err := extractExtras(data, extensionKnownKeys)
	if err != nil {
		return err
	}
	a.extra = extra
	*e = Extension(a)
	return nil
}

func (m ExtensionsMap) MarshalJSON() ([]byte, error) {
	type alias ExtensionsMap
	known, err := json.Marshal(alias(m))
	if err != nil {
		return nil, err
	}
	return inlineExtras(known, m.extra, extensionsMapKnownKeys)
}

func (m *ExtensionsMap) UnmarshalJSON(data []byte) error {
	type alias ExtensionsMap
	var a alias
	if err := json.Unmarshal(data, &a); err != nil {
		return err
	}
	extra, err := extractExtras(data, extensionsMapKnownKeys)
	if err != nil {
		return err
	}
	a.extra = extra
	*m = ExtensionsMap(a)
	return nil
}
