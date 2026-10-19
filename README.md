# Neo4j Ontology Graph Specification

A format and library for defining, validating and migrating ontologies for Neo4j, expressed as YAML or JSON, with
support for Java, Kotlin, JavaScript, TypeScript & Go.

## What is an ontology?

An **ontology** is a formal description of the things in a domain and how they relate: the kinds of entities that
exist, the relationships between them, and the properties and rules that apply to each.

For Neo4j, that is the **graph model** behind a graph, written down explicitly:

| Ontology concept | In Neo4j |
|---|---|
| Kind of entity | Node (identified by one or more labels) |
| Kind of connection | Relationship type, with its start and end nodes |
| Attribute | Property, with a type |
| Rule | Constraints (uniqueness, existence, keys) and indexes |

For example, "an `Actor` has an `id` and a `name`, and can have an `ACTED_IN` relationship to a `Movie`", forms a small ontology.

Neo4j is schema-optional, so a graph's model often lives only in people's heads, import scripts or diagrams. A
machine-readable ontology gives tools and people one shared source of truth for designing models, importing data into
the right shape, validating consistency, and exchanging models between tools.

## What's in this repository?

- **A format**: ontologies as YAML or JSON, described by a published [JSON Schema](ontology-graph-spec.schema.json).
- **Libraries**: one Kotlin codebase published for JVM, JS/TS and Go, providing:
  - **Encoding and decoding** between YAML/JSON and a typed `GraphModel`
  - **Validation**, reporting issues in a model
  - **Migration** from older or related model formats to the latest version, and back

### Example

An excerpt of a model describing actors, movies and the relationship between them (JSON):

```json
{
  "schema": "https://neo4j.com/docs/ontology-graph-spec/1.0.0/schema.json",
  "version": "1",
  "id": "movies",
  "name": "Movies",
  "description": "People and the films they appear in.",
  "nodes": {
    "Actor": {
      "label": "Actor",
      "description": "A person who performs in films.",
      "aliases": ["Performer"],
      "reference": "https://schema.org/Person",
      "properties": {
        "id":   { "type": "STRING", "key": true },
        "name": { "type": "STRING", "must_exist": true, "description": "Full name." }
      },
      "extensions": { "custom": { "owner": "catalogue-team" } }
    },
    "Movie": {
      "label": "Movie",
      "reference": "https://schema.org/Movie",
      "properties": {
        "id":    { "type": "STRING", "key": true },
        "genre": { "type": "STRING", "one_of": ["DRAMA", "COMEDY", "THRILLER"] }
      }
    }
  },
  "relationships": {
    "ACTED_IN": {
      "type": "ACTED_IN",
      "description": "An actor appeared in a film.",
      "reference": "https://schema.org/actor",
      "from": { "node": "Actor", "label": "Actor", "min_count": 0 },
      "to":   { "node": "Movie", "label": "Movie", "min_count": 1 },
      "properties": { "roles": { "type": "STRING" } }
    }
  }
}
```

> [!NOTE]
> Cardinality (`min_count`, `max_count`, `count`) and `one_of` are part of the model but not yet enforced in the database.

See [go/model/testdata/graph-spec-example.json](go/model/testdata/graph-spec-example.json) for a complete model,
including extensions.

## Installing

### Gradle

```gradle
implementation("org.neo4j.spec.graph:ontology-graph-spec:x.y.z")
```

### Maven

```xml
<dependency>
    <groupId>org.neo4j.spec.graph</groupId>
    <artifactId>ontology-graph-spec</artifactId>
    <version>x.y.z</version>
</dependency>
```

### npm

```bash
npm install -D @neo4j-importer/graph-spec
```

```typescript
import { GraphSpec } from "@neo4j-importer/graph-spec";

const model: GraphModel = GraphSpec.Json.decodeFromString(value);
```

### Go

```bash
go get github.com/neo4j/graph-spec/go@vX.Y.Z
```

The Go library bundles the Kotlin/Native library, which is embedded and loaded automatically. The runtime needs
`glibc` and `libstdc++`. See the [Go README](go/README.md) for details, including running without embedding.

## Repository layout

| Path | Contents |
|---|---|
| `src/commonMain` | Core Kotlin model, codecs, validation and migrations |
| `src/jvmMain`, `src/jsMain` | Platform-specific code for JVM and JS/TS |
| `src/bridge` | Kotlin/Native bridge used by the Go library |
| `go/` | Go library and generated Go types |
| `ontology-graph-spec.schema.json` | JSON Schema for the format |
| `docs/` | Documentation |

## Contributing

Releases are driven by labelling PRs; see [docs/RELEASING.md](docs/RELEASING.md).

## License

Apache License 2.0. See [LICENSE](LICENSE).
