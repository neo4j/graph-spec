package golang

import (
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/neo4j/ontology-graph-spec/tools/schema-generator/internal/model"
)

func TestValidTagName(t *testing.T) {
	validTags := []string{"type", "fulltext.analyzer", "LIST<ANY>", "a-b c", "día", "!#$%&()*+-./:;<=>?@[]^_{|}~ ", "123"}
	invalidTags := []string{"", "a,b", `a"b`, `a\b`, "a`b", "£"}

	for _, tag := range validTags {
		assert.True(t, validTagName(tag), "%q should be a valid tag name", tag)
	}
	for _, tag := range invalidTags {
		assert.False(t, validTagName(tag), "%q should not be a valid tag name", tag)
	}
}

func TestTypeName(t *testing.T) {
	shape := &model.NamedType{Kind: model.Union, Name: "Shape", Discriminator: "type"}
	circle := &model.NamedType{Name: "CIRCLE", VariantOf: shape}
	rect := &model.NamedType{Name: "RectangleShape", VariantOf: shape}
	point := &model.NamedType{Name: "shape_point", VariantOf: shape}

	assert.Equal(t, "ShapeVariant", interfaceName(shape))
	assert.Equal(t, "ShapeType", discriminatorMethod(shape))
	assert.Equal(t, "ShapeCircle", typeName(circle))
	assert.Equal(t, "RectangleShape", typeName(rect)) // unchanged as already had shape in name
	assert.Equal(t, "ShapePoint", typeName(point))
}

func TestCheckNames(t *testing.T) {
	enum := func(values ...string) *model.NamedType {
		return &model.NamedType{Kind: model.Enum, Name: "A", Path: "A", Values: values}
	}
	object := func(props ...string) *model.NamedType {
		d := &model.NamedType{Name: "A", Path: "A"}
		for _, p := range props {
			d.Fields = append(d.Fields, &model.Field{JSONName: p})
		}
		return d
	}
	union := &model.NamedType{Kind: model.Union, Name: "U", Path: "U", Discriminator: "k"}
	variant := object("u_k")
	variant.Tags = []*model.Tag{{Union: union, Property: "k"}}

	tests := []struct {
		types []*model.NamedType
		want  string
	}{
		{[]*model.NamedType{{Name: "Node", Path: "Node"}, {Name: "NODE", Path: "NODE"}}, `Node and NODE are both named Node in Go`},
		{[]*model.NamedType{enum("a-b", "a_b")}, `are both named AAB in Go`},
		{[]*model.NamedType{enum("<>")}, `cannot derive a Go name for enum value "<>"`},
		{[]*model.NamedType{object("fromNode", "from_node")}, `property "fromNode" and property "from_node" are both named FromNode`},
		{[]*model.NamedType{union, variant}, `the UK method and property "u_k" are both named UK`},
		{[]*model.NamedType{object("a,b")}, `property name "a,b" cannot be used as a Go struct tag`},
	}
	for _, tc := range tests {
		assert.ErrorContains(t, checkNames(&model.Model{NamedTypes: tc.types}), tc.want)
	}
}
