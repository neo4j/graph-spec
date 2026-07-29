# Design: representing vector dimension in the property type system

## Context

Neo4j `VECTOR` property types have an optional dimension value (the number of elements in the
vector, e.g. `4`). The external "data model" format the UI produces represents a
type as a structured object and already carries this:

```json
{"type": "vector", "items": {"type": "float"}, "dimension": 4}
```

Graph-spec, however, currently represents a property type as a single flat enum string
(`Neo4jType`), e.g. `"STRING"`, `"LIST<STRING>"`, `"VECTOR<FLOAT>"`, with no capacity to store 
dimension. Therefore, when migrating from data-model to graph-spec this value is lost, 
and on a roundtrip data-model → graph-spec → data-model migration:

- `DataModelV3GraphSpecMigration.neo4jType` collapses `{"type": "vector", "items": {"type": "float"}, "dimension": 4}` to the string `"VECTOR<FLOAT>"`, reading only `items.type`
- `GraphSpecDataModelV3Migration.propertyType` rebuilds `{"type": "vector", "items": {"type": "float"}}` from the string, with no dimension to recover

We currently perform a roundtrip migration in staging for the candidate graph endpoint, and
as we integrate graph-spec further, more roundtrip migrations will be added. This is an 
issue for the list and vector type support project we want to complete.

This affects both the generated graph model's node/relationship property types and the
table-field `suggested`/`supported` types.

## Goal

### Functional

* Update graph-spec to fully support vector dimension

### Non-functional

* Keep verbosity to a minimum. One expectation is that humans could handcraft/modify a graph-spec document 
  so we want to avoid any overly verbose solutions
* Ideally avoid custom logic outside the core Kotlin code. Having custom code across different languages 
  (e.g. Go/TS) is a large maintenance burden.

## Options Considered

### Option A: stash dimension in the existing `extensions` map

Utilise the existing `extensions` metadata map for `Property`/`TableField` and put `dimension` in there.

- **Pros:** Zero schema change and minor change to the library.
- **Cons:** Very hacky and not clear to the user or obvious in the spec. Dimension is core model data, 
  not arbitrary metadata.

### Option B: parameterize the type string

Keep types as a single string and optionally encode the dimension at the end. E.g. `VECTOR<FLOAT>(4)`.

- **Pros:** Single field with high readability. Keeps commonality with Neo4j's `VECTOR<FLOAT>` syntax.
- **Cons:** Brittle and requires custom serialization which doesn't work nicely across languages.
  `Neo4jType` is currently a closed enum defined as a Kotlin enum and auto-generated in a Go string-const
  set, JSON schema and JS bridge. A parameterized value within this string doesn't fit into any of these
  enum sets and is only obtained via a customer deserializer. This is a possible source of bugs and also
  requires a customer Go deserializer as auto-generated code won't support it, which is a maintenance 
  burden. The auto-generated JSON schema will also be far less descriptive and potentially confusing.

### Option C: a separate optional `dimension: Int?` field

Keep the `Neo4jType` enum as the element-type descriptor and add a sibling `dimension` field on `Property`
and `TableField`. E.g.

```json
fields:  
  embedding:
    type: "VECTOR"
    suggested: "VECTOR<FLOAT>"
    supported: ["VECTOR<FLOAT>", "VECTOR<FLOAT32>"]
    dimension: 4
```

- **Pros:** Type enum stays closed so works nicely with JS, Go + JSON Schema. Type-safe and a small, 
  localized change.
- **Cons:** Dimension will be at a level that is above the desired level. It is specific to a type, and
  in theory `Property` or `TableField` can have multiple vector types with different dimensions, so this
  approach will only model simplified use-cases. It is also not clear or enforced from the spec/model that 
  dimension only applies to vector types. Separate validation will need to be added to validate dimension 
  is only present for vector types. 

### Option D: structured / nested type object

Replace the flat enum with a type object. This object would be one of three variants and form a discriminated
union via a `type` field, having value `ScalarType`, `ListType` or `VectorType`, with the vector type having 
an optional `dimension` value.

- **Pros:** Dimension is attached to the vector type itself, so each `supported` entry carries its dimension 
  and the ambiguity from Option C disappears. It is clear in the spec/model what dimension applies to and no 
  extra validation is needed. Fully supports all possible use-cases.
- **Cons:** The largest wire-format change - `type` becomes an object for every property, not just vectors,
  so every consumer that reads a property type as a string must change. It also makes a JSON/YAML file far 
  more verbose than the existing graph-spec for types as each is now an object. Also, because the field is
  literally named `type`, the scalar case reads as the doubly-nested `"type": { "type": "STRING" }`. We could 
  get around this by instead calling the nested value `kind`, but it's still verbose. E.g. instead of the existing 
  ```json
  "properties": {
    "flightId": { 
        "type": "INTEGER",
        "key": true
    }
  }
  ...
  "fields": {
    "embedding": {
      "type": "VECTOR",
      "suggested": "VECTOR<FLOAT>",
      "supported": ["VECTOR<FLOAT>", "VECTOR<FLOAT32>"]
    }
  }
  ```
  we get
  ```json
  "properties": {
    "flightId": {
      "type": { "type": "ScalarType", "scalar": "INTEGER"},
      "key": true 
    }
  }
  ...
  "fields": {
    "embedding": {
      "type": "VECTOR",
      "suggested": { "type": "VectorType", "scalar": "FLOAT", "dimension": 4 },
      "supported": [
        { "type": "VectorType", "scalar": "FLOAT", "dimension": 4 },
        { "type": "VectorType", "scalar": "FLOAT32", "dimension": 8 }
      ]
    }
  }
  ```

### Option E: structured union keyed on the full type name (single discriminator field)

A variant of Option D that keeps the discriminated-union-of-objects shape but trims the per-object verbosity.
Instead of three variants (`ScalarType`/`ListType`/`VectorType`), each carrying a separate element field 
(`scalar`/`items`), reuse graph-spec's existing full type names (`STRING`, `LIST<STRING>`, `VECTOR<FLOAT>`, ...) 
directly as the discriminator `const`. Every type name is its own variant and only the vector variants declare 
`dimension`. E.g.

```json
"properties": {
  "flightId": {
    "type": { "type": "INTEGER" },
    "key": true
  }
}
...
"fields": {
  "embedding": {
    "type": "VECTOR",
    "suggested": { "type": "VECTOR<FLOAT>", "dimension": 4 },
    "supported": [
      { "type": "VECTOR<FLOAT>", "dimension": 4 },
      { "type": "VECTOR<FLOAT32>", "dimension": 8 }
    ]
  }
}
```

- **Pros:** Same guarantees and benefits as Option D - dimension is only associated to the vector type, it is
  schema-enforced, and all use-cases are supported. It's less verbose per object than D and easier to read without
  the `scalar`/`items` field. It keeps graph-spec's existing `VECTOR<FLOAT>` spelling verbatim which were chosen
  as they tie in with Neo4j types. It also enables easily adding to existing types in the future if needed.
  
- **Cons:** The main downside is the verbosity in the spec and auto-generated code - the discriminator must carry 
  the full type identity, so the union expands to ~37 variants instead of option D's three. The auto-generated Go 
  gets ~37 flat structs rather than three typed variants (`ScalarType`/`ListType`/`VectorType`). It also still shares
  option D's largest cost in the verbosity - `type` becomes an object for every property. 

## Best Path

Given that:
* Option A is brittle, hacky and not clear to the user or developer
* Option B is concise within a spec, but implementation is brittle and requires more custom serialization and validation
* Option C is unclear and can't support use-cases where multiple dimensions exist across values in supported fields

It seems like these options are not viable. 

Therefore, the main options are D and E. They both tie dimension to only the vector type in a schema-enforced way. 
They are both very similar but trade off verbosity between internal implemenation and user examples - option D is 
more verbose for examples but simpler in implementation and option E is the reverse. 

**I think, given verbosity within the internal implementation is preferred over verbosity in the spec, option E is the
best path.**

### Implementation notes (Option E)

The type system must serialize as a discriminated union of objects - each variant an object with a shared `type` 
discriminator const, only the vector variants carrying `dimension`. This is a hard constraint of the codegen pipeline 
as we rely on auto-generated Go and TS code which requires this, and it also ensures the "dimension only applies to 
vectors" guarantee is schema-enforced.

- **Serialization.** The current ground-truth `Neo4jType` enum type in Kotlin changes to a top-level sealed interface,
  and has one concrete data sub-class per type (~38). Each types `@SerialName` remains unchanged from before (e.g. 
  `"STRING"`, `"VECTOR<FLOAT>"`, ...) which is used as the `type` discriminator value when deserializing.
- **JSON Schema / Go** - `Neo4jType` is emitted as `oneOf` over the ~38 variant schemas with 
  `discriminator: { propertyName: "type" }`. Polymorphism in the Go structs is handled in the idiomatic Go way, similar
  to how different constraint types are currently handled.
  The JSON Schema type goes from:
  ```json
  "Neo4jType": {
    "type": "string",
      "enum": [
        "ANY",
        "BOOLEAN",
        "LIST<BOOLEAN>",
        "DATE",
        ...  
  ```
  to
  ```json
  "Neo4jType": {
    "oneOf": [
      { "$ref": "#/$defs/ANY" },
      { "$ref": "#/$defs/BOOLEAN" },
      { "$ref": "#/$defs/LIST<BOOLEAN>" },
      { "$ref": "#/$defs/DATE" },
      ...
  ```

### Backward compatibility

graph-spec is pre-release (`v4.0.0-alpha.*`) and there are no persisted older graph-spec documents to be compatible 
with, so I've not worried about backwards compatibility for the old flat-string type form. `Neo4jType` reads and writes 
the object form only. Interaction with the data model format are unaffected.
