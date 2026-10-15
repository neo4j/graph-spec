package model_test

import (
	"testing"

	"github.com/neo4j/graph-spec/tools/schema-generator/internal/model"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"

	"github.com/neo4j/graph-spec/tools/schema-generator/internal/schema"
)

func TestBuild(t *testing.T) {
	props := `
		"a": {"$ref": "#/$defs/A"},
		"list": {"type": "array", "items": {"type": ["string", "null"]}},
		"map": {"type": "object", "additionalProperties": {"oneOf": [{"type": "null"}, {"$ref": "#/$defs/A"}]}},
		"shape": {"$ref": "#/$defs/U"}`
	defs := `{
		"A": ` + object(`"k": {"type": "string", "const": "a"}, "n": {"type": "integer"}`) + `,
		"B": ` + object(`"k": {"type": "string", "const": "b"}`) + `,
		"U": {"oneOf": [{"$ref": "#/$defs/A"}, {"$ref": "#/$defs/B"}]}}`
	doc := document(t, props, defs)

	m, err := model.Build(doc)
	require.NoError(t, err)
	root, a, b, u := m.NamedTypes[0], m.NamedTypes[1], m.NamedTypes[2], m.NamedTypes[3]

	want := []*model.Type{
		{Kind: model.Ref, Named: a},
		{Kind: model.Array, Elem: &model.Type{Kind: model.String, Nullable: true}},
		{Kind: model.Map, Elem: &model.Type{Kind: model.Ref, Named: a, Nullable: true}},
		{Kind: model.Ref, Named: u},
	}
	var got []*model.Type
	for _, f := range root.Fields {
		got = append(got, f.Type)
	}
	assert.Equal(t, want, got)

	assert.Equal(t, model.Union, u.Kind)
	assert.Equal(t, "k", u.Discriminator)
	assert.Equal(t, []*model.Variant{{Value: "a", Named: a}, {Value: "b", Named: b}}, u.Variants)

	// The discriminator moves from the variant's fields to its tags.
	assert.Len(t, a.Fields, 1)
	assert.Equal(t, "n", a.Fields[0].JSONName)
	assert.Equal(t, []*model.Tag{{Union: u, Property: "k", Value: "a"}}, a.Tags)

	// A is also referenced outside the union, so only B is named after it.
	assert.Nil(t, a.VariantOf)
	assert.Same(t, u, b.VariantOf)
}

func TestBuildErrors(t *testing.T) {
	union := `"U": {"oneOf": [{"$ref": "#/$defs/A"}, {"$ref": "#/$defs/B"}]}`
	tests := []struct{ defs, want string }{
		{`{"A": ` + object(`"b": {"$ref": "#/$defs/B"}`) + `}`, `#/$defs/A/properties/b: reference "#/$defs/B" does not resolve`},
		{`{"A": ` + object(`"b": {"type": ["string", "integer"]}`) + `}`, `multiple types string, integer`},
		{`{"A": ` + object(`"b": {"type": "null"}`) + `}`, `type "null" is only supported alongside`},
		{`{"A": ` + object(`"b": {"type": "array"}`) + `}`, `an array requires "items"`},
		{`{"A": {"type": "object", "properties": {}, "required": ["b"], "additionalProperties": false}}`,
			`required property "b" is not declared`},
		{`{"A": ` + object(`"b": `+object(``)) + `}`, `#/$defs/A/properties/b: inline objects`},
		{`{"A": ` + object(`"b": {"type": "string", "enum": ["x"]}`) + `}`, `inline objects`},
		{`{"A": {"$ref": "#/$defs/B"}, "B": ` + object(``) + `}`, `#/$defs/A: a definition must be`},
		{`{"A": {"type": "string"}}`, `#/$defs/A: a definition must be`},
		{`{"A": {"type": ["string", "null"], "enum": ["x"]}}`, `a definition cannot be nullable`},
		{`{"A": {"type": "integer", "enum": ["1"]}}`, `only string enums are supported`},
		{`{"A": {"type": "string", "enum": ["a", "a"]}}`, `enum value "a" is listed more than once`},
		{`{"U": {"oneOf": [` + object(``) + `, {"$ref": "#/$defs/U"}]}}`,
			`#/$defs/U/oneOf/0: a "oneOf" branch must be a "$ref" or {"type": "null"}`},
		{`{"A": ` + object(``) + `, "B": ` + object(``) + `,
			"U": {"oneOf": [{"type": "null"}, {"$ref": "#/$defs/A"}, {"$ref": "#/$defs/B"}]}}`,
			`must have exactly one other branch`},
		{`{"A": ` + object(``) + `, "U": {"oneOf": [{"$ref": "#/$defs/A"}]}}`, `a union requires at least two variants`},
		{`{"A": {"type": "string", "enum": ["x"]}, "B": ` + object(``) + `, ` + union + `}`,
			`#/$defs/U/oneOf/0: union variants must reference an object definition`},
		{`{"A": ` + object(`"a": {"type": "string"}`) + `, "B": ` + object(``) + `, ` + union + `}`,
			`#/$defs/U/oneOf/0: cannot find the union discriminator`},
		{`{"A": ` + object(`"k": {"type": "string", "const": "a"}`) + `,
			"B": ` + object(`"j": {"type": "string", "const": "b"}`) + `, ` + union + `}`,
			`#/$defs/U/oneOf/1: variant has no "const" for discriminator property "k"`},
		{`{"A": ` + object(`"k": {"type": "string", "const": "x"}`) + `,
			"B": ` + object(`"k": {"type": "string", "const": "x"}`) + `, ` + union + `}`,
			`discriminator value "x" is used by more than one variant`},
	}
	for _, tc := range tests {
		_, err := model.Build(document(t, ``, tc.defs))
		assert.ErrorContains(t, err, tc.want, tc.defs)
	}

	doc, err := schema.Load([]byte(`{"type": "object", "properties": {}, "additionalProperties": false}`))
	require.NoError(t, err)
	_, err = model.Build(doc)
	assert.ErrorContains(t, err, `the root schema needs a "title"`)
}

func document(t *testing.T, props, defs string) *schema.Document {
	t.Helper()
	doc, err := schema.Load([]byte(
		`{"title": "Root", "type": "object", "properties": {` + props + `},"additionalProperties": false, "$defs": ` + defs + `}`))
	require.NoError(t, err)
	return doc
}

func object(props string) string {
	return `{"type": "object", "properties": {` + props + `}, "additionalProperties": false}`
}
