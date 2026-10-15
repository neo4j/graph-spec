package schema_test

import (
	"testing"

	"github.com/neo4j/graph-spec/tools/schema-generator/internal/schema"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestLoadErrors(t *testing.T) {
	tests := []struct{ name, input, want string }{
		{"invalid JSON", `{"type": `, `invalid JSON`},
		{"non-object schema", `true`, `#: a schema must be an object`},
		{"unsupported keyword", `{"anyOf": []}`, `#/anyOf: keyword "anyOf" is not supported`},
		{"nested $id", `{"items": {"$id": "x"}}`, `#/items/$id: "$id" is only supported on the root schema`},
		{"nested $defs", `{"items": {"$defs": {}}}`, `#/items/$defs: "$defs" is only supported on the root schema`},
		{"unknown type", `{"type": "date"}`, `#/type: unknown type "date"`},
		{"duplicate type", `{"type": ["string", "string"]}`, `#/type: type "string" is listed more than once`},
		{"non-object additionalProperties", `{"additionalProperties": true}`, `#/additionalProperties: a schema must be an object`},
		{"empty enum", `{"enum": []}`, `#/enum: "enum" must be a non-empty array of strings`},
		{"non-string enum", `{"enum": ["a", 1]}`, `#/enum: "enum" must be a non-empty array of strings`},
		{"non-string const", `{"const": 1}`, `#/const: expected a string`},
		{"empty oneOf", `{"oneOf": []}`, `#/oneOf: "oneOf" must be a non-empty array`},
		{"unknown keyword in oneOf branch", `{"properties": {"a": {"oneOf": [{}, {"foo": {}}]}}}`,
			`#/properties/a/oneOf/1/foo: keyword "foo" is not supported`},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			_, err := schema.Load([]byte(tt.input))
			require.ErrorContains(t, err, tt.want)
		})
	}
}

func TestLoad(t *testing.T) {
	doc, err := schema.Load([]byte(`{
		"title": "Root",
		"type": "object",
		"properties": {
			"b": {"type": ["string", "null"], "minimum": 0},
			"a/c": {"type": "object", "additionalProperties": {"type": "string"}}
		},
		"additionalProperties": false,
		"$defs": {
			"Z": {"type": "string", "const": "z"},
			"Y": {"type": "string"}
		}
	}`))
	require.NoError(t, err)

	root := doc.Root
	assert.Equal(t, "Root", root.Title)
	assert.Nil(t, root.AdditionalProperties)

	assert.Equal(t, []*schema.Property{
		{Name: "a/c", Element: &schema.Element{
			Path:  "#/properties/a~1c",
			Types: []string{"object"},
			AdditionalProperties: &schema.Element{
				Path:  "#/properties/a~1c/additionalProperties",
				Types: []string{"string"},
			},
		}},
		{Name: "b", Element: &schema.Element{Path: "#/properties/b", Types: []string{"string", "null"}}},
	}, root.Properties)

	assert.Equal(t, []*schema.Def{
		{Name: "Y", Element: &schema.Element{Path: "#/$defs/Y", Types: []string{"string"}}},
		{Name: "Z", Element: &schema.Element{Path: "#/$defs/Z", Types: []string{"string"}, Const: ptr("z")}},
	}, doc.Defs)
}

func ptr(s string) *string {
	return &s
}
