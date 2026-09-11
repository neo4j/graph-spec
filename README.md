# Neo4j Graph Specification Format

Graph Spec is a format and library for representing graph models in YAML or JSON.
It provides library support for JVM, JavaScript, TypeScript & Go for migration and validation.

> [!WARNING]
> This repository is currently under construction and highly experimental.

## Installing

### Gradle

```gradle
implementation("org.neo4j.importer:graph-spec:x.y.z")
```

### Maven

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
