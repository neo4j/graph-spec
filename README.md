# Ontology Graph spec

The Neo4j Ontology Graph Specification v1 and its SDK — a Kotlin Multiplatform
library (`src/`) with a JS/TS surface and a Go module (`go/`).

Draft; mirrors the shared proposal doc's 2026-09-25 state.

- `ontology-graph-spec.schema.json` - JSON Schema (draft 2020-12) for the ontology format; generated from the annotated Kotlin model and drift-checked in CI (ADR-0003) - do not hand-edit.
- `src/jvmTest/resources/ontology/` - example ontologies exercising the full surface, as test resources.
- `src/jvmTest/kotlin/spec/OntologyGraphSpecExamplesTest.kt` - the spec-validation gate (ADR-0007): compiles the schema against the 2020-12 meta-schema, then validates every example resource against it.
- `src/commonMain/kotlin/` - the SDK: model, codecs (JSON + YAML), validators, and the converter (`migrate/migration/graphSpec/`, ADR-0008).
- `go/` - the Go module: generated model (`go/model/model.go` - do not hand-edit), validation + migration via the Kotlin/Native bridge.

## SDKs

**JS/TS** (npm, types included):

```sh
npm install @neo4j/ontology-graph-spec
```

The package ships the `@JsExport` model twins and editors (e.g. `graphModelJs(...)`, `GraphModelEditor`) plus the generated `.d.mts` definitions.

**Go**:

```sh
go get github.com/neo4j/graph-spec/go@latest   # releases tagged go/vX.Y.Z
```

The module carries the generated `model` types plus `validation.Validate(model)` and `migration.ToOntologyGraphSpec(json, modelType)`, which call into the Kotlin source of truth via a Kotlin/Native bridge (runtime needs `glibc` + `libstdc++`; details in [go/README.md](go/README.md)).

**Kotlin/JVM**: coordinates are `org.neo4j:ontology-graph-spec`, but Maven publishing is not enabled yet (pending a release-policy decision) - consume from source for now.

## Run

JDK 17+ for Gradle (the system JDK may be older - point `JAVA_HOME` at a 17 install):

```sh
JAVA_HOME=$HOME/.local/share/jdks/temurin-17.jdk/Contents/Home ./gradlew jvmTest   # spec gate: schema + all examples
```

## Checks

```sh
./gradlew check --no-daemon    # Kotlin: JVM + JS + Native (includes the spec gate)
./gradlew spotlessCheck        # lint (ktlint + license header)
cd go && go test ./...         # Go module (via the Kotlin/Native bridge)
./go/scripts/generate-go-models.sh && git diff --exit-code   # Go model drift
./gradlew generateOntologyGraphSpecJsonSchema && git diff --exit-code   # schema drift (ADR-0003)
```

The full per-language gate table and CI mapping live in [AGENTS.md](AGENTS.md) under "Commands".

## Validation levels

Three layers, each understanding more of the document than the last:

- **JSON Schema** (`ontology-graph-spec.schema.json`) - document-as-text structure: required keys, closed objects, the type-token pattern, enums, numeric minimums, `minItems`, non-empty property names (ADR-0010). No conditionals, no cross-references; extension payloads are exempt (`extensionsMap` is open).
- **Codec decode** - "is this a `GraphModel` at all": required fields + JSON types. Lenient on purpose (`isLenient`, `ignoreUnknownKeys`), so it accepts some documents the schema rejects.
- **Model validators** (`Validations`) - the meaning-level rules the schema cannot express. `core` is the soundness floor; `namedExtensions` adds the `neo4j:*` / `ui:*` / `importer:*` rules; `importReady` / `bulkImportReady` = core + namedExtensions; `all` = everything.

Model-only rules (fire even on schema-valid documents):

- Cross-references: endpoint `node` to a `nodes` key; constraint `properties` to the same element's properties; `neo4j:index` properties to the element's properties; `importer:mapping` node/relationship/table refs.
- Cross-field semantics: `dimension` only on `VECTOR<...>`; exactly one of `label` / `labels`; endpoint `count` not combined with `min_count` / `max_count`; `min_count` <= `max_count`; constraint overlap (key redundancy, duplicated shorthand flags, existence/composite conflict, duplicate property sets); index options matching the index type.
- Blank strings the schema's presence checks miss: relationship `type`, `labels.identifier`, table column names, `canonicalQuery` tools' `cypher` (ADR-0011).
- Model-wide uniqueness: index and constraint names share one namespace.

A few rules live in both layers (the type-token pattern, numeric minimums, the `constraint_type` enum, `minItems`) as a safety net for programmatically built models - a schema-valid parsed document can never trip them. Set expectations accordingly: **schema-valid is not sound** (most issue codes are model-level only), and **zero model issues is not schema-valid** (header checks, closed-object typos, property-name emptiness are schema-only). For JS/TS and Go consumers the schema is not part of the shipped library - the model validators are the validation story there.

## Notes

- Nodes and relationships are keyed by local ids; the label lives in `label` / `labels.identifier`, the relationship type in `type`. Endpoint `node` references point at node ids. A relationship type shared across endpoint pairs sits under one id key per pair (allowed, but graph-type enforcement keys on the type alone and cannot distinguish pairs yet).
- Property types are tokens (`STRING`, `LIST<STRING>`, `LIST<ANY>`, `VECTOR<FLOAT>` + companion `dimension` field). No union types: a property is a single type or `ANY`.
- Relationship cardinality lives on the endpoints: `count` (exact) / `min_count` / `max_count`; absent = unconstrained.
- `tools` is a core field on node and relationship entries: `{ type: canonicalQuery | externalRequest | ..., name, description, ... }`, per-type shapes owner-defined.
- `extensions` at every level: first-party extensions as `neo4j:`-prefixed named keys with owner-defined shapes (not validated here), custom extensions as the fixed envelope under `custom` (`type` required; `$schema`, `name`, `definition` optional; `definition` free-form, never validated). Unknown extra fields are carried untouched.
- Constraint objects (`{ constraint_type: key|unique|mustExist, name?, properties: [...] }`) are the nameable alternative to the property shorthand flags.

## Migrating from graph spec 4.0.0

This repo was the graph spec (4.0.0); v1 is a clean break with no backwards
compatibility in the format. A one-way 4.0.0 → 1.0.0 converter ships in the
library (`migrate/migration/graphSpec/`, mapping table in ADR-0008); the
frozen 4.0.0 state is tagged `graph-spec-4.0.0`. The transition's decisions
are recorded in [`docs/adr/`](docs/adr/) (0001–0009). The 4.0.0 SDK's
pretty/internal duality is gone with it: no `internalise()`/`prettify()`
calls are needed anymore — `OntologyGraphSpec.Json.decodeFromString(doc,
Type.GRAPH_SPEC)` accepts both 4.0.0 serialisations via the converter's
identity normalisation (ADR-0008 §2).

## Releasing

Releases are automated via the Release workflow, triggered by merging a PR to `main` with one of these labels:

| Label | Effect |
|---|---|
| `release:alpha` | Increment or create the dangling alpha line on the current base |
| `release:patch` | Bump patch version (stable) |
| `release:minor` | Bump minor version (stable) |
| `release:major` | Bump major version (stable) |
| `release:promote` | Promote the newest dangling alpha base to stable (no bump) |

Only one of `release:major`, `release:minor`, `release:patch` may be set at a time.

Combinations:

| Labels | Effect |
|---|---|
| `release:alpha` alone | Continue the dangling alpha line if one exists, else create the next patch alpha |
| `release:alpha` + bump label | Bump the base, then create or increment the alpha line on that new base |
| `release:promote` alone | Promote the newest dangling alpha base to stable |

Invalid combinations (the workflow fails):

- `release:promote` + `release:alpha`
- `release:promote` + any bump label
- More than one bump label (`release:major` / `release:minor` / `release:patch`)

No manual `npm publish` or Gradle task is needed - just merge a labelled PR.
