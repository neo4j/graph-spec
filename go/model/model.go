package model

import "encoding/json"

// Constraint object form (the alternative to the mustExist/unique/key shorthand flags). Nameable, so tooling can reference individual constraints.
type Constraint struct {
	// The constraint kind: key (unique + mustExist), unique, or mustExist.
	ConstraintType string `json:"constraint_type"`
	// Optional name, so tooling can reference the constraint.
	Name *string `json:"name,omitempty"`
	// Names of properties on the same element this constraint covers.
	Properties []string `json:"properties"`
}

// Endpoint cardinality: count is the exact form, min_count/max_count the ranged form; absent means unconstrained (0..*). Counts on to constrain relationships per from-instance; counts on from constrain per to-instance.
type Endpoint struct {
	// Exact relationship count per endpoint instance.
	Count *int `json:"count,omitempty"`
	// Maximum relationship count per endpoint instance.
	MaxCount *int `json:"max_count,omitempty"`
	// Minimum relationship count per endpoint instance.
	MinCount *int `json:"min_count,omitempty"`
	// A node id from the nodes map.
	Node string `json:"node"`
}

// Custom extension envelope, four fields: type required; $schema, name, definition optional. definition is free-form and never validated by this spec. Unknown extra fields are carried untouched.
type Extension struct {
	extra      map[string]json.RawMessage `json:"-"`
	Schema     *string                    `json:"$schema,omitempty"`
	Definition interface{}                `json:"definition,omitempty"`
	Name       *string                    `json:"name,omitempty"`
	Type       string                     `json:"type"`
}

// Named extensions: first-party extensions as owner-prefixed keys (neo4j:*, ui:*, importer:*), each shape defined by its owner, available in the ontology graph spec SDK, not validated by this spec. Custom extensions ride the fixed envelope under the reserved custom key.
type ExtensionsMap struct {
	extra map[string]json.RawMessage `json:"-"`
	// Custom extensions, each in the fixed envelope.
	Custom []Extension `json:"custom,omitempty"`
}

// The full labels object: identifier (the main label) plus implied and optional labels. A node with no implied/optional labels uses the label shorthand instead.
type Labels struct {
	// The identifying (main) label.
	Identifier string `json:"identifier"`
	// Labels entailed by the identifying label (documented, never inferred).
	Implied []string `json:"implied,omitempty"`
	// Labels that may be present on instances but are not guaranteed.
	Optional []string `json:"optional,omitempty"`
}

// Type token: a Neo4j scalar, ANY, LIST<...> or VECTOR<...> (a VECTOR may carry a companion dimension field on the property). Element types are always scalars: no nested lists, no lists of vectors. VECTOR element types are numeric only: INTEGER or FLOAT (ADR-0012); LIST takes any scalar. No union types in v1: a property is a single type or ANY.
type PropertyType = string

// A single informational URI pointing at an external definition of this element (e.g. the original RDF resource).
type Reference = string

// A property of a node or relationship type: a type token, optional value constraints (one_of, pattern), the shorthand constraint flags, and metadata.
type Property struct {
	// Alternative names for this property.
	Aliases []string `json:"aliases,omitempty"`
	// Human-readable description of this property.
	Description *string `json:"description,omitempty"`
	// VECTOR companion: element count. Absent = unconstrained.
	Dimension  *int           `json:"dimension,omitempty"`
	Extensions *ExtensionsMap `json:"extensions,omitempty"`
	// Shorthand for a single-property key constraint (unique + mustExist).
	Key *bool `json:"key,omitempty"`
	// Shorthand for a single-property mustExist constraint.
	MustExist *bool `json:"mustExist,omitempty"`
	// Allowed values. JSON Schema's word. Default value support.
	OneOf []interface{} `json:"one_of,omitempty"`
	// Regular expression the property value must match.
	Pattern   *string       `json:"pattern,omitempty"`
	Reference *Reference    `json:"reference,omitempty"`
	Type      *PropertyType `json:"type,omitempty"`
	// Shorthand for a single-property unique constraint.
	Unique *bool `json:"unique,omitempty"`
}

// Core tool definition for agents: type discriminates (canonicalQuery, externalRequest, ...), name and description are the readable surface; remaining fields are defined per tool type by its owner (cypher, url, ...). Definitions only, no behaviour.
type Tool struct {
	extra       map[string]json.RawMessage `json:"-"`
	Description *string                    `json:"description,omitempty"`
	Name        *string                    `json:"name,omitempty"`
	Type        string                     `json:"type"`
}

// Every node carries its label in label (shorthand for an identifier-only labels) or labels.identifier (when implied/optional labels exist). Not schema-enforced; a node with neither is meaningless.
type Node struct {
	// Alternative names for this node type.
	Aliases []string `json:"aliases,omitempty"`
	// Constraint objects on this node type (the nameable alternative to the property flags).
	Constraints []Constraint `json:"constraints,omitempty"`
	// Human-readable description of this node type.
	Description *string        `json:"description,omitempty"`
	Extensions  *ExtensionsMap `json:"extensions,omitempty"`
	// Shorthand for labels.identifier when no implied/optional labels exist.
	Label *string `json:"label,omitempty"`
	// Full labels object: identifier plus implied/optional labels.
	Labels *Labels `json:"labels,omitempty"`
	// Properties of this node type; the map key is the property name.
	Properties map[string]Property `json:"properties,omitempty"`
	Reference  *Reference          `json:"reference,omitempty"`
	// Tools available on this node type.
	Tools []Tool `json:"tools,omitempty"`
}

// A relationship type entry: the type, its from/to endpoints with cardinality, properties, constraints, tools, and metadata.
type RelationshipEntry struct {
	// Alternative names for this relationship type.
	Aliases []string `json:"aliases,omitempty"`
	// Constraint objects on this relationship type (the nameable alternative to the property flags).
	Constraints []Constraint `json:"constraints,omitempty"`
	// Human-readable description of this relationship type.
	Description *string        `json:"description,omitempty"`
	Extensions  *ExtensionsMap `json:"extensions,omitempty"`
	// The start endpoint.
	From Endpoint `json:"from"`
	// Properties of this relationship type; the map key is the property name.
	Properties map[string]Property `json:"properties,omitempty"`
	Reference  *Reference          `json:"reference,omitempty"`
	// The end endpoint.
	To Endpoint `json:"to"`
	// Tools available on this relationship type.
	Tools []Tool `json:"tools,omitempty"`
	// The relationship type. One-to-one with the map key in the common case; several keys may share a type across different endpoint pairs.
	Type string `json:"type"`
}

// Draft JSON Schema for the Ontology Graph spec v1 proposal (2026-09-25 doc state, working-session revision). Nodes and relationships are keyed by local ids; the label/type lives inside the entry. Types are tokens. Tools are core. Extensions ride the named extensions map: first-party keys, custom envelope under custom; extension payloads are never validated by this schema.
type GraphModel struct {
	// Spec link including the version. Meta-level; doubles as document-type marker.
	Schema string `json:"$schema"`
	// Human-readable description of the ontology.
	Description *string        `json:"description,omitempty"`
	Extensions  *ExtensionsMap `json:"extensions,omitempty"`
	// Unique identifier of the ontology.
	ID string `json:"id"`
	// Human-readable name of the ontology.
	Name *string `json:"name,omitempty"`
	// Map key is the identifier within the document, not the label. The label lives in label / labels.identifier, so label renames do not break references.
	Nodes map[string]Node `json:"nodes,omitempty"`
	// Map key is the intra-document identifier; the relationship type lives in the type field. Several keys may share a type across different endpoint pairs, but note graph-type enforcement keys on the type alone and cannot distinguish pairs yet.
	Relationships map[string]RelationshipEntry `json:"relationships,omitempty"`
	// The ontology's own version. Identity metadata, not lifecycle.
	Version int `json:"version"`
}
