# ADR-0008: The 4.0.0 → 1.0.0 converter mapping

Status: Proposed

## Context

Port track 3 of ADR-0004 builds the one-way 4.0.0 → 1.0.0 converter,
replacing the legacy `migrate/migration/dataModel/` chain (deleted in
this track). Tracks 1–2 have landed: the v1 model
(`src/commonMain/kotlin/model/`, `GraphModel` with `$schema`/`id`/
`version`), the re-pointed codecs (`codec/schema/SchemaMap` unchanged,
`ExtensionValueSerializer` now an untagged raw-JSON codec), and the v1
validators. Per the repo contract (ADR first) and ADR-0004's track 3
section — "decode the 4.0.0 document to a `SchemaMap`, transform
map → map, encode as v1; it never needs the 4.0.0 model classes" — this
ADR decides the complete mapping table before implementation.

Inputs and evidence:

- **The 4.0.0 input shape.** `go/spec.json` (generated from the 4.0.0
  Kotlin model) and the deleted model on `main`
  (`model/property/Neo4jType.kt`: 38 tokens;
  `model/relationship/Relationship.kt` + `RelationshipTarget.kt`;
  `model/node/NodeConstraint.kt`; `model/type/ConstraintType.kt`;
  `model/Internal.kt` / `Pretty.kt` / `Rename.kt`: the pretty/internal
  duality; `model/extension/ExtensionValueSerializer.kt`: the tagged
  extension wire form).
- **The v1 output shape.** `ontology-spec.schema.json` and the format
  semantics in `docs/ontology-spec-v1-proposal.md` ("The converter maps
  4.0.0's top-level `version` to `$schema`"; endpoint cardinality;
  constraint flags vs objects; the extension placement rule).
- **The test corpus.** `src/jvmTest/resources/migrate/migration/dataModel/`:
  `prod-like/*.yaml` are 4.0.0 **pretty** form (nodes keyed by label,
  properties keyed by name, shorthand flags, e.g. `northwind.yaml`);
  `internal/*.yaml` are 4.0.0 **internal** form (nodes keyed `node0`…
  with `labels.identifier` set and the original key in `name`,
  properties keyed `nodeProperty0`… with their name in `name`,
  constraints longhand with deterministic names like
  `unique_categories_categoryid`); `prod-like/*.json` and
  `graph-data-model-3.0.0.json` are **3.0.0** documents — inputs to the
  legacy 3.0 → 4.0.0 migration, not to this converter.
- **A fact that shapes the table: 4.0.0 has no cardinality.** The
  MANY_TO_ONE-era cardinality enum died with data model 3.0; the 3.0 →
  4.0.0 migration did not carry it. `RelationshipTarget` on `main` is
  `{node, label}` and `git grep -i cardinality main` finds zero hits.
  The proposal's "was: cardinality MANY_TO_ONE" comment refers to the
  v1 draft's own 2026-09-14 `cardinality_type`, not to anything in
  4.0.0. There is no 4.0.0 cardinality enum to map.
- **The bridge.** `src/bridge/kotlin/bridge/Migration.kt` exports
  `@CName("migrate")`; its only caller is `go/internal/bridge` (a Go
  `internal` package — not consumer-visible), behind the Go
  `migration` package's `ToGraphSpec`/`FromGraphSpec`.

## Decision

The converter is one `Migration` over the existing SchemaMap machinery:
decode (JSON or YAML, both are 4.0.0 wire forms) → normalise → map →
emit v1. Every rule below is a map→map transform; no 4.0.0 model class
is referenced.

### 0. Input acceptance

- Input type token `graph_spec`, input version `4.0.0`; the existing
  `MigrationPath.version()` truncation accepts any `4.0.x` patch.
  `Type` regains `const val GRAPH_SPEC = "graph_spec"` and `Version`
  gains `const val GRAPH_SPEC_V4 = "4.0.0"` as the converter's input
  contract (both deleted with the 4.0.0 model in track 1; restored
  here as converter input tokens, not as a format this SDK writes).
- Anything else — data model 2.3/2.4/3.0 (flat or wrapped), import_spec
  1.0.0, or a missing `version` — is rejected with an "unsupported
  input; migrate to graph spec 4.0.0 first" error. See "Out of scope"
  below.
- JSON and YAML inputs both work: the codec decodes either to a
  `SchemaMap` (the corpus has both).

### 1. Root

| 4.0.0 | v1 | rule |
|---|---|---|
| `version: "4.0.0"` | `$schema: "https://neo4j.com/ontology-spec/1.0.0/schema.json"` | Constant: the schema's `$id`, as used by every v1 example. The 4.0.0 format version becomes the v1 spec link (proposal: "the converter maps 4.0.0's top-level `version` to `$schema`"). It is **not** carried into v1 `version`. |
| — (4.0.0 has no id) | `id` | **Random UUID v4 per conversion** (`kotlin.uuid.Uuid`, Kotlin 2.3.20 stdlib). Re-converting the same input yields a new id; the converter is run once per artifact and the output is the artifact from then on. |
| — | `version: 1` | The ontology's own Int identity starts at **1**. 4.0.0's `version` is the format version, never the ontology's, so nothing is carried. |
| `name` | `name` | Carried verbatim when present. |
| `description` | `description` | Carried when non-empty; 4.0.0's default `""` is omitted (absent means none). |
| `nodes` / `relationships` | `nodes` / `relationships` | Mapped per §4–§7. |
| `tables` / `mappings` / `display` | top-level `extensions` | Mapped per §8 (placement rule: they describe several elements or none, so they live at the top level). |
| — (4.0.0 has no root extensions) | — | Root `extensions` is emitted only when §8/§9 produce content. |

### 2. Identity normalisation (both 4.0.0 input forms, one rule)

4.0.0 files in the wild use two serialisations of the same model:
**pretty** (map keys are labels/type/property names) and **internal**
(map keys are `node0`/`nodeProperty0`/…, the readable name in `name`,
shorthand flags expanded to longhand constraints). `Internal.kt` and
`Pretty.kt` are deleted with the 4.0.0 model; the converter inlines a
map→map equivalent of the *prettify* direction — it never resurrects
those files, and it never detects which form it is reading. One rule
covers both forms and hand-written hybrids:

- **The v1 id of a node, relationship, or property is its `name` field
  if present, else its map key.** Pretty form: `name` is absent, the
  key is the id. Internal form: `name` holds the original pretty key,
  which becomes the id. The internal `node0`-style keys are discarded.
- The converter first builds the document's **id maps** (old key → v1
  id) for nodes, relationships, and per-element properties, then
  rewrites every reference through them: endpoint `node` references
  (pretty: label keys; internal: `node0` ids — both resolve),
  `display.nodes` keys, mapping `node`/`relationship`/`from.node`/
  `to.node` references, mapping `properties`/`key` property references,
  and constraint/index `properties` lists.
- A collision after resolution (two elements resolving to the same v1
  id) is a hard error, not a silent merge.
- `name` fields on nodes, relationships, and properties are consumed by
  this rule and never emitted: v1 has no `name` on those entries.

### 3. Neo4jType → v1 type tokens

All 38 tokens of the 4.0.0 `Neo4jType` enum
(`main:src/commonMain/kotlin/model/property/Neo4jType.kt`), mapped
against the v1 `propertyType` pattern
(`ontology-spec.schema.json` `$defs.propertyType`: scalars `STRING
INTEGER FLOAT BOOLEAN DATE TIME LOCALTIME DATETIME LOCALDATETIME
DURATION POINT BYTES`, plus `ANY`, `LIST<ANY>`, `LIST<scalar>`,
`VECTOR<scalar>`). **Lossy** rows widen the type; the dropped
information is stated. `dimension` on a VECTOR property carries
unchanged.

| 4.0.0 token | v1 token | verdict |
|---|---|---|
| `ANY` | `ANY` | lossless |
| `BOOLEAN` | `BOOLEAN` | lossless |
| `LIST<BOOLEAN>` | `LIST<BOOLEAN>` | lossless |
| `DATE` | `DATE` | lossless |
| `LIST<DATE>` | `LIST<DATE>` | lossless |
| `DURATION` | `DURATION` | lossless |
| `LIST<DURATION>` | `LIST<DURATION>` | lossless |
| `FLOAT32` | `FLOAT` | **lossy**: 32-bit precision class dropped (widened to 64-bit FLOAT) |
| `LIST<FLOAT32>` | `LIST<FLOAT>` | **lossy**: same widening |
| `FLOAT` | `FLOAT` | lossless |
| `LIST<FLOAT>` | `LIST<FLOAT>` | lossless |
| `INTEGER8` | `INTEGER` | **lossy**: 8-bit width dropped (widened) |
| `LIST<INTEGER8>` | `LIST<INTEGER>` | **lossy**: same widening |
| `INTEGER16` | `INTEGER` | **lossy**: 16-bit width dropped (widened) |
| `LIST<INTEGER16>` | `LIST<INTEGER>` | **lossy**: same widening |
| `INTEGER32` | `INTEGER` | **lossy**: 32-bit width dropped (widened) |
| `LIST<INTEGER32>` | `LIST<INTEGER>` | **lossy**: same widening |
| `INTEGER` | `INTEGER` | lossless |
| `LIST<INTEGER>` | `LIST<INTEGER>` | lossless |
| `LOCAL DATETIME` | `LOCALDATETIME` | lossless rename (v1 token drops the space) |
| `LIST<LOCAL DATETIME>` | `LIST<LOCALDATETIME>` | lossless rename |
| `LOCAL TIME` | `LOCALTIME` | lossless rename |
| `LIST<LOCAL TIME>` | `LIST<LOCALTIME>` | lossless rename |
| `POINT` | `POINT` | lossless |
| `LIST<POINT>` | `LIST<POINT>` | lossless |
| `STRING` | `STRING` | lossless |
| `LIST<STRING>` | `LIST<STRING>` | lossless |
| `VECTOR<FLOAT>` | `VECTOR<FLOAT>` | lossless |
| `VECTOR<FLOAT32>` | `VECTOR<FLOAT>` | **lossy**: element precision class dropped |
| `VECTOR<INTEGER>` | `VECTOR<INTEGER>` | lossless |
| `VECTOR<INTEGER32>` | `VECTOR<INTEGER>` | **lossy**: element width dropped |
| `VECTOR<INTEGER16>` | `VECTOR<INTEGER>` | **lossy**: element width dropped |
| `VECTOR<INTEGER8>` | `VECTOR<INTEGER>` | **lossy**: element width dropped |
| `ZONED DATETIME` | `DATETIME` | lossless rename (v1 `DATETIME` is the zoned form) |
| `LIST<ZONED DATETIME>` | `LIST<DATETIME>` | lossless rename |
| `ZONED TIME` | `TIME` | lossless rename (v1 `TIME` is the zoned form) |
| `LIST<ZONED TIME>` | `LIST<TIME>` | lossless rename |
| `UUID` | `STRING` | **lossy**: uuid-ness dropped; widened to `STRING`. The converter does not invent a `pattern` regex — descriptive constraints are the author's call, not the converter's. |

The same table applies everywhere a 4.0.0 Neo4jType token appears,
including inside table payloads (`columns.*.suggested` / `.supported`,
§8): a v1 document must not carry 4.0.0-era type tokens. v1 tokens with
no 4.0.0 source (`BYTES`, `LIST<BYTES>`, `LIST<ANY>`, `VECTOR<scalar>`
for non-numeric scalars) are never emitted by the converter.

### 4. Nodes

| 4.0.0 | v1 | rule |
|---|---|---|
| map key + `name` | map key | §2 id rule. |
| `label` / `labels.identifier` | `label` (shorthand) or `labels` | v1 label = `labels.identifier` ?: `label` ?: the resolved id (the pretty-form key *is* the label by 4.0.0 convention). Shorthand `label` when no `implied`/`optional`; full `labels` object otherwise, with `implied`/`optional` carried. |
| `labels.implied` / `labels.optional` | `labels.implied` / `labels.optional` | Carried verbatim. |
| `description` | `description` | Carried when non-empty. |
| `properties` | `properties` | Keys per §2; `type` per §3; `dimension` carried; `mustExist`/`unique`/`key` flags carried as flags; `name` consumed; `description` carried when non-empty; `extensions` per §9. |
| `constraints` (map) | `constraints` (list) + property flags | §6. |
| `indexes` (map) | `extensions."neo4j:index"` | §8. |
| `extensions` | `extensions` | §9. |

### 5. Relationships and cardinality

| 4.0.0 | v1 | rule |
|---|---|---|
| map key + `name` | map key | §2 id rule. |
| `type` | `type` | Carried verbatim. |
| `from` / `to` (`RelationshipTarget`) | `from` / `to` endpoints | `node` rewritten through the node id map (§2). **`label` is dropped**: v1 endpoints reference node ids and the label is reachable through the referenced node — carrying it would duplicate the node entry's truth. |
| — (no cardinality in 4.0.0) | — | **There is no 4.0.0 cardinality enum to map** (Context). Every endpoint is emitted as `{ node: <id> }` with no `count`/`min_count`/`max_count`: absent means unconstrained (0..*), which is exactly 4.0.0's semantics. Nothing is synthesised. |
| `properties`, `constraints`, `indexes`, `extensions`, `description` | as Node | §4 rules; relationship indexes also become `neo4j:index` payloads on the relationship entry. |

### 6. Constraints

4.0.0: map-keyed `NodeConstraint`/`RelationshipConstraint`
`{ type: EXISTS|KEY|PROPERTY_TYPE|UNIQUE, label?, properties[],
name? }`. v1: a `constraints` list of
`{ constraint_type: key|unique|mustExist, name?, properties[] }` plus
the property shorthand flags.

| 4.0.0 `type` | v1 | rule |
|---|---|---|
| `EXISTS` | `mustExist` | flag or object per the rule below |
| `KEY` | `key` | flag or object per the rule below |
| `UNIQUE` | `unique` | flag or object per the rule below |
| `PROPERTY_TYPE` | — | **Dropped**: redundant with the property's `type` (4.0.0 emitted one per typed property on internalisation; v1's type token already says it). |
| `label` (NodeConstraint) | — | **Dropped**: the constraint lives on the node entry; v1 endpoints and elements are identified by id, not label. |
| `name` | `name` | Carried when it is human-given (see the deterministic-name rule); dropped when machine-generated. |
| map key | — | Dropped (v1 constraints are a list; identity is `name` when present). |

**Flags vs objects — the emission rule.** A constraint becomes a
property shorthand flag (`mustExist`/`unique`/`key: true` on the
referenced property) if and only if **all** of:

1. it constrains exactly **one property**, and
2. it is **unnamed or its name is the 4.0.0 machine-generated
   deterministic id** — recomputed as
   `<type-lowercase>_<owner-resolved-id>_<property-resolved-id>`
   (`Internal.kt`'s `deterministicId`, e.g.
   `unique_categories_categoryid`); such names carry no human meaning
   and are treated as absent, and
3. the referenced property exists on the owning element.

Everything else — composites (2+ properties), and single-property
constraints with a human-given name — is emitted as a constraint
object, preserving input map order in the `constraints` list. A flag
and an object never duplicate the same constraint: when both forms
describe it (possible in hand-written 4.0.0), the flag is set and the
object is emitted only if rule 2 keeps a name; otherwise the forms
merge into the flag. This rule makes pretty and internal serialisations
of the same 4.0.0 model converge to identical v1 output: internal
form's longhand, deterministically-named single-property constraints
collapse back to the flags they came from.

### 7. Property fields

`type` per §3; `dimension` carried (VECTOR companion); flags per §6;
`description` carried when non-empty; `name` consumed by §2. 4.0.0 has
no `one_of`, `pattern`, `aliases`, or `reference` on any element — the
converter never emits them.

### 8. tables / mappings / display / indexes → named extension payloads

Per the placement rule these live at the top level (tables, mappings,
display) or on their element (indexes). The converter emits **raw
payload maps** (ADR-0005's owner subpackages and typed shapes land
later; payloads are never validated by the spec). Multi-instance
extensions are always a **list** of payload objects under the named key
— one wire form, never "object for one, list for many" (design rule 8).

| 4.0.0 | v1 key (top-level `extensions` unless noted) | payload |
|---|---|---|
| `tables` (map) | `neo4j-importer:table` | **List** of table payloads: `{ name: <4.0.0 map key>, source, columns, primaryKeys?, foreignKeys? }`, fields carried verbatim except: `columns.*.suggested`/`.supported` remapped through the §3 token table; nested tagged `extensions` (table, column, foreignKey, reference) untagged per §9 and carried inside the payload. |
| `mappings` (list) | `neo4j-importer:mapping` | **List** of mapping payloads with a `kind` field: `kind: "node"` (`node`, `table`, `properties`, `mode?`, `matchLabel?`, `key?`), `kind: "relationship"` (`relationship`, `table`, `from`, `to`, `properties?`, `mode?`, `matchLabel?`, `key?`), `kind: "query"` (`table`, `query`). Kind is detected **structurally** — `node` present → node, `relationship` present → relationship, `query` present → query — because the 4.0.0 wire does not reliably carry the `type` discriminator (the corpus fixtures omit it). All node/relationship/property references (`node`, `relationship`, `from.node`, `to.node`, `properties` keys, `key` lists) are rewritten through the §2 id maps; `TargetMapping.label` is carried verbatim (source-matching info, not a node reference). |
| `display` | `neo4j:display` | **Single object** `{ nodes: { <v1-node-id>: { x, y, extensions?-untagged } } }` — one canvas per document, keys rewritten through the node id map. |
| `indexes` (on nodes and relationships) | `neo4j:index` **on the owning element's** `extensions` | **List** of index payloads: `{ type: RANGE|TEXT|POINT|FULLTEXT|VECTOR|LOOKUP, name?, labels?, properties, options? }`; `properties` (and `labels` where present) rewritten through the §2 id maps; `options` and nested tagged `extensions` untagged per §9 and carried inside the payload. Indexes are extension objects in v1 (proposal: "indexes as core fields (they are extension objects instead)"). |

### 9. Extensions: untag, then named-vs-custom

4.0.0's wire form is the tagged tree (`ExtensionValueSerializer` on
`main` dispatches on the kind key): `{"String": {"value": X}}`,
`{"Boolean": …}`, `{"Long": …}`, `{"Double": …}`, `{"List": {"value":
[…]}}`, `{"Map": {"value": {…}}}`. v1 payloads are untagged raw JSON.
The converter **untags recursively**: `String`/`Boolean`/`Long`/
`Double` → the scalar, `List` → an array (elements untagged), `Map` →
an object (values untagged). (The `go/spec.json` rendering of this as
`{type, value}` is the generator's approximation of the polymorphic
descriptor; the kind-key form is the real wire form.)

The key rule, per element `extensions` maps, per ADR-0005 ("the
4.0.0 → 1.0.0 converter moves any colliding 4.0.0 payloads"; unknown
content carried untouched, never rejected):

- Keys under a **declared owner prefix** (`neo4j:`, `neo4j-importer:`)
  are carried as **named keys** with the untagged payload untouched.
  The converter does not shape-check them (the owner modules land
  later).
- **Every other key** is wrapped in the **custom envelope**:
  `custom: [ { type: <original key>, definition: <untagged payload> } ]`.
  `name`/`$schema` are not synthesised. Multiple custom keys append to
  the one `custom` list, input order preserved.
- 4.0.0 `extensions` on sub-structures with no v1 extensions home —
  `labels` and constraint objects (v1 `labels` and `constraint` are
  closed shapes) — **hoist to the nearest enclosing element's**
  `extensions`, through the same named-vs-custom rule. Nothing is
  dropped.

### 10. Out of scope: pre-4.0.0 inputs

Data model 2.3/2.4/3.0 (flat and wrapped) and import_spec 1.0.0 are
**rejected**. Reason: the 4.0.0 tooling already migrates those to
4.0.0 — the legacy chain
(`migrate/migration/dataModel/DataModelV2V3Migration`,
`DataModelV3GraphSpecMigration`, `GraphSpecDataModelV3Migration`)
lives in the published graph-spec 4.x artifacts and in the frozen
`graph-spec-4.0.0` tag (ADR-0001). One-way means one-way: holders of a
3.0.0 document run the 4.0.0 library's 3.0 → 4.0.0 migration, then this
converter. The v1 SDK does not carry the legacy chain — ADR-0004
deletes `migrate/migration/dataModel/` in this track — and the 3.0.0
fixtures in the corpus (`prod-like/*.json`,
`graph-data-model-3.0.0.json`) remain only as fixtures of that frozen
history, not as converter inputs.

### 11. Converter home and the bridge export

- **Home: the existing `migrate` package**, as one `Migration`
  implementation —
  `src/commonMain/kotlin/migrate/migration/graphSpec/GraphSpecV4OntologySpecMigration.kt`
  (`fromType = Type.GRAPH_SPEC`, `from = Version.GRAPH_SPEC_V4`,
  `toType = Type.ONTOLOGY_SPEC`, `to = Version.LATEST`), with its
  normaliser and the §3 token table as helpers in the same folder
  (mirroring the deleted `migrate/migration/dataModel/` layout).
  `MigrationPath`, the `Migration` abstract class, the
  `OntologySpecConfig.migrations` wiring, and the bridge are unchanged
  machinery (ADR-0006); `defaultConfig` registers the converter as the
  single default migration, so
  `OntologySpec.Json.decodeFromString(doc, Type.GRAPH_SPEC)` works with
  no API change. The legacy `migrate/migration/dataModel/` chain and
  `DataModelUtils` are deleted in the same track (ADR-0004).
- **Bridge: keep `@CName("migrate")`** on
  `src/bridge/kotlin/bridge/Migration.kt`, re-pointed at the same
  `MigrationPath` (now holding only this converter). ADR-0006
  reasoning: the consumer-visible surfaces are the Kotlin, JS, and Go
  APIs, and the C symbol appears in none of them — Go's only caller is
  `go/internal/bridge`, an `internal` package, behind the `migration`
  package whose `ToGraphSpec`/`FromGraphSpec` names track 5 re-points
  per ADR-0004's naming rules. The symbol is visible only to someone
  linking `libontologymodel` directly, and for them `migrate` remains
  the accurate verb — the bridge migrates documents between format
  versions along the path. Renaming would be a consumer-visible delta
  beyond the two ADR-0006 allows (name, extensions) with zero API
  benefit.

### Testing

The corpus is the §Context set: every `prod-like/*.yaml` (pretty) and
`internal/*.yaml` (internal) pair converts to the **same** v1 document
modulo the generated `id` — the convergence property of §2/§6 is the
primary assertion — plus focused fixtures for each lossy §3 row, the
§6 flag/object rule, §8 payloads, and §9 untagging. Converted output
validates against `ontology-spec.schema.json` (the ADR-0007 gate).

## Alternatives considered

1. **Content-derived `id`** (hash of the canonical input) instead of a
   random UUID. Rejected: it conflates identity with content — every
   later edit to the converted document would either change the id
   (defeating "identifying the spec when deployed") or leave it stale
   (a lie). `id` is identity, not integrity; a fresh UUID per
   conversion is the least-surprise semantic for a one-way converter.
2. **Carry 4.0.0's `version` into v1 `version`** (parse `"4.0.0"` → 4).
   Rejected: 4.0.0's `version` is the *format* version; v1's is the
   *ontology's own* version. Carrying it would assert a version lineage
   (1–3) the ontology never had, and the proposal explicitly reserves
   plain `version` for the ontology.
3. **Reject documents containing lossy type tokens** instead of
   widening. Rejected: the converter must convert every valid 4.0.0
   document; widening (FLOAT32→FLOAT, INTEGER8/16/32→INTEGER,
   UUID→STRING) preserves data readability — every stored value of the
   4.0.0 type is a valid value of the v1 type — while rejection would
   strand exactly the artifacts the converter exists to save.
4. **Preserve the lost precision/uuid information in custom
   extensions** (e.g. `custom: [{type: "graph-spec-4:type",
   definition: {token: "FLOAT32"}}]` on every widened property).
   Rejected: noise on a large fraction of properties for information
   whose keeper is the source document — the conversion is one-way and
   the 4.0.0 file remains the record; v1 deliberately dropped the width
   tokens (proposal type-token decision), and the converter is not a
   mechanism for smuggling them back.
5. **Emit all constraints as objects, never flags.** Rejected: the
   flags are the format's readable surface (proposal: "The readable
   surface"); a converter that never uses them produces v1 documents no
   author would write, and pretty↔internal convergence (§6) becomes
   impossible because internal form's deterministically-named longhand
   constraints would stay objects.
6. **Emit flags even for human-named single-property constraints,
   dropping the name.** Rejected: constraint names exist so tooling can
   reference individual constraints (proposal, 2026-09-25); dropping a
   human-given name loses information the format can carry, violating
   "carried, never rejected" in spirit.
7. **Detect pretty vs internal form explicitly** (sniff `node0`-style
   keys or the presence of `name`) and run two code paths. Rejected:
   two paths for one semantics, and the sniff is guesswork — a
   hand-written pretty document may legitimately use `node0` as a key.
   The §2 name-or-key rule handles both forms and hybrids uniformly
   with no detection step.
8. **Single object when a named extension has one instance, list when
   many.** Rejected: a "one means object, many means list" duality is
   exactly the two-wire-form failure design rule 8 forbids; the
   converter always emits a list for `neo4j-importer:table`,
   `neo4j-importer:mapping`, and `neo4j:index`.
9. **Keep the legacy `dataModel` migration chain alongside the
   converter.** Rejected: ADR-0004 deletes the chain in this track; a
   v1 SDK that still migrates 2.x/3.0 documents keeps the old formats
   alive past the clean break, and the composed path (4.0.0 tooling,
   then this converter) already covers those inputs.
10. **A new `convert/` package for the converter.** Rejected:
    `MigrationPath` + `Migration` + the `OntologySpecConfig.migrations`
    wiring + the bridge are precisely the machinery ADR-0006 says to
    keep, and the converter *is* a `Migration` (SchemaMap → SchemaMap,
    exactly the pattern `MigrationPath` already runs); a new package
    forks the architecture for a naming preference and re-touches every
    consumer-visible wiring point for no gain.
11. **Rename the bridge export** (`migrate` → `convert`). Rejected:
    the symbol is internal to the Go module (`go/internal/bridge`) and
    appears in no consumer-visible API; renaming breaks direct
    native-lib linkers — the only party that can see it — for zero
    clarity, and ADR-0006 limits consumer-visible deltas to the name
    and the extensions area.
12. **Carry table `suggested`/`supported` tokens verbatim** inside the
    `neo4j-importer:table` payload. Rejected: it leaks the 4.0.0 type
    system into v1 artifacts — the payload would hold tokens
    (`FLOAT32`, `ZONED DATETIME`) that no v1 consumer can interpret
    against the v1 token set — and remapping them is the same §3 table
    the converter already applies, one line of code, no guessing at the
    owner's final shape (the owner defines the envelope's fields; the
    token vocabulary is the spec's).

## Consequences

- Track 3 implements this table as decided: one `Migration` in
  `migrate/migration/graphSpec/`, the legacy `dataModel` chain deleted,
  `defaultConfig` wiring the converter, the bridge re-pointed with
  `@CName("migrate")` unchanged.
- The mapping is one-way and partly lossy by design (§3 width/precision
  tokens, UUID; PROPERTY_TYPE constraints; endpoint `label`; internal
  `node0`-style keys). Every lossy row widens; no valid 4.0.0 document
  is rejected for its types.
- Pretty and internal serialisations of the same 4.0.0 model convert to
  the same v1 document (modulo the generated `id`); the corpus pairs
  (`prod-like/*.yaml` vs `internal/*.yaml`) assert this in tests.
- Converted documents validate against `ontology-spec.schema.json`;
  conversion fixtures join the ADR-0007 spec gate.
- Pre-4.0.0 holders have a documented two-step path: graph-spec 4.x
  tooling (frozen `graph-spec-4.0.0` tag) to 4.0.0, then this
  converter.
- No format artefact changes: the wire shape, the schema, the prose,
  and the examples are untouched — this ADR governs converter behaviour
  only, so non-negotiables 2 and 4 require no proposal changelog entry,
  schema edit, or new example (same class of decision as ADR-0005).
- ADR-0004's track 3 section ("decode to SchemaMap, map→map, encode as
  v1", legacy chain deleted, no 4.0.0 model class survives) is
  implemented as written; this ADR fills in the mapping table it
  deferred.

## Later references

- ADR-0009 (docs/adr/0009-ontology-graph-spec-rename.md): renamed the spec "Ontology Spec" → "Ontology Graph Spec"; this ADR's body keeps the historical name.
