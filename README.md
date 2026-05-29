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
go get github.com/neo4j/graph-spec/go@vx.y.z
```
