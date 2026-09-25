# Ontology spec v1: proposal

Draft, 2026-09-09. Oskar Hane (WS1 spec lead).
Input: Anurag's onsite brief, Malmö workshop 1-3 Sep, this repo @ c61fbae (graph spec 4.0.0).

## Summary

Evolve the graph spec into the Ontology spec, version 1.0.0. Published for interop, governed by the ontology group, wrapped by end of October. Something usable at Graph Summit if console tooling opens.

**v1 is a clean break from 4.0.0.** No backwards compatibility in the format. A one-way 4.0.0 to 1.0.0 converter ships in the library so existing importer artifacts survive; the format itself does not carry the old shape. The converter maps 4.0.0's top-level `version` to `$schema`.

The deliverable is a written spec document plus a JSON Schema generated from it. Today the "spec" is a schema serialised from Kotlin classes with zero descriptions (Appendix B). There is no prose to patch; the document is the work. This might be reconsidered though, the work for that isn't significantly large.

## Design rules

From the brief, compressed:

1. Minimal and pragmatic. Partial RDFS + SHACL, not RDF, not OWL.
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
id: asd-asd-asd          # for identifying the spec when deployed (required)
version: 3               # the ontology's own version (required)
name: movies
description: Movie domain ontology
nodes: { <local-id>: Node }
relationships: { <local-id>: Relationship }
extensions: { <id>: {...}, custom: [ ... ] }   # ontology-scope objects (neo4j-importer:table, neo4j-importer:mapping)
```

`$schema` is the spec link including the version, meta-level (JSON-LD's `@context` precedent, JSON Schema's `$schema`). Plain `version` is reserved for the ontology's own version: identity metadata, not lifecycle. Publish, immutability and drift semantics are defined by the management layer (WS3), but a downloaded document should be able to say what it is.

Node:

| field | notes |
|---|---|
| map key | the identifier within the document, not the label. Label renames don't break references |
| `labels` | `{ identifier, implied?, optional? }`. Multi-label modelling, not a class hierarchy. `label` is the shorthand for setting the identifier only |
| `label` | the node label, shorthand form |
| `reference` | optional single URI pointing at an external definition of this element |
| `description` | |
| `aliases` | string list. Covers "aliases and synonyms" from the brief |
| `properties` | `{ <name>: Property }` |
| `constraints` | constraint objects, optionally named (`{ constraint_type, name?, properties }`); shorthand flags live on Property |
| `tools` | core tool definitions for agents (`{ type: canonicalQuery \| externalRequest \| ..., name, description, ... }`) |
| `extensions` | named first-party extensions (`neo4j:index`, `neo4j:display`, ...), custom under `custom` |

Property:

| field | notes |
|---|---|
| `type` | type token, see Data types below (`STRING`, `LIST<STRING>`, `VECTOR<FLOAT>`). `ANY` opts the property out of type constraints (mixed value types) |
| `dimension` | `VECTOR` companion: element count. Absent = unconstrained |
| `mustExist`, `unique`, `key` | single-property constraint flags. The readable surface |
| `one_of` | allowed values. JSON Schema's word. Default value support |
| `pattern` | regex. JSON Schema's word |
| `description`, `aliases` | |
| `reference` | optional single URI, as on Node |
| `extensions` | |

Relationship:

| field | notes |
|---|---|
| map key | the intra-document identifier. One-to-one id-to-type in the common case |
| `type` | the relationship type (required). Several keys may share a type across different endpoint pairs — allowed, but note graph-type enforcement keys on the type alone and cannot distinguish pairs yet |
| `from`, `to` | `{ node: <id>, count?, min_count?, max_count? }` — references a nodes-map key. Endpoint cardinality: `count` is the exact form, `min_count`/`max_count` the ranged form; absent means unconstrained (0..*). Counts on `to` constrain relationships per from-instance; counts on `from` constrain per to-instance |
| `properties`, `constraints` | as Node |
| `description`, `aliases` | |
| `reference` | optional single URI, as on Node |
| `tools` | as Node |
| `extensions` | |

Data types:

A property type is a token. Examples:

```yaml
type: LIST<STRING>
type: LIST<ANY>
type: STRING
type: INTEGER
type: VECTOR<FLOAT>   # vector can come with a companion dimension field
dimension: 512
```

Element types are always scalars: no nested lists, no lists of vectors. No union types in v1: a property is a single data type or `ANY`.

To play nice with existing dirty data, `type: ANY` is supported and implicitly opts the property out of any graph-type constraint.

## Extensions

First-party supported extensions (extensions we provide but that are not part of the core ontology) are **named extensions**: keys of an element's (or the top-level) `extensions`, `neo4j:`-prefixed, each shape defined by its owner and available in the ontology spec SDK. The naming differentiates them from custom extensions and improves the developer experience with types and autocomplete.

Custom extensions ride the fixed envelope under the reserved `custom` key:

```yaml
extensions:
  neo4j:index:
    name: actor_names
    properties: [name]
  custom:
    - $schema: "https://<url-to-index-extension-schema>/0.1.0"
      type: "index"
      name: actor_names
      description: ""
      definition: { properties: [name] }
```

The custom envelope has four fields. `type` required. `$schema` optional: the extension type's own schema and version, set and managed by its owner; never the ontology spec schema. `name` optional (a per-node display has no natural name). `definition` optional: free-form payload, never validated by this spec. The payload key is `definition`, not `properties`; that word is already overloaded in this spec.

The envelope always validates. Envelope-validated extensions are carried untouched, never rejected.

What extensions will exist, and their shapes, is addressed outside this main ontology spec.

The brief's own words are "extended information as tools available to agents". Behaviour belongs to runtimes, not to a spec.

Serialization notes: one wire format, no shorthand forms. Absent means none: empty maps and lists are omitted, never written empty.

Placement rule: an extension lives on the element it describes. If it describes several elements or none (tables, mappings), it lives at the top level. Same `extensions` shape at both levels; validation and storage do not care where an extension sits.

## Example

```yaml
$schema: "https://<url-to-schema>/1.0.0"   # ontology spec schema and version
version: 3                # ontology version
id: asd-asd-asd
name: movies
description: Movie domain ontology

nodes:
  Actor:
    label: Actor
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
        one_of: [active, retired]
      imdb_id:
        type: STRING
        pattern: "^tt[0-9]{7,8}$"
    extensions:
      neo4j:index:
        name: actor_names
        properties: [name]
      neo4j:display:
        color: "#e06209"
        caption: name

  person-id-field:
    reference: https://example.com/external/Person
    label: Person
    properties:
      id: { type: STRING, key: true }

  employee:
    labels:
      identifier: Employee
      implied: [Person]
      optional: [Contractor]
    properties:
      employee_id: { type: STRING, key: true }

  Movie:
    label: Movie
    properties:
      id: { type: STRING, key: true }
      title: { type: STRING, mustExist: true }
      embedding:
        type: VECTOR<FLOAT>
        dimension: 1042
        description: Plot embedding
        reference: https://example.com/external/embedding

  Studio:
    label: Studio
    properties:
      id: { type: STRING, key: true }
      name: { type: STRING, mustExist: true }

relationships:
  ACTED_IN:
    type: ACTED_IN
    from: { node: Actor }
    to: { node: Movie, max_count: 1 }
    description: Filmography
    properties:
      roles: { type: LIST<STRING> }
      mixed_list: { type: LIST<ANY> }
    tools:
      - type: externalRequest
        name: findRecentActingJobs
        description: Recent acting jobs for a given actor
        url: https://imdb.com/
      - type: canonicalQuery
        name: findCoActors
        description: Actors who shared a film with a given actor
        cypher: MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(co:Actor) WHERE a.name = $name RETURN co

  PRODUCED_STUDIO:
    type: PRODUCED       # same relationship type between different node pairs
    from: { node: Studio, count: 2 }
    to: { node: Movie, min_count: 1, max_count: 5 }   # cardinality 2 -> {1,5}
  PRODUCED_PERSON:
    type: PRODUCED
    from: { node: person-id-field }
    to: { node: Movie, max_count: 1 }                 # was: cardinality MANY_TO_ONE
    description: Individual producer credit

extensions:
  neo4j-importer:table:
    $schema: "https://<url-to-table-extension-schema>/0.1.0"   # the table type's own schema and version (owner-managed, optional)
    name: actors
    source: sql/postgres
    columns: { actor_id: { type: varchar } }
  neo4j-importer:mapping:
    kind: node
    node: Actor
    table: actors
    key: [actor_id]
```

## Excluded

Per the brief, unchanged:

- RDF-based terminology: class, class hierarchy, axioms, ABox, TBox
- relationship and property hierarchies
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
| labels (identifier structure), `type`, `mustExist`, `unique`, `key`, constraint objects | `description`, `aliases`, `count`/`min_count`/`max_count`, `one_of`, `pattern`, `reference`, `tools`, all extensions |

Comment [j] in the brief (ontology as superset of graph type, graph type inferable from ontology) hangs off this table. The left column is what a database can enforce on day one; the right column is what agents and tooling read.

## Interop coverage

### RDFS

| RDFS construct | v1 mapping | verdict |
|---|---|---|
| `rdfs:Class` | `nodes` entry | covered |
| `rdf:Property` | relationship type if range is a class, node property if range is a datatype | covered, with the known import ambiguity: no declared range means we cannot tell property from relationship (normal — RDFS does not distinguish them) |
| `rdfs:domain` / `rdfs:range` | `from`/`to` endpoints, property `type` (xsd to Neo4jType table) | covered |
| `rdfs:label` | `name` / `aliases` | covered |
| `rdfs:comment` | `description` | covered |
| `rdfs:seeAlso`, `rdfs:isDefinedBy` | extension objects | carried, not core |
| `rdfs:subClassOf` | excluded as a mechanism. Info preservable: the transformer flattens hierarchy into `implied` labels at import time. No inference, materialisation is tooling's job | excluded from spec, info survives |
| `rdfs:subPropertyOf` | excluded ("relationship and property hierarchies", the brief's words) | excluded |
| containers (Bag/Seq/Alt), reification | not pragmatic | excluded |
| cardinality, enums, patterns | not RDFS (that is SHACL/OWL territory). v1 has them natively | n/a |

Two transformer-side notes, not spec concerns: RDF allows multiple domains/ranges per property (each endpoint pair is its own entry under its own id key; the transformer expands accordingly), and `rdf:type` is instance-level (out of this spec's scope by definition).

### SHACL

| SHACL construct | v1 mapping | verdict |
|---|---|---|
| `sh:datatype` | property `type` | covered |
| `sh:in` | `one_of` | partially covered, applies on an element property level only |
| `sh:pattern` | `pattern` (flags fold inline, `(?i)`) | covered |
| `sh:minCount` >= 1 on property | `mustExist` | covered |
| `sh:minCount`/`sh:maxCount` on relationship | endpoint `min_count`/`max_count`/`count` | covered |
| `sh:maxCount` > 1 on property | `type: LIST<...>` | covered, lossy (no upper bound) |
| `sh:class` | relationship `to` endpoint label | covered |
| `sh:nodeKind` | the relationship-vs-property distinction itself | covered conceptually |
| `sh:name`, `sh:description` | `name`, `description` | covered |
| `sh:message`, `sh:severity` | extension (a validation type, owner-defined) | carried |
| `sh:minInclusive`/`maxExclusive` etc. (value ranges) | extension | carried, gap 1 |
| `sh:minLength`/`maxLength` | extension | carried, gap 2 |
| `sh:languageIn`, `sh:uniqueLang` | extension | carried |
| `sh:equals`, `sh:disjoint`, `sh:lessThan` (property-pair comparisons) | extension | carried, gap 3 |
| `sh:hasValue` | extension | carried |
| `sh:closed`, `sh:ignoredProperties` | extension. Neo4j is schema-optional; "closed" is enforcement policy, not ontology content | carried, semantics differ |
| `sh:order`, `sh:group` | extension, styling-adjacent | carried |
| property paths (inverse/alternative/sequence) | not constraints in v1. Sequence paths are named multi-hop paths: `tools` | different mechanism, no loss |
| `sh:not`/`and`/`or`/`xone`, qualified cardinality | | excluded, not pragmatic |
| SHACL-SPARQL constraints | raw-text extension at best | excluded from core |
| SHACL-AF rules/functions | | excluded, inference-adjacent (principle 4) |

Gaps, ranked by how likely a customer notices:

1. **Value ranges.** Plausible in regulated-industry ontologies (ingredient classes, dosage). Candidate for a validation extension type, defined by its owner rather than this spec.
2. **String lengths.** Cheap to add later, same extension.
3. **Property-pair comparisons.** Rare in practice.

Two semantic caveats, one line each in the final spec:

- SHACL validates instance data post-load. v1 fields are descriptive first, enforced via graph schema where possible (principle 3). We map SHACL's constraint vocabulary; we do not adopt its validation model.
- SHACL binds shapes to data via targets (`sh:targetClass`). In v1 the binding is identity: a node entry is the shape for its label. No target machinery.

**Answer to the brief's open question** ("possibly SHACL rules for constraints"): the pragmatic SHACL subset maps natively, the rest rides extensions, nothing forces a shape change. WS1 ships the mapping appendix; transformation logic sits with tooling (importer team), which answers comment [l].

## Decisions needed from the core group

1. **Names.** Spec title ("Neo4j Ontology Specification" proposed), repo name (`neo4j/ontology-spec` proposed), governance group (brief says OLG; OSG was the 8 Sep lean. The artifact is a spec, not a language, which argues OSG; CLG consistency argues OLG).
2. **Importer sign-off** on indexes/tables/mappings becoming extension objects. Their bar stands: the spec must be downloadable in the initial ontology release. The 4.0.0 converter is the migration story for their artifacts.
3. **WS1/WS2 boundary** (comment [k]). The spec must not block on storage decisions and vice versa. The extension mechanism is the decoupler: storage-facing concerns ride extensions until WS2 lands.
4. **Tool type shapes.** `tools` is a core field (decided 2026-09-25); the per-type shapes (`canonicalQuery`, `externalRequest`, ...) are owned by the agent-surface teams, not this spec.
5. **Extensions need no governance from this group.** The spec defines only the envelope; each extension type is defined, versioned and validated by its owner (importer owns `table`/`mapping`, console owns `display`). Unknown types are carried untouched.

## Changelog from initial draft

2026-09-25:
- Extension shape: `extensions` at every level — named keys (`neo4j:`-prefixed) for first-party extensions, the fixed envelope under `custom` for custom ones.
- `enum` → `one_of`.
- Mixed value types: `type: ANY` opts the property out of type constraints.
- Nodes and relationships keyed by local ids rather than the identifier label/type name. New fields: `label` and `labels.identifier` on node types, `type` on relationships. Label renames no longer break references; a relationship type shared across endpoint pairs now sits under one id key per pair.
- `reference` on node types, properties and relationship types: a single informational URI pointing at an external definition. Supersedes the interop/IRI item.
- The list-valued relationship form from 2026-09-14 is gone: multi-pair is expressed by several id keys sharing a `type`, one entry per key.
- Data types are tokens, not objects: `STRING`, `LIST<STRING>`, `LIST<ANY>`, `VECTOR<FLOAT>` with a companion `dimension` field. Reverses the 2026-09-11 type-object change.
- Tools are core: first-class `tools` field on node and relationship entries (`{ type: canonicalQuery | externalRequest | ..., name, description, ... }`), leaving the extensions section.
- `cardinality_type` → `count`, `min_count`, `max_count` on each `from`/`to`. No enum shorthand: `count` is the exact form, `min_count`/`max_count` the ranged form, absent = unconstrained (0..*). Closes the exact-cardinality gap and maps SHACL `sh:minCount`/`sh:maxCount` 1:1.
- Constraint objects can be named: `{ constraint_type, name?, properties }` (field renamed from `kind`); the shorthand flags stay.
- No union types on properties in v1: a single data type or `ANY`.

2026-09-14:
- Multi-pair relationships. A relationship's identity is the triple (type, from, to); the map key alone could not hold the same type between different node pairs, and the transformer note's "expands to multiple relationship entries" contradicted unique map keys. Found by implementation: 65 of 80 relationship entries in the FOAF conversion are same-type, different pairs. The map value is now a list of entries, one per endpoint pair; a bare object is shorthand for a one-entry list. Per-pair `cardinality_type`, `properties`, `constraints`, `extensions`. (Superseded 2026-09-25 by id keys; see above.)

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

**Map keys are local ids.** The 2026-09-14 shape keyed nodes by label and relationships by type, which made the key do double duty: identity for references, and the display/enforcement value. Any rename then broke every reference (mappings, endpoints, storage), and the same relationship type across two node pairs was inexpressible without a list form. With id keys the label lives in `label` / `labels.identifier` and the type in `type`; renames touch one field, references stay stable, and multi-pair needs no special shape. Cost: one extra field per entry, and endpoints now reference the id rather than the label.

**Indexes out of core.** Indexes are physical layout, not meaning. The ontology is about meaning; graph schema information is in scope only as constraint information (the brief's list). Physical hints ride extensions, which is also what keeps WS1 independent of storage decisions.

**Extensions: named first-party, enveloped custom.** (Split adopted 2026-09-25.) First-party extensions (`neo4j:index`, `neo4j:display`, `neo4j-importer:table`/`mapping`, ...) are named keys of `extensions`, each shape owned by its team and shipped in the SDK — types and autocomplete for the common cases. Everything else carries the four-field envelope under `custom`, so unknown types validate structurally and round-trip untouched. The envelope's in-value `type` field keeps custom extensions describable by graph type, which is what WS2 needs to store the ontology in the database layer and expose it to Cypher without special-casing. Self-describing was the requirement; this split keeps it true where it matters.

**The envelope.** Same root schema on every custom extension (`type`, `name`, `definition`), specifics pushed one level down. Buys: envelope-only validation, a fixed meta-model for WS2 storage, and zero governance surface (a new custom extension type is defined by its owner and never touches this spec). Costs one nesting level. Worth it.

**tables/mappings/display as extension types.** The brief calls them adjacent specs composable into single artifacts. With a generic extension mechanism there is no case for three privileged top-level fields. One mechanism, one envelope; the known types are owned outside the spec (importer owns table/mapping, and so on).

**Property flags stay; constraint objects for the rest.** `mustExist`/`unique`/`key` cover the common case in the most readable form (principle 2). Everything else — composites, or anything tooling wants to reference by name — uses the object form: `{ constraint_type, name?, properties }`. Both are one format with a documented relationship, not two formats.

**`one_of`, `pattern`, `aliases`.** `one_of` and `pattern` are JSON Schema's words; the audience already knows them.

**Cardinality lives on the endpoints.** (Decided 2026-09-25.) No `ONE_TO_MANY`-class enum: exactness comes from `count`, ranges from `min_count`/`max_count`, absence means unconstrained. One mechanism, no shorthand to drift from the full form, and SHACL `sh:minCount`/`sh:maxCount` map 1:1. The cost — writing `max_count: 1` where the enum said it in a word — is paid where precision actually lives.

**`tools`, definitions only.** Decided 8 Sep: actions are definitions at annotation level, promotable per the group's call. Promoted 2026-09-25: `tools` is a first-class field on node and relationship entries. The per-type shapes (`canonicalQuery`, `externalRequest`, ...) stay owner-defined; behaviour belongs to runtimes, not to a spec. Definitions compose.

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
