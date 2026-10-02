package model

import "encoding/json"

// Constraint object form (the alternative to the mustExist/unique/key shorthand flags). Nameable, so tooling can reference individual constraints.
type Constraint struct {
	ConstraintType string   `json:"constraint_type"`
	Name           *string  `json:"name,omitempty"`
	Properties     []string `json:"properties"`
}

// Endpoint cardinality: count is the exact form, min_count/max_count the ranged form; absent means unconstrained (0..*). Counts on to constrain relationships per from-instance; counts on from constrain per to-instance.
type Endpoint struct {
	Count    *int `json:"count,omitempty"`
	MaxCount *int `json:"max_count,omitempty"`
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

// Named extensions: first-party extensions as keys (neo4j:*), each shape defined by its owner, available in the ontology spec SDK, not validated by this spec. Custom extensions ride the fixed envelope under the reserved custom key.
type ExtensionsMap struct {
	extra  map[string]json.RawMessage `json:"-"`
	Custom []Extension                `json:"custom,omitempty"`
}

type Labels struct {
	// The identifying (main) label.
	Identifier string   `json:"identifier"`
	Implied    []string `json:"implied,omitempty"`
	Optional   []string `json:"optional,omitempty"`
}

// Type token: a Neo4j scalar, ANY, LIST<...> or VECTOR<...> (a VECTOR may carry a companion dimension field on the property). Element types are always scalars: no nested lists, no lists of vectors. No union types in v1: a property is a single type or ANY.
type PropertyType = string

// A single informational URI pointing at an external definition of this element (e.g. the original RDF resource).
type Reference = string

type Property struct {
	Aliases     []string `json:"aliases,omitempty"`
	Description *string  `json:"description,omitempty"`
	// VECTOR companion: element count. Absent = unconstrained.
	Dimension  *int           `json:"dimension,omitempty"`
	Extensions *ExtensionsMap `json:"extensions,omitempty"`
	Key        *bool          `json:"key,omitempty"`
	MustExist  *bool          `json:"mustExist,omitempty"`
	// Allowed values. JSON Schema's word. Default value support.
	OneOf     []interface{} `json:"one_of,omitempty"`
	Pattern   *string       `json:"pattern,omitempty"`
	Reference *Reference    `json:"reference,omitempty"`
	Type      *PropertyType `json:"type,omitempty"`
	Unique    *bool         `json:"unique,omitempty"`
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
	Aliases     []string       `json:"aliases,omitempty"`
	Constraints []Constraint   `json:"constraints,omitempty"`
	Description *string        `json:"description,omitempty"`
	Extensions  *ExtensionsMap `json:"extensions,omitempty"`
	// Shorthand for labels.identifier when no implied/optional labels exist.
	Label      *string             `json:"label,omitempty"`
	Labels     *Labels             `json:"labels,omitempty"`
	Properties map[string]Property `json:"properties,omitempty"`
	Reference  *Reference          `json:"reference,omitempty"`
	Tools      []Tool              `json:"tools,omitempty"`
}

type RelationshipEntry struct {
	Aliases     []string            `json:"aliases,omitempty"`
	Constraints []Constraint        `json:"constraints,omitempty"`
	Description *string             `json:"description,omitempty"`
	Extensions  *ExtensionsMap      `json:"extensions,omitempty"`
	From        Endpoint            `json:"from"`
	Properties  map[string]Property `json:"properties,omitempty"`
	Reference   *Reference          `json:"reference,omitempty"`
	To          Endpoint            `json:"to"`
	Tools       []Tool              `json:"tools,omitempty"`
	// The relationship type. One-to-one with the map key in the common case; several keys may share a type across different endpoint pairs.
	Type string `json:"type"`
}

// Draft JSON Schema for the Ontology spec v1 proposal (2026-09-25 doc state, working-session revision). Nodes and relationships are keyed by local ids; the label/type lives inside the entry. Types are tokens. Tools are core. Extensions ride the named extensions map: first-party keys, custom envelope under custom; extension payloads are never validated by this schema.
type GraphModel struct {
	// Spec link including the version. Meta-level; doubles as document-type marker.
	Schema      string         `json:"$schema"`
	Description *string        `json:"description,omitempty"`
	Extensions  *ExtensionsMap `json:"extensions,omitempty"`
	// Unique identifier of the ontology.
	ID   string  `json:"id"`
	Name *string `json:"name,omitempty"`
	// Map key is the identifier within the document, not the label. The label lives in label / labels.identifier, so label renames do not break references.
	Nodes map[string]Node `json:"nodes,omitempty"`
	// Map key is the intra-document identifier; the relationship type lives in the type field. Several keys may share a type across different endpoint pairs, but note graph-type enforcement keys on the type alone and cannot distinguish pairs yet.
	Relationships map[string]RelationshipEntry `json:"relationships,omitempty"`
	// The ontology's own version. Identity metadata, not lifecycle.
	Version int `json:"version"`
}
