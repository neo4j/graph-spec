package schema

import (
	"fmt"
	"strings"
)

// Document is a loaded JSON Schema document.
type Document struct {
	Root   *Element
	Defs   []*Def // holds the sorted entries of "$defs"
	byName map[string]*Def
}

// Def is an entry of the root's "$defs".
type Def struct {
	Name    string
	Element *Element
}

// Element is a schema or subschema. Path is its JSON pointer within the document.
type Element struct {
	Path string

	Title       string
	Description string

	Ref                  string
	Types                []string
	Properties           []*Property
	Required             []string
	AdditionalProperties *Element
	Items                *Element
	Enum                 []string
	Const                *string
	OneOf                []*Element
}

// Property is an entry of "properties".
type Property struct {
	Name    string
	Element *Element
}

// Property returns the schema of the named property, or nil if e has none.
func (e *Element) Property(name string) *Element {
	for _, p := range e.Properties {
		if p.Name == name {
			return p.Element
		}
	}
	return nil
}

// Errorf is fmt.Sprintf with the JSON pointer path of the offending schema prepended. Package model uses it too.
func Errorf(path, format string, args ...any) error {
	return fmt.Errorf("%s: %s", path, fmt.Sprintf(format, args...))
}

// Resolve returns the definition the "$ref" of e points to. Only references to entries of the root's "$defs" are
// supported.
func (d *Document) Resolve(e *Element) (*Def, error) {
	name, ok := strings.CutPrefix(e.Ref, "#/$defs/")
	if !ok {
		return nil, Errorf(e.Path, "reference %q is not supported, only references such as \"#/$defs/Name\" are", e.Ref)
	}
	def := d.byName[name]
	if def == nil {
		return nil, Errorf(e.Path, "reference %q does not resolve to a definition", e.Ref)
	}
	return def, nil
}
