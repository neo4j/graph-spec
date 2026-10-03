# ADR-0003: Schema source of truth

Status: Proposed

## Context

The rebased tree (ADR-0002) holds two artefacts that both claim to be
"the schema":

- **The 4.0.0 flow.** `ontology-graph-spec.schema.json`'s predecessor,
  `go/spec.json`, was *generated* from the Kotlin Multiplatform model:
  `src/jvmMain/kotlin/schema/GenerateGraphModelJsonSchema.kt` runs
  `SerializationClassJsonSchemaGenerator` over
  `GraphModel.serializer().descriptor` and writes the result. Language
  bindings flow the same direction: the TypeScript declarations are
  produced by Kotlin/JS. In this flow the Kotlin model is the source of
  truth; the schema and the TS types are derived artefacts.
- **The v1 proposal flow.** The proposal
  ([`docs/ontology-graph-spec-v1-proposal.md`](../ontology-graph-spec-v1-proposal.md))
  states the deliverable is "a written spec document plus a JSON Schema
  generated from it... the document is the work", and its Appendix B
  records why the 4.0.0 flow failed as a specification: the generated
  schema carries **zero `description` fields**, no prose document
  exists, and the pretty/internal format duality lives only in library
  code. kotlinx-serialization descriptors carry no KDoc, so
  schema-from-Kotlin structurally cannot produce a documented schema
  without an annotation channel.

The first revision of this ADR made the schema hand-maintained with the
implementations conforming to it. That inverted the 4.0.0 arrow but
created two authorities — the hand-written schema and the hand-written
Kotlin model — reconciled only by conformance tests, and it put the
format's day-to-day editing surface in a JSON file while the owning
team's working surface, review culture, and API ownership all live in
the Kotlin model. Every format change landed twice before it could be
checked once.

The decisive new fact: kotlinx-serialization exposes custom
`@SerialInfo` annotations on the descriptor at runtime
(`descriptor.annotations`, `getElementAnnotations(i)`). An annotation
channel (`@SpecDoc`, `@SpecPattern`, `@SpecFormat`, `@SpecMinimum`,
`@SpecMinItems`, `@SpecEnum`, `@SpecRequired`, `@SpecDef`,
`@SpecPropertyNames`) lets the Kotlin model carry every schema
annotation — descriptions included — in the same declaration as the
field it documents. The generator can then *require* `@SpecDoc` on
every structural field and refuse to emit an undocumented schema,
enforcing AGENTS.md non-negotiable 2 ("every schema field carries a
`description`") mechanically rather than by review. This removes the
Appendix B failure mode (zero descriptions) without a second prose
source: the annotation sits on the field, the generated schema is a
pure projection, and the proposal doc remains the human prose.

## Decision

**The annotated Kotlin model is the source of truth;
`ontology-graph-spec.schema.json` is a generated artefact — committed
to the repo and drift-checked, never hand-edited.**

- Schema annotations live on the model as `@SerialInfo` annotations in
  `src/commonMain/kotlin/model/spec/SpecAnnotations.kt`, on the field
  or class they describe. A field without `@SpecDoc` fails generation.
- `src/jvmMain/kotlin/schema/GenerateOntologyGraphSpecJsonSchema.kt`
  walks `GraphModel.serializer().descriptor` and emits the schema;
  the Gradle task `generateOntologyGraphSpecJsonSchema` writes the
  repo-root file. The committed file is the published artefact —
  consumers, the Go schemancer pipeline, and the spec gate all read
  it; none of them run the generator.
- Drift check, mirroring the Go model drift check: CI regenerates the
  schema and fails on `git diff --exit-code`. A model/annotation edit
  without a regenerated schema fails the build.
- The spec gate (ADR-0007) is unchanged: it compiles the committed
  schema against the 2020-12 meta-schema and validates every example
  against it.
- The change pipeline becomes: ADR → prose (proposal doc + changelog)
  → model + annotations → regenerate schema → example →
  `./gradlew jvmTest` green. Prose, schema diff, and example still
  land in the same change; the schema diff is now generated, not
  written.
- Schema features a serializer descriptor cannot express are carried
  on annotations or as named generator overrides, kept explicit and
  few: `@SpecDef` hoists shared `$defs` (`propertyType`, `reference`),
  `@SpecEnum` carries token lists (`constraint_type`), the open
  surfaces (`Tool`, the extensions map) declare
  `additionalProperties: true`, and the custom extension's free-form
  `definition` emits the boolean schema `true`.
- Design rule 8 (one wire format) is unaffected: the single
  serialisation is still specified once — now in the annotated model —
  and every implementation normalises to it internally.

## Alternatives considered

1. **Hand-maintained schema, implementations conform (this ADR's first
   revision).** Rejected: two authorities reconciled only by
   conformance tests; every format change is written twice (schema
   edit + model edit) before it can be checked once. It also moved the
   format's editing surface out of the Kotlin model, which is where
   the owning team's review culture and API ownership live — the
   schema became a second file to learn, review, and keep in sync, and
   the "schema wins" rule settled disagreements by fiat rather than by
   design.
2. **kotlinx-schema-generator (the 4.0.0 generator), unannotated.**
   Rejected: descriptors carry no KDoc and the library has no channel
   for `description`, `pattern`, `format`, or `propertyNames`; its
   output is exactly the undocumented schema Appendix B post-mortems.
   The annotation channel is the whole point; a generator that cannot
   read it is not a generator for this spec.
3. **Schema → types generation for Kotlin too (the Go/schemancer
   arrow).** Rejected: the Kotlin model is the SDK's public API
   (ADR-0006) with editors, `@JsExport` twins, and hand-written
   semantics (InlineExtras catch-alls, custom serializers). Generating
   it from the schema would discard the kept machinery and rewrite the
   port for no fidelity gain — the schema cannot express those
   behaviours anyway.
4. **KDoc/Javadoc extraction at runtime.** Rejected: kotlinx-serialization
   descriptors do not carry KDoc, and there is no Kotlin/Native+JS
   metadata path for it. `@SerialInfo` annotations are the only
   channel that survives to the descriptor on every target.

## Consequences

- The schema is never hand-edited; review attention goes to the model
  annotations, and the generated diff is checked for intent.
- AGENTS.md non-negotiable 2 is enforced by the generator: no
  `@SpecDoc`, no schema. The "description on every field" rule cannot
  silently regress.
- The Go pipeline (`generate-go-models.sh`) is untouched: it consumes
  the committed `ontology-graph-spec.schema.json` as before.
- ADR-0006 (kept machinery), ADR-0007 (spec gate), and ADR-0008
  (converter) are unaffected; ADR-0004's port tracks now treat the
  annotated model as the format's editing surface.
- The risk accepted: the generator is bespoke code that must itself be
  reviewed, and its named overrides (open surfaces, `definition: true`)
  are places where schema shape is decided in generator code rather
  than in annotations. The overrides are enumerated in the generator
  and covered by the spec gate's example validation.
- This revision rewrites the ADR in place before any of it merged; the
  first revision (hand-maintained schema) is preserved in git history
  as the considered-and-rejected alternative 1.

## Later references

- ADR-0009 (docs/adr/0009-ontology-graph-spec-rename.md): renamed the spec "Ontology Spec" → "Ontology Graph Spec"; this ADR's body uses the current name.
