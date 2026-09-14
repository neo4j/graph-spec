# Ontology spec v1: proposal

Draft, 2026-09-09. Oskar Hane (WS1 spec lead).
Input: Anurag's onsite brief, Malmö workshop 1-3 Sep, this repo @ c61fbae (graph spec 4.0.0).

## Summary

Evolve the graph spec into the Ontology spec, version 1.0.0. Published for interop, governed by the ontology group, wrapped by end of October. Something usable at Graph Summit if console tooling opens.

**v1 is a clean break from 4.0.0.** No backwards compatibility in the format. A one-way 4.0.0 to 1.0.0 converter ships in the library so existing importer artifacts survive; the format itself does not carry the old shape. The converter maps 4.0.0's top-level `version` to `$schema`.

The deliverable is a written spec document plus a JSON Schema generated from it. Today the "spec" is a schema serialised from Kotlin classes with zero descriptions (Appendix B). There is no prose to patch; the document is the work.

## Design rules

From the brief, compressed:

1. Minimal and pragmatic. RDFS, not RDF, not OWL.
2. Least surprise. Feels like object orientation to an enterprise developer, not like RDF.
3. Schema optional. Descriptive first, enforcing over time. Enforcement is the direction we build toward.
4. No RDF inference.
5. Interop with RDF tooling, industry standards, Databricks-class platforms.
6. Bottom-up and top-down creation both supported.

Added for v1:

7. **Clean break.** v1 ignores backwards compatibility. The format gets to be right instead of compatible.
8. **One wire format.** The spec describes exactly one serialisation. 4.0.0 has two (pretty + internal); the duality is undocumented library convention (Appendix B). v1 specifies the human format. Normalisation stays a library detail.
9. **Extensions are typed objects.** Anything outside core rides as `{ type: "...", ... }` objects, describable by graph type. No namespaced keys.

## The format

Top level:

```yaml
$schema: "https://<url-to-schema>/1.0.0"   # ontology spec schema and version (required)
version: 3                # the ontology's own version (optional; lifecycle is WS3)
name: movies
description: Movie domain ontology
nodes: { <label>: Node }
relationships: { <type>: Relationship | [Relationship] }   # list = one entry per endpoint pair
extensions: [ ... ]       # ontology-scope objects (table, mapping)
```

`$schema` is the spec link including the version, meta-level (JSON-LD's `@context` precedent, JSON Schema's `$schema`). Plain `version` is reserved for the ontology's own version: identity metadata, not lifecycle. Publish, immutability and drift semantics are defined by the management layer (WS3), but a downloaded document should be able to say what it is.

Node:

| field | notes |
|---|---|
| map key | **the node label.** No identifier field, no shorthand |
| `labels` | optional, only `implied` + `optional`. Multi-label modelling, not a class hierarchy |
| `description` | |
| `aliases` | string list. Covers "aliases and synonyms" from the brief |
| `properties` | `{ <name>: Property }` |
| `constraints` | composite constraint objects only (multi-property keys/uniques) |
| `extensions` | typed objects (index, display, tool, ...) |

Property:

| field | notes |
|---|---|
| `type` | type object, see Type objects below. Scalar shorthand: bare token (`STRING`) |
| `mustExist`, `unique`, `key` | single-property constraint flags. The readable surface |
| `enum` | allowed values. JSON Schema's word |
| `pattern` | regex. JSON Schema's word |
| `description`, `aliases` | |
| `extensions` | |

Relationship:

| field | notes |
|---|---|
| map key / `type` | the relationship type |
| `from`, `to` | `{ node: <label> }`. One endpoint per field per entry. The same type between different node pairs is multiple entries: a relationship's identity is the triple (type, from, to), so the map value is a list when more than one pair exists. A bare object is shorthand for a one-entry list; the list is the normalized form. Each pair carries its own `cardinality`, `properties`, `constraints` and `extensions` |
| `cardinality` | optional: `ONE_TO_ONE`, `ONE_TO_MANY`, `MANY_TO_ONE`, `MANY_TO_MANY` |
| `properties`, `constraints` | as Node |
| `description`, `aliases`, `extensions` | |

Type objects:

A property type is an object, not a token. Parameterized types are decomposed into fields, never encoded into the type string: `VECTOR<FLOAT>(1042)` is not a value this spec uses. `kind` discriminates:

| kind | fields | example |
|---|---|---|
| `scalar` | `scalar` (Neo4j scalar type) | `{ kind: scalar, scalar: STRING }` |
| `list` | `items` (element scalar, required) | `{ kind: list, items: STRING }` |
| `vector` | `items` (element scalar, required), `dimension` (optional int) | `{ kind: vector, items: FLOAT, dimension: 1042 }` |

Scalars may be written as the bare token (`type: STRING`); the object form is the normalized form. `dimension` absent means unconstrained. Element types are always scalars: no nested lists, no lists of vectors.

Extension object:

```yaml
{ $schema: "https://<url-to-index-extension-schema>/0.1.0", type: "index", name: actor_names, definition: { properties: [name] } }
```

Fixed envelope, four fields. `type` required. `$schema` optional: the extension type's own schema and version, set and managed by its owner; never the ontology spec schema. `name` optional (a per-node display has no natural name). `definition` optional: free-form payload, never validated by this spec. The payload key is `definition`, not `properties`; that word is already overloaded in this spec.

The spec validates the envelope only. A `definition` is never validated by this spec: each extension type is defined, versioned and validated by its owner (the importer owns table/mapping, console owns display, the agent surface owns tool). Unknown types are carried untouched, never rejected. That is the brief's "which extensions do we allow, what validation do they require" answered structurally: none, and none.

Example extension types (with example type names to avoid type collisions), some used in the current graph spec:

| type | definition carries | was, in 4.0.0 |
|---|---|---|
| `neo4j:index` | `properties`, index kind | privileged `indexes` maps on Node/Relationship |
| `neo4j-importer:table` | `source`, `columns`, `primaryKeys`, `foreignKeys` | top-level `tables` |
| `neo4j-importer:mapping` | `kind` (node or relationship), endpoint/property mappings, `mode`, `key` | top-level `mappings` |
| `display` | position, colors, captions, icons, arrow types | top-level `display` (x/y only) |
| `tool` | `description`, `cypher`. Definitions for agents: canonical queries, named multi-hop paths, matching patterns. Definitions only, no behaviour | did not exist |

The 4.0.0 mapping discriminator (`type: NodeMapping`) collides with the envelope's `type`, so it moves into the definition as `kind`.

The brief's own words are "extended information as tools available to agents". Behaviour belongs to runtimes, not to a spec.

Serialization notes: one wire format, no shorthand forms. Absent means none: empty maps and lists are omitted, never written empty.

Placement rule: an extension lives on the element it describes. If it describes several elements or none (tables, mappings), it lives at the top level. Same envelope shape at both levels; validation and storage do not care where an extension sits.

## Example

```yaml
$schema: "https://<url-to-schema>/1.0.0"   # ontology spec schema and version
name: movies
description: Movie domain ontology

nodes:
  Actor:
    description: A person who acts in films
    aliases: [Performer]
    properties:
      id: { type: STRING, key: true }
      name:
        type: STRING
        mustExist: true
        description: Stage name
      status:
        type: STRING
        enum: [active, retired]
      imdb_id:
        type: STRING
        pattern: "^tt[0-9]{7,8}$"
    extensions:
      - type: index
        name: actor_names
        definition: { properties: [name] }
      - type: display
        definition: { color: "#e06209", caption: name }

  Person:
    labels:
      implied: [Employee]
      optional: [Contractor]
    properties:
      id: { type: STRING, key: true }

  Movie:
    properties:
      id: { type: STRING, key: true }
      title: { type: STRING, mustExist: true }
      embedding:
        type: { kind: vector, items: FLOAT, dimension: 1042 }
        description: Plot embedding

  Studio:
    properties:
      id: { type: STRING, key: true }
      name: { type: STRING, mustExist: true }

relationships:
  ACTED_IN:
    from: { node: Actor }
    to: { node: Movie }
    cardinality: MANY_TO_ONE
    description: Filmography
    properties:
      roles: { type: { kind: list, items: STRING } }
    extensions:
      - type: tool
        name: findCoActors
        definition:
          description: Actors who shared a film with a given actor
          cypher: MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(co:Actor) WHERE a.name = $name RETURN co

  PRODUCED: # same relationship between different node types
    - from: { node: Studio }
      to: { node: Movie }
      cardinality_type: ONE_TO_MANY
    - from: { node: Person }
      to: { node: Movie }
      cardinality_type: MANY_TO_ONE
      description: Individual producer credit

extensions:
  - $schema: "https://<url-to-table-extension-schema>/0.1.0"   # the table type's own schema and version (owner-managed, optional)
    type: neo4j-importer:table
    name: actors
    definition:
      source: sql/postgres
      columns: { actor_id: { type: varchar } }
  - type: neo4j-importer:mapping
    definition: { kind: node, node: Actor, table: actors, key: [actor_id] }
```

## Excluded

Per the brief, unchanged:

- RDF-based terminology: class, class hierarchy, axioms, ABox, TBox
- relationship and property hierarchies
- lineage and provenance
- explicit inference machinery, unless used for AI use cases

Plus, decided here:

- indexes as core fields (they are extension objects instead)
- SHACL as a validation model (its constraint vocabulary maps, see below; its open-world validation semantics do not)
- ontology version lifecycle and publish semantics as format concerns. That is the management layer's job (WS3). The format carries only identity: `$schema` (required, the spec schema link + version) and optional `version` (the ontology's own version)
- extension type definitions and validation. Owned, defined and validated by each extension type's owner; this spec defines only the envelope

## Enforcement mapping

The brief: "when specified, enforce as much as we can via graph schema, while leaving the rest as descriptive." Per field:

| enforceable via graph schema | descriptive only |
|---|---|
| labels (identifier structure), `type`, `mustExist`, `unique`, `key`, constraint objects | `description`, `aliases`, `cardinality`, `enum`, `pattern`, all extensions |

Comment [j] in the brief (ontology as superset of graph type, graph type inferable from ontology) hangs off this table. The left column is what a database can enforce on day one; the right column is what agents and tooling read.

## Interop coverage

### RDFS

| RDFS construct | v1 mapping | verdict |
|---|---|---|
| `rdfs:Class` | `nodes` entry | covered |
| `rdf:Property` | relationship type if range is a class, node property if range is a datatype | covered |
| `rdfs:domain` / `rdfs:range` | `from`/`to` endpoints, property `type` (xsd to Neo4jType table) | covered |
| `rdfs:label` | `name` / `aliases` | covered |
| `rdfs:comment` | `description` | covered |
| `rdfs:seeAlso`, `rdfs:isDefinedBy` | extension objects | carried, not core |
| `rdfs:subClassOf` | excluded as a mechanism. Info preservable: the transformer flattens hierarchy into `implied` labels at import time. No inference, materialisation is tooling's job | excluded from spec, info survives |
| `rdfs:subPropertyOf` | excluded ("relationship and property hierarchies", the brief's words) | excluded |
| containers (Bag/Seq/Alt), reification | not pragmatic | excluded |
| cardinality, enums, patterns | not RDFS (that is SHACL/OWL territory). v1 has them natively | n/a |

Two transformer-side notes, not spec concerns: RDF allows multiple domains/ranges per property (each endpoint pair is one entry under the type; the transformer expands accordingly), and `rdf:type` is instance-level (out of this spec's scope by definition).

### SHACL

| SHACL construct | v1 mapping | verdict |
|---|---|---|
| `sh:datatype` | property `type` | covered |
| `sh:in` | `enum` | covered |
| `sh:pattern` | `pattern` (flags fold inline, `(?i)`) | covered |
| `sh:minCount` >= 1 on property | `mustExist` | covered |
| `sh:minCount`/`sh:maxCount` on relationship | `cardinality` | covered, lossy (gap 1) |
| `sh:maxCount` > 1 on property | `type: { kind: list, items: ... }` | covered, lossy (no upper bound) |
| `sh:class` | relationship `to` endpoint label | covered |
| `sh:nodeKind` | the relationship-vs-property distinction itself | covered conceptually |
| `sh:name`, `sh:description` | `name`, `description` | covered |
| `sh:message`, `sh:severity` | extension (a validation type, owner-defined) | carried |
| `sh:minInclusive`/`maxExclusive` etc. (value ranges) | extension | carried, gap 2 |
| `sh:minLength`/`maxLength` | extension | carried, gap 3 |
| `sh:languageIn`, `sh:uniqueLang` | extension | carried |
| `sh:equals`, `sh:disjoint`, `sh:lessThan` (property-pair comparisons) | extension | carried, gap 4 |
| `sh:hasValue` | extension | carried |
| `sh:closed`, `sh:ignoredProperties` | extension. Neo4j is schema-optional; "closed" is enforcement policy, not ontology content | carried, semantics differ |
| `sh:order`, `sh:group` | extension, styling-adjacent | carried |
| property paths (inverse/alternative/sequence) | not constraints in v1. Sequence paths are named multi-hop paths: `tool` extensions | different mechanism, no loss |
| `sh:not`/`and`/`or`/`xone`, qualified cardinality | | excluded, not pragmatic |
| SHACL-SPARQL constraints | raw-text extension at best | excluded from core |
| SHACL-AF rules/functions | | excluded, inference-adjacent (principle 4) |

Gaps, ranked by how likely a customer notices:

1. **Exact cardinalities** (min 2, max 5) do not fit a 4-value enum. If field evidence demands it, v1.1 adds optional `minOccurrences`/`maxOccurrences` ints. Not v1.
2. **Value ranges.** Plausible in regulated-industry ontologies (ingredient classes, dosage). Candidate for a validation extension type, defined by its owner rather than this spec.
3. **String lengths.** Cheap to add later, same extension.
4. **Property-pair comparisons.** Rare in practice.

Two semantic caveats, one line each in the final spec:

- SHACL validates instance data post-load. v1 fields are descriptive first, enforced via graph schema where possible (principle 3). We map SHACL's constraint vocabulary; we do not adopt its validation model.
- SHACL binds shapes to data via targets (`sh:targetClass`). In v1 the binding is identity: a node entry is the shape for its label. No target machinery.

**Answer to the brief's open question** ("possibly SHACL rules for constraints"): the pragmatic SHACL subset maps natively, the rest rides extensions, nothing forces a shape change. WS1 ships the mapping appendix; transformation logic sits with tooling (importer team), which answers comment [l].

## Decisions needed from the core group

1. **Names.** Spec title ("Neo4j Ontology Specification" proposed), repo name (`neo4j/ontology-spec` proposed), governance group (brief says OLG; OSG was the 8 Sep lean. The artifact is a spec, not a language, which argues OSG; CLG consistency argues OLG).
2. **Importer sign-off** on indexes/tables/mappings becoming extension objects. Their bar stands: the spec must be downloadable in the initial ontology release. The 4.0.0 converter is the migration story for their artifacts.
3. **WS1/WS2 boundary** (comment [k]). The spec must not block on storage decisions and vice versa. The extension mechanism is the decoupler: storage-facing concerns ride extensions until WS2 lands.
4. **`tool` extension shape.** Owned by the agent-surface teams, not this spec. The kind question (query vs path vs pattern) lives in their definition.
5. **Extensions need no governance from this group.** The spec defines only the envelope; each extension type is defined, versioned and validated by its owner (importer owns `table`/`mapping`, console owns `display`, agent surface owns `tool`). Unknown types are carried untouched.

## Changelog from initial draft

2026-09-14:
- Multi-pair relationships. A relationship's identity is the triple (type, from, to); the map key alone could not hold the same type between different node pairs, and the transformer note's "expands to multiple relationship entries" contradicted unique map keys. Found by implementation: 65 of 80 relationship entries in the FOAF conversion are same-type, different pairs. The map value is now a list of entries, one per endpoint pair; a bare object is shorthand for a one-entry list. Per-pair `cardinality`, `properties`, `constraints`, `extensions`.

2026-09-11:
- Property `type` changed from token to type object: `{ kind: scalar|list|vector, ... }` with parameters as fields (`scalar`, `items`, `dimension`). A vector dimension is runtime data and can never live in an enum; `LIST<STRING>`-style tokens hit the same wall in codegen (protobuf enums, per-language serialization). Same shape the importer team landed on in their dimension-support branch. Scalars keep the bare-token shorthand, `type: STRING` stays legal.
  Before: `roles: { type: LIST }`, vectors as `type: VECTOR` + separate `dimension` field.
  After: `roles: { type: { kind: list, items: STRING } }`, `embedding: { type: { kind: vector, items: FLOAT, dimension: 1042 } }`.

2026-09-10:
- Deleted top-level `$version` and extension-level `$version`, replaced both with `$schema`, which links to the schema in a specific version. `$version` alone is not enough; now following JSON Schema's convention. `$schema` also answers the document-type ask (no sniffing structure to guess document kind).
- The spec does not define or validate extension types. Each extension type is defined, versioned and validated by its owner; the spec defines only the envelope.
- Extension type names namespaced by owner in examples (`neo4j-importer:table`) to avoid collisions.

## Appendix A: why each decision is what it is

**Clean break.** 4.0.0 self-flags experimental and was built by two teams for import tooling. A public standard prices every accident of that lineage forever. The converter keeps importer artifacts alive without keeping the old format alive.

**One wire format.** The pretty/internal split means the same model has two legal serialisations, and every consumer pays for both. The label/identifier alignment has already cost a dedicated fix commit (ed3fa28) and a validator rule that rejects the shorthand form unless the library normalises it first. Two formats is where drift lives. The internal normal form survives as an unexported library step for migration and validation, not as published spec.

**Map key is the label.** A node with no multi-label needs zero label boilerplate, and there is nothing left to disagree with the key. The alternative (always-explicit `labels.identifier`) duplicates the key on every node forever and needs a validator rule to catch disagreement. Multi-label stays explicit and visible exactly where it exists, which also makes the RDFS conversation cleaner: `implied` sits right there to argue over.

**Indexes out of core.** Indexes are physical layout, not meaning. The ontology is about meaning; graph schema information is in scope only as constraint information (the brief's list). Physical hints ride extensions, which is also what keeps WS1 independent of storage decisions.

**Extensions as typed objects.** A namespaced key (`"neo4j.table": {...}`) hides the type from graph type. An in-value `type` field makes extension shapes constrainable by graph type itself, which is exactly what WS2 needs to store the ontology in the database layer and expose it to Cypher without special-casing. Self-describing was the requirement; this shape is what makes it true.

**The envelope.** Same root schema on every extension (`type`, `name`, `definition`), specifics pushed one level down. Buys: envelope-only validation, a fixed meta-model for WS2 storage, and zero governance surface (a new extension type is defined by its owner and never touches this spec). Costs one nesting level. Worth it.

**tables/mappings/display as extension types.** The brief calls them adjacent specs composable into single artifacts. With a generic extension mechanism there is no case for three privileged top-level fields. One mechanism, one envelope; the known types are owned outside the spec (importer owns table/mapping, and so on).

**Property flags stay; constraint objects only for composites.** `mustExist`/`unique`/`key` cover the common case in the most readable form (principle 2). Composite keys need the object form. Both are one format with a documented relationship, not two formats.

**`enum`, `pattern`, `cardinality`, `aliases`.** `enum` and `pattern` are JSON Schema's words; the audience already knows them. The 4-value `cardinality` enum covers the pragmatic cases people actually model; exact counts are deferred to a later version on evidence, not on speculation.

**`tool` extensions, definitions only.** Decided 8 Sep: actions are definitions at annotation level, promotable to first-class fields in a future version. The brief's term is "tools". Behaviour in a spec rots; definitions compose.

**`implied`/`optional` kept, documented as multi-label.** Importer lineage, observable today in one validator rule (constraints attach to identifier union implied). Not subclassing. The hierarchy question belongs to the ontology group, not to the format.

**Versioning out of the format, identity in.** Publish, immutability, and drift are management-layer semantics (WS3). A format that bakes them in forces every consumer to implement a lifecycle they may not have. But the artifact's own identity is different: `name` and `description` were already there, and `version` is the same class. So `$schema` links the governing spec including its version (JSON Schema's `$schema` convention; `$`-prefixed fields are meta), and plain `version` is reserved for the ontology's own version, optional. Doing this at v1 costs nothing; reclaiming `version` later would cost a format break. The same sigil inside an extension links the extension type's own schema, owner-managed and never the spec's: one rule, `$schema` points at whatever governs the object. Extension evolution never forces an ontology spec release. `$schema` over a bare `$version`: the link identifies which schema AND its version, and it doubles as the document-type marker (no sniffing structure to guess what kind of document it is).

**Written document first.** Appendix B is the evidence that patching is not an option.

## Appendix B: what exists today (repo findings)

- The spec is `go/spec.json` (JSON Schema draft 2020-12), generated from the Kotlin model. **No prose document exists.** No `docs/`, README is install instructions, zero `description` fields in the schema.
- The words pretty, internal, shorthand appear nowhere in the schema. The dual format exists only in `Pretty.kt` / `Internal.kt` and tests.
- Validators assume the internal form: `NodeLabel` errors `missing_node_identifier_label` unless `labels.identifier` is set, so a shorthand document fails validation unless normalised first. The shorthand is a pre-validation convenience, not a specified format.
- `implied`/`optional` semantics are observable only in migration code (data model 3.0: first label token = identifier, rest = implied) and one validator rule (constraints attach to identifier union implied). Undocumented elsewhere.
- `Node` carries both `label` (string) and `labels` (object), both optional, nothing required except top-level `version`.

Consequence: the October deliverable is writing a spec, not patching one. Effort estimates should treat it that way.

## References

- Anurag's onsite brief: https://docs.google.com/document/d/1SzfZc1t7ESTMUBqQqwG9DfLK9KieIC3FeaN4Y4_GXSU (verified live 2026-09-09)
- Malmö workshop, 1-3 Sep: transcripts and Sudhir's day-1 summary in the mine graph
- This repo @ c61fbae, graph spec 4.0.0 (LATEST)
