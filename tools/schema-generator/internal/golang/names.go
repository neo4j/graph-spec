package golang

import (
	"fmt"
	"strings"
	"unicode"

	"github.com/neo4j/graph-spec/tools/schema-generator/internal/model"
)

// typeName is the Go name of a named type. A definition only used as a variant of one union is prefixed with the
// union's name, unless its name already starts or ends with it.
func typeName(named *model.NamedType) string {
	name := identifier(named.Name)
	if u := named.VariantOf; u != nil {
		union := typeName(u)
		if !strings.HasPrefix(name, union) && !strings.HasSuffix(name, union) {
			name = union + camelCase(named.Name)
		}
	}
	return name
}

func valueName(named *model.NamedType, v string) string {
	return typeName(named) + camelCase(v)
}

func valuesVarName(named *model.NamedType) string {
	return typeName(named) + "Values"
}

func interfaceName(named *model.NamedType) string {
	return typeName(named) + "Variant"
}

func discriminatorMethod(named *model.NamedType) string {
	return typeName(named) + camelCase(named.Discriminator)
}

func markerName(named *model.NamedType) string { return "is" + typeName(named) }

type namespace map[string]string // Go identifiers to owners

func (ns namespace) claim(name, owner string) error {
	if prev, ok := ns[name]; ok {
		return fmt.Errorf("%s and %s are both named %s in Go", prev, owner, name)
	}
	ns[name] = owner
	return nil
}

// checkNames rejects schema names that have no Go name, and any two that end up with the same Go name.
func checkNames(m *model.Model) error {
	pkgNs := namespace{}
	for _, named := range m.NamedTypes {
		if identifier(named.Name) == "" {
			return fmt.Errorf("%s: cannot derive a Go name from %q", named.Path, named.Name)
		}
		if err := pkgNs.claim(typeName(named), named.Path); err != nil {
			return err
		}
		switch named.Kind {
		case model.Struct:
			if err := checkStructFields(named); err != nil {
				return err
			}
		case model.Enum:
			for _, v := range named.Values {
				if camelCase(v) == "" {
					return fmt.Errorf("%s: cannot derive a Go name for enum value %q", named.Path, v)
				}
				if err := pkgNs.claim(valueName(named, v), fmt.Sprintf("enum value %q of %s", v, named.Path)); err != nil {
					return err
				}
			}
			if err := pkgNs.claim(valuesVarName(named), "the list of values of "+named.Path); err != nil {
				return err
			}
		case model.Union:
			if err := pkgNs.claim(interfaceName(named), "the variant interface of "+named.Path); err != nil {
				return err
			}
		}
	}
	return nil
}

func checkStructFields(named *model.NamedType) error {
	structNs := namespace{}
	if len(named.Tags) > 0 {
		structNs["MarshalJSON"] = "the MarshalJSON method"
	}
	for _, t := range named.Tags {
		structNs[discriminatorMethod(t.Union)] = "the " + discriminatorMethod(t.Union) + " method"
	}
	for _, f := range named.Fields {
		if !validTagName(f.JSONName) {
			return fmt.Errorf("%s: property name %q cannot be used as a Go struct tag", f.Path, f.JSONName)
		}
		name := identifier(f.JSONName)
		if name == "" {
			return fmt.Errorf("%s: cannot derive a Go name for property %q", f.Path, f.JSONName)
		}
		if err := structNs.claim(name, fmt.Sprintf("property %q", f.JSONName)); err != nil {
			return fmt.Errorf("%s: %w", named.Path, err)
		}
	}

	// The discriminator properties written by MarshalJSON become fields of an anonymous struct, so they only need to be
	// distinct from each other.
	tagOwners := map[string]string{}
	for _, t := range named.Tags {
		name := identifier(t.Property)
		if name == "" {
			return fmt.Errorf("%s: cannot derive a Go name for discriminator property %q", named.Path, t.Property)
		}
		if prev, ok := tagOwners[name]; ok && prev != t.Property {
			return fmt.Errorf("%s: discriminator properties %q and %q are both named %s in Go", named.Path, prev,
				t.Property, name)
		}
		tagOwners[name] = t.Property
	}
	return nil
}

// acceptableChars are all acceptable non-alphanumeric chars that encoding/json accepts in a struct tag name
const acceptableChars = "!#$%&()*+-./:;<=>?@[]^_{|}~ "

// validTagName matches the names encoding/json accepts in a struct tag. Other names are silently ignored by
// encoding/json, which would fall back to the Go field name.
func validTagName(s string) bool {
	if s == "" {
		return false
	}
	for _, c := range s {
		if !unicode.IsLetter(c) && !unicode.IsDigit(c) && !strings.ContainsRune(acceptableChars, c) {
			return false
		}
	}
	return true
}
