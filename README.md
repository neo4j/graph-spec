# Ontology Graph spec

The Neo4j Ontology Graph Specification v1 and its SDK — a Kotlin Multiplatform
library (`src/`) with a JS/TS surface and a Go module (`go/`).

Draft; mirrors the shared proposal doc's 2026-09-25 state.

- `ontology-graph-spec.schema.json` - JSON Schema (draft 2020-12) for the ontology format; the hand-maintained source of truth (ADR-0003).
- `src/jvmTest/resources/ontology/` - example ontologies exercising the full surface, as test resources.
- `src/jvmTest/kotlin/spec/OntologyGraphSpecExamplesTest.kt` - the spec-validation gate (ADR-0007): compiles the schema against the 2020-12 meta-schema, then validates every example resource against it.
- `src/commonMain/kotlin/` - the SDK: model, codecs (JSON + YAML), validators, and the converter (`migrate/migration/graphSpec/`, ADR-0008).
- `go/` - the Go module: generated model (`go/model/model.go` - do not hand-edit), validation + migration via the Kotlin/Native bridge.

## SDKs

**JS/TS** (npm, types included):

```sh
npm install @neo4j-importer/ontology-graph-spec
```

The package ships the `@JsExport` model twins and editors (e.g. `graphModelJs(...)`, `GraphModelEditor`) plus the generated `.d.mts` definitions.

**Go**:

```sh
go get github.com/neo4j/graph-spec/go@latest   # releases tagged go/vX.Y.Z
```

The module carries the generated `model` types plus `validation.Validate(model)` and `migration.ToOntologyGraphSpec(json, modelType)`, which call into the Kotlin source of truth via a Kotlin/Native bridge (runtime needs `glibc` + `libstdc++`; details in [go/README.md](go/README.md)).

**Kotlin/JVM**: coordinates are `org.neo4j.importer:ontology-graph-spec`, but Maven publishing is not enabled yet (pending a release-policy decision) - consume from source for now.

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
```

The full per-language gate table and CI mapping live in [AGENTS.md](AGENTS.md) under "Commands".

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
are recorded in [`docs/adr/`](docs/adr/) (0001–0009).

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
