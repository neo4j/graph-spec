package model

import (
	"bytes"
	"encoding/json"
	"fmt"
)

// The type of a constraint.
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

// The target of a foreign key.
type ForeignKeyReference struct {
	// Names of the referenced columns.
	Columns []string `json:"columns,omitempty"`
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
	// Id of the referenced table.
	Table string `json:"table"`
}

// A foreign key of a table.
type ForeignKey struct {
	// Names of the columns in this table which make up the key.
	Columns []string `json:"columns"`
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
	// The table and columns the key refers to.
	References ForeignKeyReference `json:"references"`
}

// Write approach used when importing data (merged into existing data or always created).
type MappingMode string

const (
	MappingModeMerge  MappingMode = "MERGE"
	MappingModeCreate MappingMode = "CREATE"
)

var MappingModeValues = []MappingMode{
	MappingModeMerge,
	MappingModeCreate,
}

// Maps a property to a source column.
type PropertyMapping struct {
	// Name of the source column.
	Column string `json:"column"`
}

// Identifies a node at one end of a mapped relationship.
type TargetMapping struct {
	// Label of the node.
	Label *string `json:"label,omitempty"`
	// Mappings from property ids to source columns used to match the node.
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
	case "NODE":
		v = &NODE{}
	case "QUERY":
		v = &QUERY{}
	case "RELATIONSHIP":
		v = &RELATIONSHIP{}
	default:
		return fmt.Errorf("Mapping: unknown type %q", peek.Type)
	}

	if err := json.Unmarshal(data, v); err != nil {
		return fmt.Errorf("Mapping: invalid %q payload: %w", peek.Type, err)
	}

	w.MappingUnion = v
	return nil
}

// Maps rows of a table to nodes.
type NODE struct {
	// Id of another mapping in the same list which must be imported before this one. Empty when there is no dependency.
	DependsOn *string `json:"depends_on,omitempty"`
	// Optional id of this mapping, so other mappings in the same list can reference it in depends_on.
	ID *string `json:"id,omitempty"`
	// Ids of the properties which identify a node.
	Key []string `json:"key,omitempty"`
	// Label used to match existing nodes.
	MatchLabel *string `json:"match_label,omitempty"`
	// Whether nodes are merged with existing nodes or always created.
	Mode *MappingMode `json:"mode,omitempty"`
	// Id of the node being mapped.
	Node string `json:"node"`
	// Mappings from property ids to source columns.
	Properties map[string]PropertyMapping `json:"properties"`
	// Id of the source table.
	Table string `json:"table"`
	Type  string `json:"type"`
}

func (NODE) isMapping() {}

func (NODE) MappingType() string { return "NODE" }

// Maps the results of a query run against a table.
type QUERY struct {
	// Id of another mapping in the same list which must be imported before this one. Empty when there is no dependency.
	DependsOn *string `json:"depends_on,omitempty"`
	// Optional id of this mapping, so other mappings in the same list can reference it in depends_on.
	ID *string `json:"id,omitempty"`
	// The query to run.
	Query string `json:"query"`
	// Id of the source table.
	Table string `json:"table"`
	Type  string `json:"type"`
}

func (QUERY) isMapping() {}

func (QUERY) MappingType() string { return "QUERY" }

// Maps rows of a table to relationships.
type RELATIONSHIP struct {
	// Id of another mapping in the same list which must be imported before this one. Empty when there is no dependency.
	DependsOn *string `json:"depends_on,omitempty"`
	// How the start node of the relationship is matched.
	FromNode TargetMapping `json:"from_node"`
	// Optional id of this mapping, so other mappings in the same list can reference it in depends_on.
	ID *string `json:"id,omitempty"`
	// Ids of the properties which identify a relationship.
	Key []string `json:"key,omitempty"`
	// Label used to match existing relationships.
	MatchLabel *string `json:"match_label,omitempty"`
	// Whether relationships are merged with existing relationships or always created.
	Mode *MappingMode `json:"mode,omitempty"`
	// Mappings from property ids to source columns.
	Properties map[string]PropertyMapping `json:"properties,omitempty"`
	// Id of the relationship being mapped.
	Relationship string `json:"relationship"`
	// Id of the source table.
	Table string `json:"table"`
	// How the end node of the relationship is matched.
	ToNode TargetMapping `json:"to_node"`
	Type   string        `json:"type"`
}

func (RELATIONSHIP) isMapping() {}

func (RELATIONSHIP) MappingType() string { return "RELATIONSHIP" }

// A Neo4j property type.
type Neo4jType string

const (
	Neo4jTypeAny               Neo4jType = "ANY"
	Neo4jTypeListAny           Neo4jType = "LIST<ANY>"
	Neo4jTypeBoolean           Neo4jType = "BOOLEAN"
	Neo4jTypeListBoolean       Neo4jType = "LIST<BOOLEAN>"
	Neo4jTypeDate              Neo4jType = "DATE"
	Neo4jTypeListDate          Neo4jType = "LIST<DATE>"
	Neo4jTypeDuration          Neo4jType = "DURATION"
	Neo4jTypeListDuration      Neo4jType = "LIST<DURATION>"
	Neo4jTypeFloat32           Neo4jType = "FLOAT32"
	Neo4jTypeListFloat32       Neo4jType = "LIST<FLOAT32>"
	Neo4jTypeFloat             Neo4jType = "FLOAT"
	Neo4jTypeListFloat         Neo4jType = "LIST<FLOAT>"
	Neo4jTypeInteger8          Neo4jType = "INTEGER8"
	Neo4jTypeListInteger8      Neo4jType = "LIST<INTEGER8>"
	Neo4jTypeInteger16         Neo4jType = "INTEGER16"
	Neo4jTypeListInteger16     Neo4jType = "LIST<INTEGER16>"
	Neo4jTypeInteger32         Neo4jType = "INTEGER32"
	Neo4jTypeListInteger32     Neo4jType = "LIST<INTEGER32>"
	Neo4jTypeInteger           Neo4jType = "INTEGER"
	Neo4jTypeListInteger       Neo4jType = "LIST<INTEGER>"
	Neo4jTypeLocalDatetime     Neo4jType = "LOCAL DATETIME"
	Neo4jTypeListLocalDatetime Neo4jType = "LIST<LOCAL DATETIME>"
	Neo4jTypeLocalTime         Neo4jType = "LOCAL TIME"
	Neo4jTypeListLocalTime     Neo4jType = "LIST<LOCAL TIME>"
	Neo4jTypePoint             Neo4jType = "POINT"
	Neo4jTypeListPoint         Neo4jType = "LIST<POINT>"
	Neo4jTypeString            Neo4jType = "STRING"
	Neo4jTypeListString        Neo4jType = "LIST<STRING>"
	Neo4jTypeVectorFloat       Neo4jType = "VECTOR<FLOAT>"
	Neo4jTypeVectorFloat32     Neo4jType = "VECTOR<FLOAT32>"
	Neo4jTypeVectorInteger     Neo4jType = "VECTOR<INTEGER>"
	Neo4jTypeVectorInteger32   Neo4jType = "VECTOR<INTEGER32>"
	Neo4jTypeVectorInteger16   Neo4jType = "VECTOR<INTEGER16>"
	Neo4jTypeVectorInteger8    Neo4jType = "VECTOR<INTEGER8>"
	Neo4jTypeZonedDatetime     Neo4jType = "ZONED DATETIME"
	Neo4jTypeListZonedDatetime Neo4jType = "LIST<ZONED DATETIME>"
	Neo4jTypeZonedTime         Neo4jType = "ZONED TIME"
	Neo4jTypeListZonedTime     Neo4jType = "LIST<ZONED TIME>"
	Neo4jTypeUUID              Neo4jType = "UUID"
)

var Neo4jTypeValues = []Neo4jType{
	Neo4jTypeAny,
	Neo4jTypeListAny,
	Neo4jTypeBoolean,
	Neo4jTypeListBoolean,
	Neo4jTypeDate,
	Neo4jTypeListDate,
	Neo4jTypeDuration,
	Neo4jTypeListDuration,
	Neo4jTypeFloat32,
	Neo4jTypeListFloat32,
	Neo4jTypeFloat,
	Neo4jTypeListFloat,
	Neo4jTypeInteger8,
	Neo4jTypeListInteger8,
	Neo4jTypeInteger16,
	Neo4jTypeListInteger16,
	Neo4jTypeInteger32,
	Neo4jTypeListInteger32,
	Neo4jTypeInteger,
	Neo4jTypeListInteger,
	Neo4jTypeLocalDatetime,
	Neo4jTypeListLocalDatetime,
	Neo4jTypeLocalTime,
	Neo4jTypeListLocalTime,
	Neo4jTypePoint,
	Neo4jTypeListPoint,
	Neo4jTypeString,
	Neo4jTypeListString,
	Neo4jTypeVectorFloat,
	Neo4jTypeVectorFloat32,
	Neo4jTypeVectorInteger,
	Neo4jTypeVectorInteger32,
	Neo4jTypeVectorInteger16,
	Neo4jTypeVectorInteger8,
	Neo4jTypeZonedDatetime,
	Neo4jTypeListZonedDatetime,
	Neo4jTypeZonedTime,
	Neo4jTypeListZonedTime,
	Neo4jTypeUUID,
}

// A column of a data source table.
type TableColumn struct {
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
	// Dimension of the column when it holds vectors.
	Dimension *int `json:"dimension,omitempty"`
	// Name of the column in the source. Unique within the table.
	Name *string `json:"name,omitempty"`
	// Size of the column.
	Size *int `json:"size,omitempty"`
	// Neo4j type suggested for values of this column.
	Suggested *Neo4jType `json:"suggested,omitempty"`
	// Neo4j types values of this column can be converted to when using the import tool.
	Supported []Neo4jType `json:"supported,omitempty"`
	// Data type of the column in the source.
	Type *string `json:"type,omitempty"`
}

// A data source table schema.
type Table struct {
	// Columns of the table in their source order.
	Columns []TableColumn `json:"columns,omitempty"`
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
	// Foreign keys of the table keyed by id.
	ForeignKeys map[string]ForeignKey `json:"foreign_keys,omitempty"`
	// Names of the columns making up the primary key.
	PrimaryKeys []string `json:"primary_keys,omitempty"`
	// Where the table's data comes from.
	Source string `json:"source"`
}

// Extensions for a graph model.
type GraphModelExtensions struct {
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
	// Sets of data source mappings from tables to nodes and relationships, often used for importing.
	Mappings map[string][]Mapping `json:"mappings,omitempty"`
	// Data source table schemas keyed by table id.
	Tables map[string]Table `json:"tables,omitempty"`
}

// Extensions for the labels of a node.
type LabelsExtensions struct {
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
}

// The labels of a node.
type Labels struct {
	// Extensions for the labels.
	Extensions *LabelsExtensions `json:"extensions,omitempty"`
	// The label which identifies the node.
	Identifier *string `json:"identifier,omitempty"`
	// Labels implied by the node in addition to the identifier.
	Implied []string `json:"implied,omitempty"`
	// Labels which may be present on the node but aren't required.
	Optional []string `json:"optional,omitempty"`
}

// Extensions for a node constraint.
type NodeConstraintExtensions struct {
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
}

// A constraint on a node.
type NodeConstraint struct {
	// Extensions for the constraint.
	Extensions *NodeConstraintExtensions `json:"extensions,omitempty"`
	// Label the constraint applies to.
	Label *string `json:"label,omitempty"`
	// Human-readable name. Used in place of the stable id in the pretty form of the model.
	Name *string `json:"name,omitempty"`
	// Ids of the properties the constraint covers.
	Properties []string `json:"properties"`
	// Type of the constraint.
	Type ConstraintType `json:"type"`
}

// Visual information for a node.
type NodeDisplay struct {
	// Display colour of the node as a hex string, e.g. #FF8800.
	Colour *string `json:"colour,omitempty"`
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
	// Horizontal position.
	X float64 `json:"x"`
	// Vertical position.
	Y float64 `json:"y"`
}

type IndexOptionUnion interface {
	IndexOptionType() string
	isIndexOption()
}

type IndexOption struct {
	IndexOptionUnion
}

func (w IndexOption) MarshalJSON() ([]byte, error) {
	if w.IndexOptionUnion == nil {
		return []byte("null"), nil
	}
	return json.Marshal(w.IndexOptionUnion)
}

func (w *IndexOption) UnmarshalJSON(data []byte) error {
	data = bytes.TrimSpace(data)
	if bytes.Equal(data, []byte("null")) {
		w.IndexOptionUnion = nil
		return nil
	}

	var peek struct {
		Type string `json:"type"`
	}
	if err := json.Unmarshal(data, &peek); err != nil {
		return fmt.Errorf("IndexOption: invalid JSON: %w", err)
	}
	if peek.Type == "" {
		return fmt.Errorf("IndexOption: missing discriminator field %q", "type")
	}

	var v IndexOptionUnion
	switch peek.Type {
	case "FULLTEXT":
		v = &FULLTEXT{}
	case "POINT":
		v = &POINT{}
	case "VECTOR":
		v = &VECTOR{}
	default:
		return fmt.Errorf("IndexOption: unknown type %q", peek.Type)
	}

	if err := json.Unmarshal(data, v); err != nil {
		return fmt.Errorf("IndexOption: invalid %q payload: %w", peek.Type, err)
	}

	w.IndexOptionUnion = v
	return nil
}

// Configuration of a full-text index.
type FULLTEXT struct {
	// Analyzer used by the index.
	FulltextAnalyzer *string `json:"fulltext.analyzer,omitempty"`
	// Analyzer used when the index doesn't specify one.
	FulltextDefaultAnalyzer *string `json:"fulltext.default_analyzer,omitempty"`
	// Whether the index is eventually consistent, updating asynchronously after transactions commit.
	FulltextEventuallyConsistent *bool `json:"fulltext.eventually_consistent,omitempty"`
	// Number of threads used to apply updates to an eventually consistent index.
	FulltextEventuallyConsistentApplyParallelism *int `json:"fulltext.eventually_consistent_apply_parallelism,omitempty"`
	// Maximum number of pending updates queued for an eventually consistent index.
	FulltextEventuallyConsistentIndexUpdateQueueMaxLength *int `json:"fulltext.eventually_consistent_index_update_queue_max_length,omitempty"`
	// How often an eventually consistent index is refreshed.
	FulltextEventuallyConsistentRefreshInterval *string `json:"fulltext.eventually_consistent_refresh_interval,omitempty"`
	// Number of threads used to refresh an eventually consistent index.
	FulltextEventuallyConsistentRefreshParallelism *int   `json:"fulltext.eventually_consistent_refresh_parallelism,omitempty"`
	Type                                           string `json:"type"`
}

func (FULLTEXT) isIndexOption() {}

func (FULLTEXT) IndexOptionType() string { return "FULLTEXT" }

// Configuration of a point index.
type POINT struct {
	// Upper bounds of the 3D cartesian space.
	SpatialCartesian3dMax []float64 `json:"spatial.cartesian-3d.max,omitempty"`
	// Lower bounds of the 3D cartesian space.
	SpatialCartesian3dMin []float64 `json:"spatial.cartesian-3d.min,omitempty"`
	// Upper bounds of the 2D cartesian space.
	SpatialCartesianMax []float64 `json:"spatial.cartesian.max,omitempty"`
	// Lower bounds of the 2D cartesian space.
	SpatialCartesianMin []float64 `json:"spatial.cartesian.min,omitempty"`
	// Upper bounds of the 3D WGS-84 space.
	SpatialWgs843dMax []float64 `json:"spatial.wgs-84-3d.max,omitempty"`
	// Lower bounds of the 3D WGS-84 space.
	SpatialWgs843dMin []float64 `json:"spatial.wgs-84-3d.min,omitempty"`
	// Upper bounds of the 2D WGS-84 space.
	SpatialWgs84Max []float64 `json:"spatial.wgs-84.max,omitempty"`
	// Lower bounds of the 2D WGS-84 space.
	SpatialWgs84Min []float64 `json:"spatial.wgs-84.min,omitempty"`
	Type            string    `json:"type"`
}

func (POINT) isIndexOption() {}

func (POINT) IndexOptionType() string { return "POINT" }

// Configuration of a vector index.
type VECTOR struct {
	Type string `json:"type"`
	// Default expansion factor applied to searches of the index.
	VectorDefaultSearchExpansionFactor *float64 `json:"vector.default_search_expansion_factor,omitempty"`
	// Number of dimensions of the indexed vectors.
	VectorDimensions *int `json:"vector.dimensions,omitempty"`
	// Number of nearest neighbours considered when building the HNSW graph.
	VectorHnswEfConstruction *int `json:"vector.hnsw.ef_construction,omitempty"`
	// Maximum number of connections per node in the HNSW graph.
	VectorHnswM *int `json:"vector.hnsw.m,omitempty"`
	// Whether vector quantization is enabled.
	VectorQuantizationEnabled *bool `json:"vector.quantization.enabled,omitempty"`
	// Type of vector quantization used.
	VectorQuantizationType *string `json:"vector.quantization.type,omitempty"`
	// Function used to compare vectors, e.g. cosine or euclidean.
	VectorSimilarityFunction *string `json:"vector.similarity_function,omitempty"`
}

func (VECTOR) isIndexOption() {}

func (VECTOR) IndexOptionType() string { return "VECTOR" }

// The type of an index.
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

// An index on a node.
type NodeIndex struct {
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
	// Labels the index covers.
	Labels []string `json:"labels"`
	// Human-readable name. Used in place of the stable id in the pretty form of the model.
	Name *string `json:"name,omitempty"`
	// Configuration specific to the type of index.
	Options *IndexOption `json:"options,omitempty"`
	// Ids of the properties the index covers.
	Properties []string `json:"properties"`
	// Type of the index.
	Type IndexType `json:"type"`
}

// Extensions for a node.
type NodeExtensions struct {
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
	// Optional visual information of this node.
	Display *NodeDisplay `json:"display,omitempty"`
	// Database indexes on this node keyed by id.
	Indexes map[string]NodeIndex `json:"indexes,omitempty"`
}

// Extensions for a property.
type PropertyExtensions struct {
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
}

// A property of a node or relationship.
type Property struct {
	// Description of the property.
	Description *string `json:"description,omitempty"`
	// Dimension of the property when it holds a vector.
	Dimension *int `json:"dimension,omitempty"`
	// Extensions for the property.
	Extensions *PropertyExtensions `json:"extensions,omitempty"`
	// Shorthand for a key constraint. Expanded into constraints when the model is internalised.
	Key *bool `json:"key,omitempty"`
	// Shorthand for an existence constraint. Expanded into constraints when the model is internalised.
	MustExist *bool `json:"must_exist,omitempty"`
	// Human-readable name. Used in place of the stable id in the pretty form of the model.
	Name *string `json:"name,omitempty"`
	// Values the property is restricted to.
	OneOf []string `json:"one_of,omitempty"`
	// Pattern values of the property are expected to match.
	Pattern *string `json:"pattern,omitempty"`
	// Reference or URI of an external resource describing the property.
	Reference *string `json:"reference,omitempty"`
	// Neo4j type of the property.
	Type *Neo4jType `json:"type,omitempty"`
	// Shorthand for a uniqueness constraint. Expanded into constraints when the model is internalised.
	Unique *bool `json:"unique,omitempty"`
}

// A node of the graph.
type Node struct {
	// Alternative names for the node.
	Aliases []string `json:"aliases,omitempty"`
	// Constraints on the node keyed by id.
	Constraints map[string]NodeConstraint `json:"constraints,omitempty"`
	// Description of the node.
	Description *string `json:"description,omitempty"`
	// Extensions for the node.
	Extensions *NodeExtensions `json:"extensions,omitempty"`
	// Shorthand for the identifying label. Moved to labels when the model is internalised.
	Label *string `json:"label,omitempty"`
	// Labels of the node.
	Labels *Labels `json:"labels,omitempty"`
	// Human-readable name. Used in place of the stable id in the pretty form of the model.
	Name *string `json:"name,omitempty"`
	// Properties of the node keyed by id.
	Properties map[string]Property `json:"properties,omitempty"`
	// Reference or URI of an external resource describing the node.
	Reference *string `json:"reference,omitempty"`
}

// Extensions for a relationship constraint.
type RelationshipConstraintExtensions struct {
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
}

// A constraint on a relationship.
type RelationshipConstraint struct {
	// Extensions for the constraint.
	Extensions *RelationshipConstraintExtensions `json:"extensions,omitempty"`
	// Human-readable name. Used in place of the stable id in the pretty form of the model.
	Name *string `json:"name,omitempty"`
	// Ids of the properties the constraint covers.
	Properties []string `json:"properties"`
	// Type of the constraint.
	Type ConstraintType `json:"type"`
}

// An index on a relationship.
type RelationshipIndex struct {
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
	// Human-readable name. Used in place of the stable id in the pretty form of the model.
	Name *string `json:"name,omitempty"`
	// Configuration specific to the type of index.
	Options *IndexOption `json:"options,omitempty"`
	// Ids of the properties the index covers.
	Properties []string `json:"properties"`
	// Type of the index.
	Type IndexType `json:"type"`
}

// Extensions for a relationship.
type RelationshipExtensions struct {
	// User-defined arbitrary data.
	Custom map[string]interface{} `json:"custom,omitempty"`
	// Indexes on this relationship keyed by id.
	Indexes map[string]RelationshipIndex `json:"indexes,omitempty"`
}

// One end of a relationship.
type RelationshipTarget struct {
	// Exact number of relationships per node at this end. Null when unconstrained.
	Count *int `json:"count,omitempty"`
	// Label of the node at this end.
	Label *string `json:"label,omitempty"`
	// Maximum number of relationships per node at this end.
	MaxCount *int `json:"max_count,omitempty"`
	// Minimum number of relationships per node at this end.
	MinCount *int `json:"min_count,omitempty"`
	// Id of the node at this end.
	Node *string `json:"node,omitempty"`
}

// A relationship of the graph.
type Relationship struct {
	// Alternative names for the relationship.
	Aliases []string `json:"aliases,omitempty"`
	// Constraints on the relationship keyed by id.
	Constraints map[string]RelationshipConstraint `json:"constraints,omitempty"`
	// Description of the relationship.
	Description *string `json:"description,omitempty"`
	// Extensions for the relationship.
	Extensions *RelationshipExtensions `json:"extensions,omitempty"`
	// The node the relationship starts at.
	From RelationshipTarget `json:"from"`
	// Human-readable name. Used in place of the stable id in the pretty form of the model.
	Name *string `json:"name,omitempty"`
	// Properties of the relationship keyed by id.
	Properties map[string]Property `json:"properties,omitempty"`
	// Reference or URI of an external resource describing the relationship.
	Reference *string `json:"reference,omitempty"`
	// The node the relationship ends at.
	To RelationshipTarget `json:"to"`
	// Type of the relationship.
	Type string `json:"type"`
}

type GraphModel struct {
	// Description of the model.
	Description *string `json:"description,omitempty"`
	// Extensions to the model: data source tables, mappings and external custom data.
	Extensions *GraphModelExtensions `json:"extensions,omitempty"`
	// Unique identifier of the model, typically a kebab-case version of the name, used for referencing.
	ID string `json:"id"`
	// Human readable name for the model.
	Name *string `json:"name,omitempty"`
	// Node definitions keyed by node id.
	Nodes map[string]Node `json:"nodes,omitempty"`
	// Relationship definitions keyed by relationship id.
	Relationships map[string]Relationship `json:"relationships,omitempty"`
	// Schema specification uri used.
	Schema string `json:"schema"`
	// User-defined version of this model document, e.g. a revision number or label.
	Version string `json:"version"`
}
