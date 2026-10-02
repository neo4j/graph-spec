# Ontology spec

Two worlds coexist in this repo (ADR-0002):

- The **Neo4j Ontology Specification v1 overlay** - the schema and the validating examples listed below. Draft; mirrors the shared proposal doc's 2026-09-25 state.
- The **graph-spec 4.0.0 implementation** - the Kotlin Multiplatform model (`src/`), the Go module (`go/`), and the Gradle build (`gradle/`) - kept in place while the port re-targets it at the v1 format in place (ADR-0004).

Spec overlay contents:

- `ontology-spec.schema.json` - JSON Schema (draft 2020-12) for the ontology format.
- `src/jvmTest/resources/ontology/` - the example ontologies exercising the full surface, as test resources.
- `src/jvmTest/kotlin/spec/OntologySpecExamplesTest.kt` - the spec-validation gate (ADR-0007): compiles the schema against the 2020-12 meta-schema, then validates every example resource against it.

## Run

The spec gate is a Gradle test (JDK 17; the system JDK may be older - point `JAVA_HOME` at a 17 install):

```sh
JAVA_HOME=$HOME/.local/share/jdks/temurin-17.jdk/Contents/Home ./gradlew jvmTest   # spec schema + all examples
```

## Checks

The Gradle gate above covers the spec overlay. The 4.0.0 implementation has its own gates:

```sh
./gradlew check --no-daemon   # Kotlin: JVM + JS + Native (includes jvmTest)
cd go && go test ./...        # Go module (via the Kotlin/Native bridge)
```

The full per-language gate table (spotless, Go model drift, CI mapping) lives in [AGENTS.md](AGENTS.md) under "How to run checks".

## Notes

- Nodes and relationships are keyed by local ids; the label lives in `label` / `labels.identifier`, the relationship type in `type`. Endpoint `node` references point at node ids. A relationship type shared across endpoint pairs sits under one id key per pair (allowed, but graph-type enforcement keys on the type alone and cannot distinguish pairs yet).
- Property types are tokens (`STRING`, `LIST<STRING>`, `LIST<ANY>`, `VECTOR<FLOAT>` + companion `dimension` field). No union types: a property is a single type or `ANY`.
- Relationship cardinality lives on the endpoints: `count` (exact) / `min_count` / `max_count`; absent = unconstrained.
- `tools` is a core field on node and relationship entries: `{ type: canonicalQuery | externalRequest | ..., name, description, ... }`, per-type shapes owner-defined.
- `extensions` at every level: first-party extensions as `neo4j:`-prefixed named keys with owner-defined shapes (not validated here), custom extensions as the fixed envelope under `custom` (`type` required; `$schema`, `name`, `definition` optional; `definition` free-form, never validated). Unknown extra fields are carried untouched.
- Constraint objects (`{ constraint_type: key|unique|mustExist, name?, properties: [...] }`) are the nameable alternative to the property shorthand flags.

```xml
<dependency>
    <groupId>org.neo4j.importer</groupId>
    <artifactId>graph-spec</artifactId>
    <version>x.y.z</version>
</dependency>
```

### Node Package Manager

`npm install -D @neo4j-importer/graph-spec`

```typescript
import { GraphSpec } from "@neo4j-importer/graph-spec";

const model: GraphModel = GraphSpec.Json.decodeFromString(value);
```

### Go

```bash
go get github.com/neo4j/graph-spec/go/vX@vX.Y.Z
```

> [!NOTE]
> The Go library comes bundled with the Kotlin/Native library which is embedded and loaded automatically. 
> The runtime needs `glibc` and `libstdc++`. The Go library can also be run without automatic embedding if needed 
> (e.g. due to runtime restrictions) - see the [Go README](go/README.md).

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
