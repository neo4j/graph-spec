# Neo4j Graph Specification Format

Graph Spec is a format and library for representing graph models in YAML or JSON.
It provides library support for JVM, JavaScript, TypeScript & Go for migration and validation.

> [!WARNING]
> This repository is currently under construction and highly experimental.

## Installing

### TypeScript

`npm install -D @neo4j-importer/graph-spec`

```typescript
import { GraphSpec } from "@neo4j-importer/graph-spec";

const model: GraphModel = GraphSpec.Json.decodeFromString(value);
```
