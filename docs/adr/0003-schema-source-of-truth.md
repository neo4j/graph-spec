# ADR-0003: Schema source of truth

Status: Proposed

## Context

The rebased tree (ADR-0002) holds two artefacts that both claim to be
"the schema":

- **The 4.0.0 flow.** `ontology-spec.schema.json`'s predecessor,
  `go/spec.json`, was *generated* from the Kotlin Multiplatform model:
  `src/jvmMain/kotlin/schema/GenerateGraphModelJsonSchema.kt` runs
  `SerializationClassJsonSchemaGenerator` over
  `GraphModel.serializer().descriptor` and writes the result. Language
  bindings flow the same direction: the TypeScript declarations are
  produced by Kotlin/JS and then post-processed by the build-logic
  codegen (`TypeScriptModifierTask` with `script/TypeScriptTypes.kt` /
  `script/TypeScriptUnions.kt`), a textual hack that rewrites generated
  `.d.mts` files — replacing enum-typed fields with string union types
  (`ConstraintTypeJs`, `IndexTypeJs`, `MappingModeJs`, `Neo4jTypeJs`)
  because Kotlin/JS cannot express them (KT-55101). In this flow the
  Kotlin model is the source of truth; the schema and the TS types are
  derived artefacts.
- **The v1 flow.** The proposal
  ([`docs/ontology-spec-v1-proposal.md`](../ontology-spec-v1-proposal.md))
  states the deliverable is "a written spec document plus a JSON Schema
  generated from it... the document is the work", and its Appendix B
  records why the 4.0.0 flow failed as a specification: the generated
  schema carries **zero `description` fields**, no prose document
  exists, and the pretty/internal format duality lives only in library
  code. kotlinx-serialization descriptors carry no KDoc, so
  schema-from-Kotlin structurally cannot produce a documented schema
  without a parallel annotation channel that would itself become a
  second, drift-prone source of prose.

The hand-written `ontology-spec.schema.json` at the repo root already
inverts this: every structural field carries a `description`
(`$schema`, `id`, `version`, `nodes`, `relationships`, `propertyType`,
`endpoint`, `tool`, `extension`, `extensionsMap`, `node.label`, ...),
and AGENTS.md non-negotiable 2 mandates "every schema field carries a
`description`" with prose + schema + example landing in the same change.

The port of the Kotlin/Go/TypeScript SDK to the ontology spec (in scope
since ADR-0002) must now pick a direction. ADR-0002's API-stability
constraint ("existing SDK public APIs stay the same to the largest
extent possible") constrains the *bindings' surface*, not the schema's
provenance. The question: what is the source of truth for
`ontology-spec.schema.json` — hand-maintained with implementations
conforming to it, or generated from a Kotlin v1 model as in 4.0.0?

ADR-0006 (Accepted) has since sharpened ADR-0002's API-stability
constraint into an architecture principle: the port keeps main's
machinery — the Kotlin Multiplatform model, the kotlinx-serialization
codecs, the `@JsExport` JS/TS surface, the Go schemancer codegen
pipeline, and the validator framework — re-targeted at the v1 schema,
with consumer-visible changes limited to the name and the extensions
area. That principle governs *how* the port is built, not where the
schema's authority lives, so the question above stands as posed.

## Decision

**The schema stays hand-maintained, co-equal with the written spec
document, and the Kotlin/Go/TypeScript implementations conform to it.**
(Option A, with schema→types generation as the sanctioned derivation.)

- **This recommendation is unchanged under ADR-0006's architecture
  principle.** Hand-maintained schema plus conforming implementations
  is compatible with keeping main's machinery: the machinery is
  re-targeted at the v1 schema, not discarded, and every re-targeted
  implementation conforms to the hand-maintained schema rather than
  generating it. ADR-0006 records this reading itself, citing
  "ADR-0003's rule that the schema is hand-maintained and the
  implementations conform to it" as the basis for the re-targeting.
- `ontology-spec.schema.json` is edited by hand, in the same change as
  its prose and at least one validating example (AGENTS.md
  non-negotiables 2 and 4). It is never emitted by a generator from an
  implementation model.
- Implementations **conform**: the Kotlin model, Go bindings, and
  TypeScript types are validated against the schema in tests (fixture
  documents validated by `scripts/validate.mjs` on the schema side;
  round-trip/serialisation tests on the SDK side proving the bindings
  read and write exactly the schema's shape). A schema/bindings
  mismatch fails CI; the schema wins.
- The derivation arrow, where generation is wanted at all, is
  **schema → language types**, never model → schema. Generating
  TypeScript interfaces (and optionally Go structs / Kotlin
  serialisable classes) from `ontology-spec.schema.json` is permitted
  as a convenience; the generated files are checked in and diff-checked
  in CI, and the schema remains the input. This is also the arrow the
  kept machinery runs under ADR-0006: the Go schemancer codegen
  pipeline is re-targeted at `ontology-spec.schema.json`, so
  schema → types is the port's native direction, not an added step.
- **build-logic's TypeScript codegen is retired for the v1 surface
  only; the machinery itself is kept per ADR-0006.**
  `TypeScriptModifierTask` / `TypeScriptTypes` / `TypeScriptUnions`
  exist to repair Kotlin/JS declaration output (enum → string-union
  rewriting, `LIST_` → `LIST<...>` token renaming). The v1 surface
  gives the rewriter nothing to repair: v1 type tokens
  (`LIST<STRING>`, `VECTOR<FLOAT>`) are schema `pattern`-validated
  strings, not enums, so the Kotlin/JS declarations for the v1 model
  already carry the correct types and the enum → string-union rewrite
  has no input there. The Kotlin/JS → TypeScript declaration pipeline
  itself is kept machinery under ADR-0006 — v1 TypeScript declarations
  flow from the re-targeted `@JsExport` surface exactly as 4.0.0's do,
  minus the repair step — so retiring the rewrite changes no
  consumer-visible surface: consumers see the published `.d.mts`
  types, not the build logic that produced them. The task and its
  plugin remain in the tree serving the 4.0.0 build during the
  coexistence period (ADR-0002) and are removed only when the 4.0.0
  build goes; no v1 code path may depend on them.
- `src/jvmMain/kotlin/schema/GenerateGraphModelJsonSchema.kt` and the
  `kotlinx-schema-generator` dependency are not ported to v1. The
  4.0.0 generator keeps working for the frozen 4.0.0 artefacts only.
- Design rule 8 (one wire format) is unaffected and reinforced: the
  single serialisation is specified once, in prose + schema, and every
  implementation normalises to it internally.

## Alternatives considered

1. **Generate the schema from a Kotlin v1 model (the 4.0.0 flow,
   Option B).** The Kotlin model would be the source of truth;
   `GenerateGraphModelJsonSchema.kt`'s successor would emit
   `ontology-spec.schema.json`, and Go/TS would follow the model as
   they do today. Rejected: this is exactly the pipeline Appendix B
   documents as having produced an undocumented spec — zero
   `description` fields, no prose, format rules observable only in
   library code. kotlinx-serialization descriptors do not carry
   documentation, so descriptions would need a parallel annotation
   channel that becomes a second source of prose drifting from both
   code and schema. It also inverts the proposal's written-spec-first
   stance and AGENTS.md non-negotiable 2 ("the deliverable is the
   written spec... never the other way around"), and it makes the
   format hostage to what one implementation's serializer can express
   (the `TypeScriptModifierTask` hack is the visible scar of that
   coupling).
2. **Dual maintenance: hand-written schema and hand-written Kotlin
   model, no conformance tests.** Rejected: without tests pinning the
   bindings to the schema, drift is guaranteed — Appendix B's
   pretty/internal duality and undocumented `implied`/`optional`
   semantics are the observed cost of exactly this arrangement. The
   conformance tests are the mechanism that makes a hand-maintained
   schema safe; dropping them keeps the cost of two sources and loses
   the benefit of either.
3. **Full hybrid: hand-maintained schema, but generate the schema's
   *types* from the Kotlin model and diff-check them against the
   schema.** Rejected: it keeps the Kotlin model as a second, equal
   authority for shape, so every format change is implemented twice
   before it can be checked once, and disagreements have no principled
   resolution. Schema → types generation (permitted in the Decision)
   captures the same convenience with a single arrow of authority.
4. **Keep the build-logic TS codegen and point it at v1 Kotlin
   declarations.** Rejected: the codegen is a workaround for KT-55101
   (Kotlin/JS cannot emit string union types); v1's type tokens
   (`LIST<STRING>`, `VECTOR<FLOAT>`) are schema `pattern`-validated
   strings, not enums, so there is nothing for the union-rewriter to
   fix. Carrying a textual patch step into v1 would re-couple the
   published types to Kotlin/JS declaration quirks for no benefit.
   This rejects the rewrite step only; the Kotlin/JS → TypeScript
   declaration pipeline it post-processes is kept machinery per
   ADR-0006.

## Consequences

- Format changes follow the AGENTS.md pipeline unchanged: ADR → prose →
  hand-edited schema → example → `npm run validate`. No generator step
  is inserted into that path.
- The SDK port tracks (charted per ADR-0002 after this ADR lands) add
  conformance tests to their CI: Kotlin/Go/TS fixtures must validate
  against `ontology-spec.schema.json`, and serialisation round-trips
  must reproduce schema-valid documents. API stability (ADR-0002) is
  preserved at the binding-signature level; the wire shape is dictated
  by the schema.
- build-logic's `TypeScriptModifierTask`, `TypeScriptModifierPlugin`,
  and `script/TypeScript{Types,Unions}.kt` have no v1 consumer. They
  stay only as long as the 4.0.0 build does and are deleted with it —
  an internal build-logic change with no consumer-visible effect. The
  v1 TypeScript declarations come from the Kotlin/JS `@JsExport`
  surface kept per ADR-0006, with schema → types generation remaining
  a permitted convenience (per the Decision); neither path passes
  through the retired rewrite.
- `GenerateGraphModelJsonSchema.kt` is 4.0.0-only; nothing in v1 may
  regenerate `ontology-spec.schema.json`.
- The risk accepted: the hand-maintained schema can drift from the
  *implementations* (the schema cannot drift from the prose — they land
  in the same change by rule). The conformance tests are the mitigation
  and are mandatory, not optional.
- ADR-0004 (port sequencing) and ADR-0005 (extension packaging) assume
  this decision: port tracks implement *against* the schema, and
  extension type owners ship their own schemas under the same
  hand-maintained rule. ADR-0006 (port architecture principle) builds
  on it directly: the kept machinery is re-targeted at, and conforms
  to, the hand-maintained schema.
