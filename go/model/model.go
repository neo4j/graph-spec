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
		v = &AnyType{}
	case "BOOLEAN":
		v = &BooleanType{}
	case "DATE":
		v = &DateType{}
	case "DURATION":
		v = &DurationType{}
	case "FLOAT32":
		v = &Float32Type{}
	case "FLOAT":
		v = &FloatType{}
	case "INTEGER16":
		v = &Integer16Type{}
	case "INTEGER32":
		v = &Integer32Type{}
	case "INTEGER8":
		v = &Integer8Type{}
	case "INTEGER":
		v = &IntegerType{}
	case "LIST<BOOLEAN>":
		v = &ListBooleanType{}
	case "LIST<DATE>":
		v = &ListDateType{}
	case "LIST<DURATION>":
		v = &ListDurationType{}
	case "LIST<FLOAT32>":
		v = &ListFloat32Type{}
	case "LIST<FLOAT>":
		v = &ListFloatType{}
	case "LIST<INTEGER16>":
		v = &ListInteger16Type{}
	case "LIST<INTEGER32>":
		v = &ListInteger32Type{}
	case "LIST<INTEGER8>":
		v = &ListInteger8Type{}
	case "LIST<INTEGER>":
		v = &ListIntegerType{}
	case "LIST<LOCAL DATETIME>":
		v = &ListLocalDateTimeType{}
	case "LIST<LOCAL TIME>":
		v = &ListLocalTimeType{}
	case "LIST<POINT>":
		v = &ListPointType{}
	case "LIST<STRING>":
		v = &ListStringType{}
	case "LIST<ZONED DATETIME>":
		v = &ListZonedDateTimeType{}
	case "LIST<ZONED TIME>":
		v = &ListZonedTimeType{}
	case "LOCAL DATETIME":
		v = &LocalDateTimeType{}
	case "LOCAL TIME":
		v = &LocalTimeType{}
	case "POINT":
		v = &PointType{}
	case "STRING":
		v = &StringType{}
	case "UUID":
		v = &UuidType{}
	case "VECTOR<FLOAT32>":
		v = &VectorFloat32Type{}
	case "VECTOR<FLOAT>":
		v = &VectorFloatType{}
	case "VECTOR<INTEGER16>":
		v = &VectorInteger16Type{}
	case "VECTOR<INTEGER32>":
		v = &VectorInteger32Type{}
	case "VECTOR<INTEGER8>":
		v = &VectorInteger8Type{}
	case "VECTOR<INTEGER>":
		v = &VectorIntegerType{}
	case "ZONED DATETIME":
		v = &ZonedDateTimeType{}
	case "ZONED TIME":
		v = &ZonedTimeType{}
	default:
		return fmt.Errorf("Neo4jType: unknown type %q", peek.Type)
	}

	if err := json.Unmarshal(data, v); err != nil {
		return fmt.Errorf("Neo4jType: invalid %q payload: %w", peek.Type, err)
	}

	w.Neo4jTypeUnion = v
	return nil
}

type AnyType struct {
	Type string `json:"type"`
}

func (AnyType) isNeo4jType() {}

func (AnyType) Neo4jTypeType() string { return "ANY" }

type BooleanType struct {
	Type string `json:"type"`
}

func (BooleanType) isNeo4jType() {}

func (BooleanType) Neo4jTypeType() string { return "BOOLEAN" }

type DateType struct {
	Type string `json:"type"`
}

func (DateType) isNeo4jType() {}

func (DateType) Neo4jTypeType() string { return "DATE" }

type DurationType struct {
	Type string `json:"type"`
}

func (DurationType) isNeo4jType() {}

func (DurationType) Neo4jTypeType() string { return "DURATION" }

type Float32Type struct {
	Type string `json:"type"`
}

func (Float32Type) isNeo4jType() {}

func (Float32Type) Neo4jTypeType() string { return "FLOAT32" }

type FloatType struct {
	Type string `json:"type"`
}

func (FloatType) isNeo4jType() {}

func (FloatType) Neo4jTypeType() string { return "FLOAT" }

type Integer16Type struct {
	Type string `json:"type"`
}

func (Integer16Type) isNeo4jType() {}

func (Integer16Type) Neo4jTypeType() string { return "INTEGER16" }

type Integer32Type struct {
	Type string `json:"type"`
}

func (Integer32Type) isNeo4jType() {}

func (Integer32Type) Neo4jTypeType() string { return "INTEGER32" }

type Integer8Type struct {
	Type string `json:"type"`
}

func (Integer8Type) isNeo4jType() {}

func (Integer8Type) Neo4jTypeType() string { return "INTEGER8" }

type IntegerType struct {
	Type string `json:"type"`
}

func (IntegerType) isNeo4jType() {}

func (IntegerType) Neo4jTypeType() string { return "INTEGER" }

type ListBooleanType struct {
	Type string `json:"type"`
}

func (ListBooleanType) isNeo4jType() {}

func (ListBooleanType) Neo4jTypeType() string { return "LIST<BOOLEAN>" }

type ListDateType struct {
	Type string `json:"type"`
}

func (ListDateType) isNeo4jType() {}

func (ListDateType) Neo4jTypeType() string { return "LIST<DATE>" }

type ListDurationType struct {
	Type string `json:"type"`
}

func (ListDurationType) isNeo4jType() {}

func (ListDurationType) Neo4jTypeType() string { return "LIST<DURATION>" }

type ListFloat32Type struct {
	Type string `json:"type"`
}

func (ListFloat32Type) isNeo4jType() {}

func (ListFloat32Type) Neo4jTypeType() string { return "LIST<FLOAT32>" }

type ListFloatType struct {
	Type string `json:"type"`
}

func (ListFloatType) isNeo4jType() {}

func (ListFloatType) Neo4jTypeType() string { return "LIST<FLOAT>" }

type ListInteger16Type struct {
	Type string `json:"type"`
}

func (ListInteger16Type) isNeo4jType() {}

func (ListInteger16Type) Neo4jTypeType() string { return "LIST<INTEGER16>" }

type ListInteger32Type struct {
	Type string `json:"type"`
}

func (ListInteger32Type) isNeo4jType() {}

func (ListInteger32Type) Neo4jTypeType() string { return "LIST<INTEGER32>" }

type ListInteger8Type struct {
	Type string `json:"type"`
}

func (ListInteger8Type) isNeo4jType() {}

func (ListInteger8Type) Neo4jTypeType() string { return "LIST<INTEGER8>" }

type ListIntegerType struct {
	Type string `json:"type"`
}

func (ListIntegerType) isNeo4jType() {}

func (ListIntegerType) Neo4jTypeType() string { return "LIST<INTEGER>" }

type ListLocalDateTimeType struct {
	Type string `json:"type"`
}

func (ListLocalDateTimeType) isNeo4jType() {}

func (ListLocalDateTimeType) Neo4jTypeType() string { return "LIST<LOCAL DATETIME>" }

type ListLocalTimeType struct {
	Type string `json:"type"`
}

func (ListLocalTimeType) isNeo4jType() {}

func (ListLocalTimeType) Neo4jTypeType() string { return "LIST<LOCAL TIME>" }

type ListPointType struct {
	Type string `json:"type"`
}

func (ListPointType) isNeo4jType() {}

func (ListPointType) Neo4jTypeType() string { return "LIST<POINT>" }

type ListStringType struct {
	Type string `json:"type"`
}

func (ListStringType) isNeo4jType() {}

func (ListStringType) Neo4jTypeType() string { return "LIST<STRING>" }

type ListZonedDateTimeType struct {
	Type string `json:"type"`
}

func (ListZonedDateTimeType) isNeo4jType() {}

func (ListZonedDateTimeType) Neo4jTypeType() string { return "LIST<ZONED DATETIME>" }

type ListZonedTimeType struct {
	Type string `json:"type"`
}

func (ListZonedTimeType) isNeo4jType() {}

func (ListZonedTimeType) Neo4jTypeType() string { return "LIST<ZONED TIME>" }

type LocalDateTimeType struct {
	Type string `json:"type"`
}

func (LocalDateTimeType) isNeo4jType() {}

func (LocalDateTimeType) Neo4jTypeType() string { return "LOCAL DATETIME" }

type LocalTimeType struct {
	Type string `json:"type"`
}

func (LocalTimeType) isNeo4jType() {}

func (LocalTimeType) Neo4jTypeType() string { return "LOCAL TIME" }

type PointType struct {
	Type string `json:"type"`
}

func (PointType) isNeo4jType() {}

func (PointType) Neo4jTypeType() string { return "POINT" }

type StringType struct {
	Type string `json:"type"`
}

func (StringType) isNeo4jType() {}

func (StringType) Neo4jTypeType() string { return "STRING" }

type UuidType struct {
	Type string `json:"type"`
}

func (UuidType) isNeo4jType() {}

func (UuidType) Neo4jTypeType() string { return "UUID" }

type VectorFloat32Type struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorFloat32Type) isNeo4jType() {}

func (VectorFloat32Type) Neo4jTypeType() string { return "VECTOR<FLOAT32>" }

type VectorFloatType struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorFloatType) isNeo4jType() {}

func (VectorFloatType) Neo4jTypeType() string { return "VECTOR<FLOAT>" }

type VectorInteger16Type struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorInteger16Type) isNeo4jType() {}

func (VectorInteger16Type) Neo4jTypeType() string { return "VECTOR<INTEGER16>" }

type VectorInteger32Type struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorInteger32Type) isNeo4jType() {}

func (VectorInteger32Type) Neo4jTypeType() string { return "VECTOR<INTEGER32>" }

type VectorInteger8Type struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorInteger8Type) isNeo4jType() {}

func (VectorInteger8Type) Neo4jTypeType() string { return "VECTOR<INTEGER8>" }

type VectorIntegerType struct {
	Dimension *int   `json:"dimension,omitempty"`
	Type      string `json:"type"`
}

func (VectorIntegerType) isNeo4jType() {}

func (VectorIntegerType) Neo4jTypeType() string { return "VECTOR<INTEGER>" }

type ZonedDateTimeType struct {
	Type string `json:"type"`
}

func (ZonedDateTimeType) isNeo4jType() {}

func (ZonedDateTimeType) Neo4jTypeType() string { return "ZONED DATETIME" }

type ZonedTimeType struct {
	Type string `json:"type"`
}

func (ZonedTimeType) isNeo4jType() {}

func (ZonedTimeType) Neo4jTypeType() string { return "ZONED TIME" }

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
