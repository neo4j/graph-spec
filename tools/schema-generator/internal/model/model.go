// Package model holds the language-neutral model the generator builds from a schema.
package model

// Model is the set of named types to generate, in output order.
type Model struct {
	NamedTypes []*NamedType
}

type NamedTypeKind int

const (
	// Struct is an object with known properties.
	Struct NamedTypeKind = iota
	// Enum is a set of string values.
	Enum
	// Union is a discriminated set of Struct variants.
	Union
)

type NamedType struct {
	Kind NamedTypeKind
	Path string
	Doc  string

	// Name is the root schema's "title" or the definition's key under "$defs".
	Name string
	// VariantOf is set on a definition that is only ever used as a variant of this union.
	VariantOf *NamedType

	// Fields if a Struct.
	Fields []*Field
	// Tags are the discriminator properties a Struct writes when it is encoded, one per union it is a variant of.
	Tags []*Tag

	// Values if an Enum.
	Values []string

	// Discriminator is the JSON property that selects a Union variant.
	Discriminator string
	Variants      []*Variant
}

type Field struct {
	JSONName string
	Doc      string
	Required bool
	Type     *Type
	Path     string
}

type Variant struct {
	Value string
	Named *NamedType
}

type Tag struct {
	Union    *NamedType
	Property string
	Value    string
}

type TypeKind int

const (
	String TypeKind = iota
	Integer
	Number
	Boolean
	// Any accepts every JSON value.
	Any
	Array
	Map
	// Ref refers to the named type in Type.Named.
	Ref
)

type Type struct {
	Kind TypeKind
	// Elem is the element type of an Array or the value type of a Map.
	Elem *Type
	// Named is the named type a Ref type refers to.
	Named *NamedType
	// Nullable is set when the value may also be JSON null.
	Nullable bool
}
