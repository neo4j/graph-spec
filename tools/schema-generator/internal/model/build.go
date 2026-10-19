package model

import (
	"slices"
	"strings"

	"github.com/neo4j/graph-spec/tools/schema-generator/internal/schema"
)

// Build normalises a schema into an internal Model representation
func Build(doc *schema.Document) (*Model, error) {
	root := doc.Root
	if root.Title == "" {
		return nil, schema.Errorf(root.Path, "the root schema needs a \"title\" to name it")
	}
	b := &builder{doc: doc, named: map[*schema.Def]*NamedType{}, referenced: map[*NamedType]bool{}}

	// Define the skeleton named types for every definition up front and then fully define them in subsequent loops.
	// This avoids any sequencing issues when dealing with recursive definitions.
	rootType := &NamedType{Name: root.Title, Path: root.Path}
	model := &Model{NamedTypes: []*NamedType{rootType}}
	for _, def := range doc.Defs {
		d := &NamedType{Name: def.Name, Path: def.Element.Path}
		b.named[def] = d
		model.NamedTypes = append(model.NamedTypes, d)
	}

	if err := b.define(rootType, root); err != nil {
		return nil, err
	}
	for _, def := range doc.Defs {
		if err := b.define(b.named[def], def.Element); err != nil {
			return nil, err
		}
	}

	b.finaliseUnions(model.NamedTypes)
	return model, nil
}

type builder struct {
	doc        *schema.Document
	named      map[*schema.Def]*NamedType
	unions     []*NamedType
	referenced map[*NamedType]bool // all types referenced from a property, list item or map value
}

type shapeKind int

const (
	shapeAny shapeKind = iota
	shapeRef
	shapePrimitive
	shapeArray
	shapeMap
	shapeStruct
	shapeEnum
	shapeUnion
)

type shape struct {
	kind     shapeKind
	prim     TypeKind
	ref      *schema.Element
	nullable bool
}

// classify decides what a schema element becomes
func classify(e *schema.Element) (shape, error) {
	if e.Ref != "" {
		return shape{kind: shapeRef, ref: e}, nil
	}
	if len(e.OneOf) > 0 {
		return classifyOneOf(e)
	}

	types := slices.DeleteFunc(slices.Clone(e.Types), func(t string) bool { return t == "null" })
	nullable := len(types) < len(e.Types)
	switch {
	case len(types) > 1:
		return shape{}, schema.Errorf(e.Path, "multiple types %s are not supported", strings.Join(types, ", "))
	case len(types) == 0 && nullable:
		return shape{}, schema.Errorf(e.Path, "type \"null\" is only supported alongside another type or as a "+
			"\"oneOf\" branch")
	case len(types) == 0:
		return shape{kind: shapeAny}, nil
	}

	if e.Enum != nil {
		if types[0] != "string" {
			return shape{}, schema.Errorf(e.Path, "only string enums are supported, found type %q", types[0])
		}
		return shape{kind: shapeEnum, nullable: nullable}, nil
	}

	sh := shape{nullable: nullable}
	switch types[0] {
	case "string":
		sh.kind, sh.prim = shapePrimitive, String
	case "integer":
		sh.kind, sh.prim = shapePrimitive, Integer
	case "number":
		sh.kind, sh.prim = shapePrimitive, Number
	case "boolean":
		sh.kind, sh.prim = shapePrimitive, Boolean
	case "array":
		if e.Items == nil {
			return shape{}, schema.Errorf(e.Path, "an array requires \"items\"")
		}
		sh.kind = shapeArray
	case "object":
		sh.kind = shapeStruct
		if e.AdditionalProperties != nil {
			sh.kind = shapeMap
		}
	}
	return sh, nil
}

// classifyOneOf handles the two uses of "oneOf": a union of references to object definitions, and a nullable
// reference written as a "$ref" alongside {"type": "null"}.
func classifyOneOf(e *schema.Element) (shape, error) {
	var refs []*schema.Element
	nullable := false
	for _, br := range e.OneOf {
		switch {
		case br.Ref != "":
			refs = append(refs, br)
		case len(br.Types) == 1 && br.Types[0] == "null":
			nullable = true
		default:
			return shape{}, schema.Errorf(br.Path, "a \"oneOf\" branch must be a \"$ref\" or {\"type\": \"null\"}")
		}
	}
	if nullable {
		if len(refs) != 1 {
			return shape{}, schema.Errorf(e.Path, "a \"oneOf\" with a null branch must have exactly one other branch")
		}
		return shape{kind: shapeRef, ref: refs[0], nullable: true}, nil
	}
	if len(refs) < 2 {
		return shape{}, schema.Errorf(e.Path, "a union requires at least two variants")
	}
	return shape{kind: shapeUnion}, nil
}

func (b *builder) typeOf(e *schema.Element) (*Type, error) {
	sh, err := classify(e)
	if err != nil {
		return nil, err
	}
	switch sh.kind {
	case shapeAny:
		return &Type{Kind: Any}, nil
	case shapeRef:
		def, err := b.doc.Resolve(sh.ref)
		if err != nil {
			return nil, err
		}
		d := b.named[def]
		b.referenced[d] = true
		return &Type{Kind: Ref, Named: d, Nullable: sh.nullable}, nil
	case shapePrimitive:
		return &Type{Kind: sh.prim, Nullable: sh.nullable}, nil
	case shapeArray:
		return b.collection(Array, e.Items, sh.nullable)
	case shapeMap:
		return b.collection(Map, e.AdditionalProperties, sh.nullable)
	default:
		return nil, schema.Errorf(e.Path, "inline objects, enums and unions are not supported, move the schema to "+
			"\"$defs\" and use a \"$ref\"")
	}
}

// collection returns an Array or Map type whose elements are described by elem.
func (b *builder) collection(kind TypeKind, elem *schema.Element, nullable bool) (*Type, error) {
	t, err := b.typeOf(elem)
	if err != nil {
		return nil, err
	}
	return &Type{Kind: kind, Elem: t, Nullable: nullable}, nil
}

func (b *builder) define(d *NamedType, e *schema.Element) error {
	sh, err := classify(e)
	if err != nil {
		return err
	}
	if sh.nullable {
		return schema.Errorf(e.Path, "a definition cannot be nullable, make the references to it nullable instead")
	}
	d.Doc = e.Description
	switch sh.kind {
	case shapeStruct:
		return b.defineStruct(d, e)
	case shapeEnum:
		return b.defineEnum(d, e)
	case shapeUnion:
		return b.defineUnion(d, e)
	default:
		return schema.Errorf(e.Path, "a definition must be an object with \"properties\", a string "+
			"enum or a \"oneOf\" union")
	}
}

func (b *builder) defineStruct(d *NamedType, e *schema.Element) error {
	d.Kind = Struct
	required := map[string]bool{}
	for _, r := range e.Required {
		if e.Property(r) == nil {
			return schema.Errorf(e.Path, "required property %q is not declared in \"properties\"", r)
		}
		required[r] = true
	}
	for _, p := range e.Properties {
		t, err := b.typeOf(p.Element)
		if err != nil {
			return err
		}
		d.Fields = append(d.Fields, &Field{
			JSONName: p.Name,
			Doc:      p.Element.Description,
			Required: required[p.Name],
			Type:     t,
			Path:     p.Element.Path,
		})
	}
	return nil
}

func (b *builder) defineEnum(d *NamedType, e *schema.Element) error {
	d.Kind = Enum
	seen := map[string]bool{}
	for _, value := range e.Enum {
		if seen[value] {
			return schema.Errorf(e.Path, "enum value %q is listed more than once", value)
		}
		seen[value] = true
		d.Values = append(d.Values, value)
	}
	return nil
}

func (b *builder) defineUnion(d *NamedType, e *schema.Element) error {
	d.Kind = Union
	var defs []*schema.Def
	for _, br := range e.OneOf {
		def, err := b.doc.Resolve(br)
		if err != nil {
			return err
		}
		sh, err := classify(def.Element)
		if err != nil {
			return err
		}
		if sh.kind != shapeStruct {
			return schema.Errorf(br.Path, "union variants must reference an object definition")
		}
		defs = append(defs, def)
	}

	var err error
	if d.Discriminator, err = discriminator(e.OneOf[0].Path, defs[0].Element); err != nil {
		return err
	}
	seen := map[string]bool{}
	for i, def := range defs {
		p := def.Element.Property(d.Discriminator)
		if p == nil || p.Const == nil {
			return schema.Errorf(e.OneOf[i].Path, "variant has no \"const\" for discriminator property %q", d.Discriminator)
		}
		value := *p.Const
		if seen[value] {
			return schema.Errorf(e.OneOf[i].Path, "discriminator value %q is used by more than one variant", value)
		}
		seen[value] = true
		d.Variants = append(d.Variants, &Variant{Value: value, Named: b.named[def]})
	}
	b.unions = append(b.unions, d)
	return nil
}

// discriminator returns the property that selects a union variant.
func discriminator(path string, variant *schema.Element) (string, error) {
	var consts []string
	for _, p := range variant.Properties {
		if p.Element.Const != nil {
			consts = append(consts, p.Name)
		}
	}
	if len(consts) != 1 {
		return "", schema.Errorf(path, "cannot find the union discriminator: the variant must have exactly one "+
			"property with a \"const\", found %q", consts)
	}
	return consts[0], nil
}

// finaliseUnions moves each discriminator property out of the variant structs. It should not be a class variable or
// free-form, so does not live alongside the other fields. It marks definitions that are only referenced as variants of
// a single union, so they can be named after it.
func (b *builder) finaliseUnions(named []*NamedType) {
	for _, u := range b.unions {
		for _, v := range u.Variants {
			v.Named.Fields = slices.DeleteFunc(v.Named.Fields, func(f *Field) bool { return f.JSONName == u.Discriminator })
			v.Named.Tags = append(v.Named.Tags, &Tag{Union: u, Property: u.Discriminator, Value: v.Value})
		}
	}

	for _, d := range named {
		if len(d.Tags) == 1 && !b.referenced[d] {
			d.VariantOf = d.Tags[0].Union
		}
	}
}
