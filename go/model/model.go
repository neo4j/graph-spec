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
	case "ANY":
		v = &ANY{}
	case "BOOLEAN":
		v = &BOOLEAN{}
	case "DATE":
		v = &DATE{}
	case "DURATION":
		v = &DURATION{}
	case "FLOAT32":
		v = &FLOAT32{}
	case "FLOAT":
		v = &FLOAT{}
	case "INTEGER16":
		v = &INTEGER16{}
	case "INTEGER32":
		v = &INTEGER32{}
	case "INTEGER8":
		v = &INTEGER8{}
	case "INTEGER":
		v = &INTEGER{}
	case "LIST<BOOLEAN>":
		v = &ListBoolean{}
	case "LIST<DATE>":
		v = &ListDate{}
	case "LIST<DURATION>":
		v = &ListDuration{}
	case "LIST<FLOAT32>":
		v = &ListFloat32{}
	case "LIST<FLOAT>":
		v = &ListFloat{}
	case "LIST<INTEGER16>":
		v = &ListInteger16{}
	case "LIST<INTEGER32>":
		v = &ListInteger32{}
	case "LIST<INTEGER8>":
		v = &ListInteger8{}
	case "LIST<INTEGER>":
		v = &ListInteger{}
	case "LIST<LOCAL DATETIME>":
		v = &ListLocalDatetime{}
	case "LIST<LOCAL TIME>":
		v = &ListLocalTime{}
	case "LIST<POINT>":
		v = &ListPoint{}
	case "LIST<STRING>":
		v = &ListString{}
	case "LIST<ZONED DATETIME>":
		v = &ListZonedDatetime{}
	case "LIST<ZONED TIME>":
		v = &ListZonedTime{}
	case "LOCAL DATETIME":
		v = &LocalDatetime{}
	case "LOCAL TIME":
		v = &LocalTime{}
	case "POINT":
		v = &POINT{}
	case "STRING":
		v = &STRING{}
	case "UUID":
		v = &UUID{}
	case "VECTOR<FLOAT32>":
		v = &VectorFloat32{}
	case "VECTOR<FLOAT>":
		v = &VectorFloat{}
	case "VECTOR<INTEGER16>":
		v = &VectorInteger16{}
	case "VECTOR<INTEGER32>":
		v = &VectorInteger32{}
	case "VECTOR<INTEGER8>":
		v = &VectorInteger8{}
	case "VECTOR<INTEGER>":
		v = &VectorInteger{}
	case "ZONED DATETIME":
		v = &ZonedDatetime{}
	case "ZONED TIME":
		v = &ZonedTime{}
	default:
		return fmt.Errorf("Neo4jType: unknown type %q", peek.Type)
	}

	if err := json.Unmarshal(data, v); err != nil {
		return fmt.Errorf("Neo4jType: invalid %q payload: %w", peek.Type, err)
	}

	w.Neo4jTypeUnion = v
	return nil
}

type ANY struct {
	Type string `json:"type"`
}

func (ANY) isNeo4jType() {}

func (ANY) Neo4jTypeType() string { return "ANY" }

type BOOLEAN struct {
	Type string `json:"type"`
}

func (BOOLEAN) isNeo4jType() {}

func (BOOLEAN) Neo4jTypeType() string { return "BOOLEAN" }

type DATE struct {
	Type string `json:"type"`
}

func (DATE) isNeo4jType() {}

func (DATE) Neo4jTypeType() string { return "DATE" }

type DURATION struct {
	Type string `json:"type"`
}

func (DURATION) isNeo4jType() {}

func (DURATION) Neo4jTypeType() string { return "DURATION" }

type FLOAT32 struct {
	Type string `json:"type"`
}

func (FLOAT32) isNeo4jType() {}

func (FLOAT32) Neo4jTypeType() string { return "FLOAT32" }

type FLOAT struct {
	Type string `json:"type"`
}

func (FLOAT) isNeo4jType() {}

func (FLOAT) Neo4jTypeType() string { return "FLOAT" }

type INTEGER16 struct {
	Type string `json:"type"`
}

func (INTEGER16) isNeo4jType() {}

func (INTEGER16) Neo4jTypeType() string { return "INTEGER16" }

type INTEGER32 struct {
	Type string `json:"type"`
}

func (INTEGER32) isNeo4jType() {}

func (INTEGER32) Neo4jTypeType() string { return "INTEGER32" }

type INTEGER8 struct {
	Type string `json:"type"`
}

func (INTEGER8) isNeo4jType() {}

func (INTEGER8) Neo4jTypeType() string { return "INTEGER8" }

type INTEGER struct {
	Type string `json:"type"`
}

func (INTEGER) isNeo4jType() {}

func (INTEGER) Neo4jTypeType() string { return "INTEGER" }

type ListBoolean struct {
	Type string `json:"type"`
}

func (ListBoolean) isNeo4jType() {}

func (ListBoolean) Neo4jTypeType() string {
	return "LIST<BOOLEAN>"
}

type ListDate struct {
	Type string `json:"type"`
}

func (ListDate) isNeo4jType() {}

func (ListDate) Neo4jTypeType() string { return "LIST<DATE>" }

type ListDuration struct {
	Type string `json:"type"`
}

func (ListDuration) isNeo4jType() {}

func (ListDuration) Neo4jTypeType() string {
	return "LIST<DURATION>"
}

type ListFloat32 struct {
	Type string `json:"type"`
}

func (ListFloat32) isNeo4jType() {}

func (ListFloat32) Neo4jTypeType() string {
	return "LIST<FLOAT32>"
}

type ListFloat struct {
	Type string `json:"type"`
}

func (ListFloat) isNeo4jType() {}

func (ListFloat) Neo4jTypeType() string { return "LIST<FLOAT>" }

type ListInteger16 struct {
	Type string `json:"type"`
}

func (ListInteger16) isNeo4jType() {}

func (ListInteger16) Neo4jTypeType() string {
	return "LIST<INTEGER16>"
}

type ListInteger32 struct {
	Type string `json:"type"`
}

func (ListInteger32) isNeo4jType() {}

func (ListInteger32) Neo4jTypeType() string {
	return "LIST<INTEGER32>"
}

type ListInteger8 struct {
	Type string `json:"type"`
}

func (ListInteger8) isNeo4jType() {}

func (ListInteger8) Neo4jTypeType() string {
	return "LIST<INTEGER8>"
}

type ListInteger struct {
	Type string `json:"type"`
}

func (ListInteger) isNeo4jType() {}

func (ListInteger) Neo4jTypeType() string {
	return "LIST<INTEGER>"
}

type ListLocalDatetime struct {
	Type string `json:"type"`
}

func (ListLocalDatetime) isNeo4jType() {}

func (ListLocalDatetime) Neo4jTypeType() string {
	return "LIST<LOCAL DATETIME>"
}

type ListLocalTime struct {
	Type string `json:"type"`
}

func (ListLocalTime) isNeo4jType() {}

func (ListLocalTime) Neo4jTypeType() string {
	return "LIST<LOCAL TIME>"
}

type ListPoint struct {
	Type string `json:"type"`
}

func (ListPoint) isNeo4jType() {}

func (ListPoint) Neo4jTypeType() string { return "LIST<POINT>" }

type ListString struct {
	Type string `json:"type"`
}

func (ListString) isNeo4jType() {}

func (ListString) Neo4jTypeType() string {
	return "LIST<STRING>"
}

type ListZonedDatetime struct {
	Type string `json:"type"`
}

func (ListZonedDatetime) isNeo4jType() {}

func (ListZonedDatetime) Neo4jTypeType() string {
	return "LIST<ZONED DATETIME>"
}

type ListZonedTime struct {
	Type string `json:"type"`
}

func (ListZonedTime) isNeo4jType() {}

func (ListZonedTime) Neo4jTypeType() string {
	return "LIST<ZONED TIME>"
}

type LocalDatetime struct {
	Type string `json:"type"`
}

func (LocalDatetime) isNeo4jType() {}

func (LocalDatetime) Neo4jTypeType() string { return "LOCAL DATETIME" }

type LocalTime struct {
	Type string `json:"type"`
}

func (LocalTime) isNeo4jType() {}

func (LocalTime) Neo4jTypeType() string { return "LOCAL TIME" }

type POINT struct {
	Type string `json:"type"`
}

func (POINT) isNeo4jType() {}

func (POINT) Neo4jTypeType() string { return "POINT" }

type STRING struct {
	Type string `json:"type"`
}

func (STRING) isNeo4jType() {}

func (STRING) Neo4jTypeType() string { return "STRING" }

type UUID struct {
	Type string `json:"type"`
}

func (UUID) isNeo4jType() {}

func (UUID) Neo4jTypeType() string { return "UUID" }

type VectorFloat32 struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorFloat32) isNeo4jType() {}

func (VectorFloat32) Neo4jTypeType() string {
	return "VECTOR<FLOAT32>"
}

type VectorFloat struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorFloat) isNeo4jType() {}

func (VectorFloat) Neo4jTypeType() string {
	return "VECTOR<FLOAT>"
}

type VectorInteger16 struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorInteger16) isNeo4jType() {}

func (VectorInteger16) Neo4jTypeType() string {
	return "VECTOR<INTEGER16>"
}

type VectorInteger32 struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorInteger32) isNeo4jType() {}

func (VectorInteger32) Neo4jTypeType() string {
	return "VECTOR<INTEGER32>"
}

type VectorInteger8 struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorInteger8) isNeo4jType() {}

func (VectorInteger8) Neo4jTypeType() string {
	return "VECTOR<INTEGER8>"
}

type VectorInteger struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorInteger) isNeo4jType() {}

func (VectorInteger) Neo4jTypeType() string {
	return "VECTOR<INTEGER>"
}

type ZonedDatetime struct {
	Type string `json:"type"`
}

func (ZonedDatetime) isNeo4jType() {}

func (ZonedDatetime) Neo4jTypeType() string { return "ZONED DATETIME" }

type ZonedTime struct {
	Type string `json:"type"`
}

func (ZonedTime) isNeo4jType() {}

func (ZonedTime) Neo4jTypeType() string { return "ZONED TIME" }

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
