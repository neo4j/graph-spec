package model

import (
	"bytes"
	"encoding/json"
	"fmt"
)

type ConstraintType string

const (
	ConstraintTypeExists       ConstraintType = "EXISTS"
	ConstraintTypeKey          ConstraintType = "KEY"
	ConstraintTypePropertyType ConstraintType = "PROPERTY_TYPE"
	ConstraintTypeUnique       ConstraintType = "UNIQUE"
)

var ConstraintTypeValues = []ConstraintType{
	ConstraintTypeExists,
	ConstraintTypeKey,
	ConstraintTypePropertyType,
	ConstraintTypeUnique,
}

type ExtensionValueUnion interface {
	ExtensionValueType() string
	isExtensionValue()
}

type ExtensionValue struct {
	ExtensionValueUnion
}

func (w ExtensionValue) MarshalJSON() ([]byte, error) {
	if w.ExtensionValueUnion == nil {
		return []byte("null"), nil
	}
	return json.Marshal(w.ExtensionValueUnion)
}

func (w *ExtensionValue) UnmarshalJSON(data []byte) error {
	data = bytes.TrimSpace(data)
	if bytes.Equal(data, []byte("null")) {
		w.ExtensionValueUnion = nil
		return nil
	}

	var peek struct {
		Type string `json:"type"`
	}
	if err := json.Unmarshal(data, &peek); err != nil {
		return fmt.Errorf("ExtensionValue: invalid JSON: %w", err)
	}
	if peek.Type == "" {
		return fmt.Errorf("ExtensionValue: missing discriminator field %q", "type")
	}

	var v ExtensionValueUnion
	switch peek.Type {
	case "String":
		v = &String{}
	case "Boolean":
		v = &Boolean{}
	case "Long":
		v = &Long{}
	case "Double":
		v = &Double{}
	case "List":
		v = &List{}
	case "Map":
		v = &Map{}
	default:
		return fmt.Errorf("ExtensionValue: unknown type %q", peek.Type)
	}

	if err := json.Unmarshal(data, v); err != nil {
		return fmt.Errorf("ExtensionValue: invalid %q payload: %w", peek.Type, err)
	}

	w.ExtensionValueUnion = v
	return nil
}

type String struct {
	Type  string `json:"type"`
	Value string `json:"value"`
}

func (String) isExtensionValue() {}

func (String) ExtensionValueType() string { return "String" }

type Boolean struct {
	Type  string `json:"type"`
	Value bool   `json:"value"`
}

func (Boolean) isExtensionValue() {}

func (Boolean) ExtensionValueType() string { return "Boolean" }

type Long struct {
	Type  string `json:"type"`
	Value int    `json:"value"`
}

func (Long) isExtensionValue() {}

func (Long) ExtensionValueType() string { return "Long" }

type Double struct {
	Type  string  `json:"type"`
	Value float64 `json:"value"`
}

func (Double) isExtensionValue() {}

func (Double) ExtensionValueType() string { return "Double" }

type List struct {
	Type  string           `json:"type"`
	Value []ExtensionValue `json:"value"`
}

func (List) isExtensionValue() {}

func (List) ExtensionValueType() string { return "List" }

type Map struct {
	Type  string                    `json:"type"`
	Value map[string]ExtensionValue `json:"value"`
}

func (Map) isExtensionValue() {}

func (Map) ExtensionValueType() string { return "Map" }

type NodeDisplay struct {
	Extensions map[string]ExtensionValue `json:"extensions,omitempty"`
	X          float64                   `json:"x"`
	Y          float64                   `json:"y"`
}

type Display struct {
	Nodes map[string]NodeDisplay `json:"nodes,omitempty"`
}

type ForeignKeyReference struct {
	Extensions map[string]ExtensionValue `json:"extensions,omitempty"`
	Fields     []string                  `json:"fields,omitempty"`
	Table      string                    `json:"table"`
}

type ForeignKey struct {
	Extensions map[string]ExtensionValue `json:"extensions,omitempty"`
	Fields     []string                  `json:"fields"`
	References ForeignKeyReference       `json:"references"`
}

type MappingMode string

const (
	MappingModeMerge  MappingMode = "MERGE"
	MappingModeCreate MappingMode = "CREATE"
)

var MappingModeValues = []MappingMode{
	MappingModeMerge,
	MappingModeCreate,
}

type PropertyMapping struct {
	Field string `json:"field"`
}

type TargetMapping struct {
	Label      *string                    `json:"label,omitempty"`
	Node       *string                    `json:"node,omitempty"`
	Properties map[string]PropertyMapping `json:"properties,omitempty"`
}

type MappingUnion interface {
	MappingType() string
	isMapping()
}

type Mapping struct {
	MappingUnion
}

func (w Mapping) MarshalJSON() ([]byte, error) {
	if w.MappingUnion == nil {
		return []byte("null"), nil
	}
	return json.Marshal(w.MappingUnion)
}

func (w *Mapping) UnmarshalJSON(data []byte) error {
	data = bytes.TrimSpace(data)
	if bytes.Equal(data, []byte("null")) {
		w.MappingUnion = nil
		return nil
	}

	var peek struct {
		Type string `json:"type"`
	}
	if err := json.Unmarshal(data, &peek); err != nil {
		return fmt.Errorf("Mapping: invalid JSON: %w", err)
	}
	if peek.Type == "" {
		return fmt.Errorf("Mapping: missing discriminator field %q", "type")
	}

	var v MappingUnion
	switch peek.Type {
	case "NodeMapping":
		v = &NodeMapping{}
	case "RelationshipMapping":
		v = &RelationshipMapping{}
	case "QueryMapping":
		v = &QueryMapping{}
	case "LabelMapping":
		v = &LabelMapping{}
	default:
		return fmt.Errorf("Mapping: unknown type %q", peek.Type)
	}

	if err := json.Unmarshal(data, v); err != nil {
		return fmt.Errorf("Mapping: invalid %q payload: %w", peek.Type, err)
	}

	w.MappingUnion = v
	return nil
}

type NodeMapping struct {
	Keys       []string                   `json:"keys,omitempty"`
	MatchLabel *string                    `json:"matchLabel,omitempty"`
	Mode       *MappingMode               `json:"mode,omitempty"`
	Node       string                     `json:"node"`
	Properties map[string]PropertyMapping `json:"properties"`
	Table      string                     `json:"table"`
	Type       string                     `json:"type"`
}

func (NodeMapping) isMapping() {}

func (NodeMapping) MappingType() string { return "NodeMapping" }

type RelationshipMapping struct {
	From         TargetMapping              `json:"from"`
	Keys         []string                   `json:"keys,omitempty"`
	MatchLabel   *string                    `json:"matchLabel,omitempty"`
	Mode         *MappingMode               `json:"mode,omitempty"`
	Properties   map[string]PropertyMapping `json:"properties,omitempty"`
	Relationship string                     `json:"relationship"`
	Table        string                     `json:"table"`
	To           TargetMapping              `json:"to"`
	Type         string                     `json:"type"`
}

func (RelationshipMapping) isMapping() {}

func (RelationshipMapping) MappingType() string { return "RelationshipMapping" }

type QueryMapping struct {
	Query string `json:"query"`
	Table string `json:"table"`
	Type  string `json:"type"`
}

func (QueryMapping) isMapping() {}

func (QueryMapping) MappingType() string { return "QueryMapping" }

type LabelMapping struct {
	Field string `json:"field"`
	Table string `json:"table"`
	Type  string `json:"type"`
}

func (LabelMapping) isMapping() {}

func (LabelMapping) MappingType() string { return "LabelMapping" }

type Labels struct {
	Extensions map[string]ExtensionValue `json:"extensions,omitempty"`
	Identifier *string                   `json:"identifier,omitempty"`
	Implied    []string                  `json:"implied,omitempty"`
	Optional   []string                  `json:"optional,omitempty"`
}

type NodeConstraint struct {
	Extensions map[string]ExtensionValue `json:"extensions,omitempty"`
	Label      *string                   `json:"label,omitempty"`
	Name       *string                   `json:"name,omitempty"`
	Properties []string                  `json:"properties"`
	Type       ConstraintType            `json:"type"`
}

type IndexType string

const (
	IndexTypeFulltext IndexType = "FULLTEXT"
	IndexTypePoint    IndexType = "POINT"
	IndexTypeRange    IndexType = "RANGE"
	IndexTypeText     IndexType = "TEXT"
	IndexTypeVector   IndexType = "VECTOR"
	IndexTypeLookup   IndexType = "LOOKUP"
)

var IndexTypeValues = []IndexType{
	IndexTypeFulltext,
	IndexTypePoint,
	IndexTypeRange,
	IndexTypeText,
	IndexTypeVector,
	IndexTypeLookup,
}

type NodeIndex struct {
	Extensions map[string]ExtensionValue `json:"extensions,omitempty"`
	Labels     []string                  `json:"labels"`
	Name       *string                   `json:"name,omitempty"`
	Options    map[string]ExtensionValue `json:"options,omitempty"`
	Properties []string                  `json:"properties"`
	Type       IndexType                 `json:"type"`
}

type Neo4jScalar string

const (
	Neo4jScalarAny           Neo4jScalar = "ANY"
	Neo4jScalarBoolean       Neo4jScalar = "BOOLEAN"
	Neo4jScalarString        Neo4jScalar = "STRING"
	Neo4jScalarInteger       Neo4jScalar = "INTEGER"
	Neo4jScalarInteger8      Neo4jScalar = "INTEGER8"
	Neo4jScalarInteger16     Neo4jScalar = "INTEGER16"
	Neo4jScalarInteger32     Neo4jScalar = "INTEGER32"
	Neo4jScalarFloat         Neo4jScalar = "FLOAT"
	Neo4jScalarFloat32       Neo4jScalar = "FLOAT32"
	Neo4jScalarDate          Neo4jScalar = "DATE"
	Neo4jScalarDuration      Neo4jScalar = "DURATION"
	Neo4jScalarPoint         Neo4jScalar = "POINT"
	Neo4jScalarUUID          Neo4jScalar = "UUID"
	Neo4jScalarLocalDatetime Neo4jScalar = "LOCAL DATETIME"
	Neo4jScalarLocalTime     Neo4jScalar = "LOCAL TIME"
	Neo4jScalarZonedDatetime Neo4jScalar = "ZONED DATETIME"
	Neo4jScalarZonedTime     Neo4jScalar = "ZONED TIME"
)

var Neo4jScalarValues = []Neo4jScalar{
	Neo4jScalarAny,
	Neo4jScalarBoolean,
	Neo4jScalarString,
	Neo4jScalarInteger,
	Neo4jScalarInteger8,
	Neo4jScalarInteger16,
	Neo4jScalarInteger32,
	Neo4jScalarFloat,
	Neo4jScalarFloat32,
	Neo4jScalarDate,
	Neo4jScalarDuration,
	Neo4jScalarPoint,
	Neo4jScalarUUID,
	Neo4jScalarLocalDatetime,
	Neo4jScalarLocalTime,
	Neo4jScalarZonedDatetime,
	Neo4jScalarZonedTime,
}

type Neo4jTypeUnion interface {
	Neo4jTypeType() string
	isNeo4jType()
}

type Neo4jType struct {
	Neo4jTypeUnion
}

func (w Neo4jType) MarshalJSON() ([]byte, error) {
	if w.Neo4jTypeUnion == nil {
		return []byte("null"), nil
	}
	return json.Marshal(w.Neo4jTypeUnion)
}

func (w *Neo4jType) UnmarshalJSON(data []byte) error {
	data = bytes.TrimSpace(data)
	if bytes.Equal(data, []byte("null")) {
		w.Neo4jTypeUnion = nil
		return nil
	}

	var peek struct {
		Type string `json:"type"`
	}
	if err := json.Unmarshal(data, &peek); err != nil {
		return fmt.Errorf("Neo4jType: invalid JSON: %w", err)
	}
	if peek.Type == "" {
		return fmt.Errorf("Neo4jType: missing discriminator field %q", "type")
	}

	var v Neo4jTypeUnion
	switch peek.Type {
	case "ListType":
		v = &ListType{}
	case "ScalarType":
		v = &ScalarType{}
	case "VectorType":
		v = &VectorType{}
	default:
		return fmt.Errorf("Neo4jType: unknown type %q", peek.Type)
	}

	if err := json.Unmarshal(data, v); err != nil {
		return fmt.Errorf("Neo4jType: invalid %q payload: %w", peek.Type, err)
	}

	w.Neo4jTypeUnion = v
	return nil
}

type ListType struct {
	Items Neo4jScalar `json:"items"`
	Type  string      `json:"type"`
}

func (ListType) isNeo4jType() {}

func (ListType) Neo4jTypeType() string { return "ListType" }

type ScalarType struct {
	Scalar Neo4jScalar `json:"scalar"`
	Type   string      `json:"type"`
}

func (ScalarType) isNeo4jType() {}

func (ScalarType) Neo4jTypeType() string { return "ScalarType" }

type VectorType struct {
	Dimension *int        `json:"dimension,omitempty"`
	Items     Neo4jScalar `json:"items"`
	Type      string      `json:"type"`
}

func (VectorType) isNeo4jType() {}

func (VectorType) Neo4jTypeType() string { return "VectorType" }

type Property struct {
	Extensions map[string]ExtensionValue `json:"extensions,omitempty"`
	Key        *bool                     `json:"key,omitempty"`
	MustExist  *bool                     `json:"mustExist,omitempty"`
	Name       *string                   `json:"name,omitempty"`
	Type       *Neo4jType                `json:"type,omitempty"`
	Unique     *bool                     `json:"unique,omitempty"`
}

type Node struct {
	Constraints map[string]NodeConstraint `json:"constraints,omitempty"`
	Extensions  map[string]ExtensionValue `json:"extensions,omitempty"`
	Indexes     map[string]NodeIndex      `json:"indexes,omitempty"`
	Label       *string                   `json:"label,omitempty"`
	Labels      *Labels                   `json:"labels,omitempty"`
	Name        *string                   `json:"name,omitempty"`
	Properties  map[string]Property       `json:"properties,omitempty"`
}

type RelationshipConstraint struct {
	Extensions map[string]ExtensionValue `json:"extensions,omitempty"`
	Name       *string                   `json:"name,omitempty"`
	Options    map[string]ExtensionValue `json:"options,omitempty"`
	Properties []string                  `json:"properties"`
	Type       ConstraintType            `json:"type"`
}

type RelationshipIndex struct {
	Extensions map[string]ExtensionValue `json:"extensions,omitempty"`
	Name       *string                   `json:"name,omitempty"`
	Options    map[string]ExtensionValue `json:"options,omitempty"`
	Properties []string                  `json:"properties"`
	Type       IndexType                 `json:"type"`
}

type RelationshipTarget struct {
	Label    *string `json:"label,omitempty"`
	Node     *string `json:"node,omitempty"`
	Property *string `json:"property,omitempty"`
}

type Relationship struct {
	Constraints map[string]RelationshipConstraint `json:"constraints,omitempty"`
	Extensions  map[string]ExtensionValue         `json:"extensions,omitempty"`
	From        RelationshipTarget                `json:"from"`
	Indexes     map[string]RelationshipIndex      `json:"indexes,omitempty"`
	Name        *string                           `json:"name,omitempty"`
	Properties  map[string]Property               `json:"properties,omitempty"`
	To          RelationshipTarget                `json:"to"`
	Type        string                            `json:"type"`
}

type TableField struct {
	Extensions map[string]ExtensionValue `json:"extensions,omitempty"`
	Name       *string                   `json:"name,omitempty"`
	Size       *int                      `json:"size,omitempty"`
	Suggested  *Neo4jType                `json:"suggested,omitempty"`
	Supported  []Neo4jType               `json:"supported,omitempty"`
	Type       *string                   `json:"type,omitempty"`
}

type Table struct {
	Extensions  map[string]ExtensionValue `json:"extensions,omitempty"`
	Fields      map[string]TableField     `json:"fields,omitempty"`
	ForeignKeys map[string]ForeignKey     `json:"foreignKeys,omitempty"`
	PrimaryKeys []string                  `json:"primaryKeys,omitempty"`
	Source      string                    `json:"source"`
}

type GraphModel struct {
	Display       *Display                `json:"display,omitempty"`
	Mappings      []Mapping               `json:"mappings,omitempty"`
	Nodes         map[string]Node         `json:"nodes,omitempty"`
	Relationships map[string]Relationship `json:"relationships,omitempty"`
	Tables        map[string]Table        `json:"tables,omitempty"`
	Version       string                  `json:"version"`
}
