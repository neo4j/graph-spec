# ADR-0014: The spec-link field is named schema

Status: Proposed

## Context

The v1 spec-link field is `$schema` today. It arrived with the proposal
changelog entry of 2026-09-10: "Deleted top-level `$version` and
extension-level `$version`, replaced both with `$schema`, which links to
the schema in a specific version. `$version` alone is not enough; now
following JSON Schema's convention. `$schema` also answers the
document-type ask (no sniffing structure to guess document kind)."
Appendix A of the proposal argues the `$` sigil convention at length:
`$`-prefixed fields are meta, JSON Schema's `$schema` and JSON-LD's
`@context` are the precedents, and the sigil generalises across the
format — "one rule, `$schema` points at whatever governs the object,"
so the same key inside the custom extension envelope links the
extension type's own, owner-managed schema and never the ontology graph
spec's.

The rename surface is small but spread out. `$schema` is the wire key
of: the document root (`GraphModel.kt`'s `@SerialName("$schema")`); the
custom extension envelope (`CustomExtension` +
`CustomExtensionSerializer`, ADR-0005); the `importer:table` /
`importer:mapping` extension shapes (`TableExtension.kt` /
`MappingExtension.kt` and their Js twins); the 4.0.0 → 1.0.0
converter's emission (ADR-0008 §1 maps 4.0.0's top-level `version` to
`$schema` holding the schema's `$id`); the three examples under
`src/jvmTest/resources/ontology/`; the Go fixtures; and the generated
`ontology-graph-spec.schema.json`.

The OSG call of 2026-10-07 (Google Doc
169NfYZe08TGQDEHC6D4UZ3vMX6U62sDPuoWmibrvxtY) revisited the naming —
"Schema field naming: $schema vs schema etc." — and decided: **the
field is named `schema`**.

## Decision

**Every format-level `$schema` wire key becomes `schema`** — the
document root (GraphModel), the custom extension envelope, and the
`importer:table` / `importer:mapping` extension shapes — and the
4.0.0 → 1.0.0 converter emits `schema` where ADR-0008 §1 emits
`$schema`. One field name, one spelling, everywhere the format defines
the key.

The exclusion is explicit: the `"$schema"` key at the top of
`ontology-graph-spec.schema.json` itself is JSON Schema's own
draft-2020-12 meta-keyword — it declares which JSON Schema dialect the
artefact is written in — and **stays**, as does `"$id"`. This ADR
renames the ontology graph spec's own field; it does not touch the
keywords JSON Schema reserves for its artefacts.

The field's semantics are unchanged: it is the spec link including the
version, doubles as the document-type marker, and inside an extension
points at the extension type's own owner-managed schema. Only the
spelling changes. The proposal's prose and changelog gain a dated entry
naming this ADR; the Kotlin model's `@SerialName`s, the examples, the
Go fixtures, and the generated schema change in the implementing change
(non-negotiable 2: prose + schema + example together).

## Alternatives considered

1. **Keep `$schema`** — the JSON Schema / JSON-LD sigil convention
   argued in proposal Appendix A ("`$`-prefixed fields are meta"; "one
   rule, `$schema` points at whatever governs the object"). Rejected:
   the OSG call weighed the convention and chose plain `schema`. The
   sigil's meta-vs-data distinction is not load-bearing — the field's
   position (root, envelope) already says what it is — and v1 has no
   other `$`-prefixed field, so the sigil is a one-key exception every
   author must learn. Plain `schema` is the least-surprise spelling
   (design rule 2) for the enterprise developer the format is written
   for, and it performs both of the field's jobs — spec link and
   document-type marker — exactly as well.
2. **A mixed rule: the root field is `schema`, extension envelopes keep
   `$schema`.** Rejected: it is two rules for one concept. The root and
   the envelope field are the same idea — "points at whatever governs
   the object" — and splitting their spellings forces every author and
   consumer to remember which layer takes the sigil, for a distinction
   that carries no semantics. Uniformity is the point of the rename; a
   mixed rule keeps the worst of both spellings.

## Consequences

- This is a wire-incompatible rename for any existing v1 draft
  documents: a document carrying `$schema` stops being valid, and one
  carrying `schema` is invalid under the pre-rename schema. Acceptable
  pre-publication: v1 is unpublished (target 2026-10-21), so no
  released artifact depends on the old spelling.
- The converter's output changes: ADR-0008 §1's root mapping row emits
  `schema: "https://…/1.0.0/schema.json"` instead of `$schema`, and its
  §9 custom-envelope rule reads "`name`/`schema` are not synthesised".
  The change is one emission site; the mapping table's semantics are
  untouched.
- The format gains one uniform rule with no sigil exceptions: every
  field the format defines is a plain word. Proposal Appendix A's
  sigil argument is superseded for the field name; its reasoning about
  the field's semantics (spec link + version, document-type marker,
  owner-managed extension schemas) still stands.
- ADR-0005 and ADR-0008 keep their historical `$schema` spelling in
  their bodies, with a "Later references" note pointing here.
- The implementing change touches the Kotlin model
  (`@SerialName("schema")` on `GraphModel`, `CustomExtension`, the
  importer extension shapes and their Js twins), the converter, the
  three ontology examples, the Go fixtures, and regenerates
  `ontology-graph-spec.schema.json` (ADR-0003) — the schema artefact's
  own top-level `"$schema"`/`"$id"` keywords are untouched.
