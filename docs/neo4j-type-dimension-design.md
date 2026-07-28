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
  more verbose than the existing graph-spec for types. E.g. instead of the existing 
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
  option D's largest cost in the verbosity - `type` becomes an object for every property, and (because the field is 
  literally named `type`) the scalar case reads as the doubly-nested `"type": { "type": "STRING" }`.

## Chosen path: Option E

Given that:
* Option A is hacky and not clear to the user or developer
* Option B is nice for the spec, but brittle and requires more custom serialization
* Option C is unclear and can't support use-cases where multiple dimensions exist across values in supported fields

It seems like these options are not viable. 

Therefore, the main options are D and E. They both tie dimension to only the vector type in a schema-enforced way. 
They are both very similar but trade off verbosity between internal implemenation and user examples - option D is 
is more verbose for examples but simpler in implementation and option E is the reverse. I think, given the 
implementation simplification is not that great for option D, option E is the best path.

### Vocabulary

We kept graph-spec's own uppercase vocabulary inside the new object
(`VECTOR`/`LIST`/`FLOAT`, `ZONED DATETIME`, `ANY`) rather than adopting the data model's
lowercase spelling (`vector`/`array`/`float`, `datetime`, `null`-for-any). This changes only
the *shape*, not the spelling, so graph-spec consumers see a smaller conceptual break and the
migrations keep doing the vocabulary translation they already did (`array`↔`LIST`,
`datetime`↔`ZONED DATETIME`, `null`↔`ANY`, case).

### Kotlin model

- `Neo4jScalar` — a new enum of the scalar element types (the old enum minus the `LIST_*`
  and `VECTOR_*` variants), with `@SerialName` for the spaced names.
- `Neo4jType` — a `@Serializable sealed class` with three subtypes, serialized by kotlinx's
  **native sealed polymorphism** (no custom serializer):
  - `ScalarType(scalar: Neo4jScalar)` — `@SerialName("ScalarType")`
  - `ListType(items: Neo4jScalar)` — `@SerialName("ListType")`
  - `VectorType(items: Neo4jScalar, dimension: Int? = null)` — `@SerialName("VectorType")`
  The default class discriminator (`type`) supplies the wire discriminator, so each variant is
  `{ "type": "<SerialName>", … }`. The sealed hierarchy gives the compile-time guarantees:
  `dimension` exists only on `VectorType`, and a list/vector element is always a `Neo4jScalar`.

### Why a discriminated union (and native polymorphism)

The goal on the Go/JVM side was a proper, typed model — distinct `ScalarType`/`ListType`/
`VectorType` structs and a shared `Neo4jScalar` enum — not a single loose struct. That is only
achievable as a discriminated `oneOf`, which drove three choices:

- **Native sealed polymorphism (an ergonomics choice, not a necessity).** A custom
  `JsonContentPolymorphicSerializer` is *not* off the table: `ExtensionValue` and `Mapping` both
  use one today and generate a clean `oneOf` + `discriminator: { propertyName: "type" }` through
  the Go JSON-schema generator (`generateGraphModelJsonSchema`, which walks
  `GraphModel.serializer().descriptor`). The trick is that each supplies a hand-built
  `buildSerialDescriptor(…, PolymorphicKind.SEALED) { … }`; the generator only crashes
  (`ArrayIndexOutOfBoundsException`) on a *bare* `JsonContentPolymorphicSerializer` whose default
  descriptor has no field structure. So the choice between native sealed polymorphism and a
  custom serializer is about ergonomics, not feasibility — both auto-generate cleanly in Go.
  Native polymorphism is preferred because the framework injects the discriminator and derives
  the descriptor, so there is no hand-maintained descriptor or `selectDeserializer` to keep in
  sync (and no `@EncodeDefault` / `@OptIn(ExperimentalSerializationApi)`). It emits `Neo4jType`
  as `oneOf: [ScalarType, ListType, VectorType]` with `discriminator: { propertyName: "type" }`,
  each variant a distinct schema, and `Neo4jScalar` as a reusable string enum. A custom
  serializer would only be the tool of choice for a different wire *layout* (e.g. Option E's
  single-discriminator form); it cannot escape the objects-with-a-discriminator shape that Go
  auto-generation requires.
- **A dedicated `ScalarType` variant** (rather than a bare scalar or the `{type:"STRING"}` form)
  so all three branches are objects with a fixed discriminator const — the shape a discriminated
  `oneOf` requires.
- **`@SerialName("ScalarType"/"ListType"/"VectorType")`** matches the existing `Mapping →
  NodeMapping`/… convention, so the generated Go types get clean names instead of `SCALAR`/`LIST`/
  `VECTOR`.

The trade-off is verbosity for scalars (`{ "type": "ScalarType", "scalar": "STRING" }` vs the
old `"STRING"`), accepted in exchange for a clean, typed cross-language model.

### JS package

`Neo4jType` is exported to JS/TypeScript as a real discriminated-object union — the same
`@JsExport @JsPlainObject external interface` + `toJs`/`toClass` pattern already used by
`ExtensionValueJs` and `MappingJs`:

- `Neo4jTypeJs { type: string }` with `ScalarTypeJs { scalar }` / `ListTypeJs { items }` /
  `VectorTypeJs { items, dimension?: number }` — `scalar`/`items` are the scalar-name string.
- `PropertyJs.type`, `TableFieldJs.suggested`/`supported` now use `Neo4jTypeJs`.
- The `PropertyEditor`/`NodeEditor`/`RelationshipEditor` `setPropertyType` methods take a
  `Neo4jTypeJs` (previously a loosely-typed `String`).

Because the union is now a first-class exported type, the `generateTsUnions` build hack no
longer force-converts `Neo4jType` from an enum. It only surfaces the `Neo4jScalar` enum as a
string union (`Neo4jScalarJs`) applied to the scalar-name fields (`ScalarTypeJs.scalar`,
`ListTypeJs.items`, `VectorTypeJs.items`), giving frontend type-safety on the scalar name (with
the spaced serial names, e.g. `"LOCAL DATETIME"`).

### Migrations

Both migrations become near pass-throughs for the type shape:
- forward carries `items` + `dimension` through, translating vocabulary to uppercase;
- reverse reads the structured object and translates back to the data model's lowercase
  `{type, items, dimension}` (with `null` for `ANY`).

### Backward compatibility

graph-spec is pre-release (`v4.0.0-alpha.*`) and there are no persisted older graph-spec
documents to be compatible with, so I've not worried about backwards compatibility for the old 
flat-string type form. `Neo4jType` reads and writes the object form only. Interaction with the
data model format are unaffected.
