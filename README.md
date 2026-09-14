# Ontology spec v1 - JSON Schema + examples

Draft. Mirrors the shared proposal doc's 2026-09-14 state.

- `ontology-spec.schema.json` - JSON Schema (draft 2020-12) for the ontology format.
- `examples/` - example ontologies exercising the full surface.
- `scripts/validate.mjs` - validates the schema, then every example against it.

## Run

Requires Node >= 24.

```sh
npm install
npm run validate
```

## Notes

- Extensions validate the envelope only (`type` required; `$schema`, `name`, `definition` optional). `definition` is free-form by design and never validated here. Unknown extra fields on an extension are carried untouched.
- Composite constraint shape (`{ kind: key|unique, properties: [...] }`) is provisional, aligned with graph-spec 4.0.0's constraint object; the proposal names the construct but does not pin the shape yet.
- Example type names in extensions (`neo4j:index`, `neo4j-importer:table`, ...) are illustrative; their definitions are owned and versioned by their owners, not by this spec.

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
