# ADR-0006: Port architecture principle

Status: Accepted

## Context

The branch is rebased onto `main` (ADR-0002), so the full graph spec
4.0.0 implementation — the Kotlin Multiplatform model, the
kotlinx-serialization codecs, the `@JsExport` JS/TS surface, the Go
schemancer codegen pipeline, and the validator framework under
`src/commonMain/kotlin/validate/` — coexists with the v1 ontology
overlay (`ontology-spec.schema.json`, `docs/`, `examples/`, `scripts/`).
The SDK/converter port is in scope in this repo now (ADR-0002), and the
schema it ports *against* is hand-maintained with implementations
conforming to it (ADR-0003).

ADR-0002 set an API-stability constraint: existing SDK public APIs
(Kotlin, Go, TypeScript) "stay the same to the largest extent possible
through the port", with extensions as the named exception. That phrasing
leaves the port's architecture open: "largest extent possible" could
mean anything from a conservative re-target to a redesign that keeps
only a few signatures. The spec lead has now decided the principle in
conversation: "use main's way of working, just with the new schema" —
and, for consumers of today's Kotlin, JS, or Go SDKs, "the change would
be minimal. Maybe some things around extensions only that changes —
other than the name." This ADR sharpens ADR-0002's constraint into that
concrete rule.

On the extensions exception specifically: the map-extension-model
research recorded that today's extension surface is a generic six-kind
value tree — the `ExtensionValue` sealed class
(`src/commonMain/kotlin/model/extension/ExtensionValue.kt`:
`StringValue`, `BooleanValue`, `LongValue`, `DoubleValue`, `ListValue`,
`MapValue`) — addressed by map keys, with per-element `extensions` maps
on nodes, relationships, properties, and the document root. Extension
validation does not exist anywhere: no validator under
`src/commonMain/kotlin/validate/` touches extensions. The extensions
area therefore has no validation contract to preserve through the port;
any per-named-extension validation that v1 introduces is new surface,
not a behaviour change to an existing contract.

## Decision

- **The port keeps main's architecture and machinery, re-targeted at
  the v1 ontology schema.** The Kotlin Multiplatform model, the
  kotlinx-serialization codecs, the `@JsExport` JS/TS surface, the Go
  schemancer codegen pipeline, and the validator framework are carried
  over and pointed at `ontology-spec.schema.json` — not redesigned, not
  rewritten. The port is a re-targeting of proven machinery onto the new
  schema, per ADR-0003's rule that the schema is hand-maintained and the
  implementations conform to it.
- **Consumer-visible changes are limited to exactly two.** For consumers
  of today's Kotlin, JS, or Go SDKs for graph-schema, the only visible
  deltas are (1) the name — graph-spec becomes ontology-spec — and (2)
  the extensions area, whose packaging and shape ADR-0005 decides.
  Everything else about the port — how the model classes, codecs,
  codegen, and validators are adapted internally to the v1 schema — is
  an implementation detail of the port and carries no consumer-facing
  commitment beyond ADR-0002's API-stability constraint as sharpened
  here.

## Alternatives considered

1. **Clean-slate SDK redesign.** Design new Kotlin/Go/TypeScript SDKs
   for v1 from scratch, treating the clean-break format as licence for a
   clean-break implementation. Rejected: it imposes consumer migration
   cost for no consumer benefit — the existing machinery is proven
   against 4.0.0, and the schema change does not invalidate it. The
   format broke; the architecture did not.
2. **Keep the 4.0.0 model classes as a compatibility shim alongside the
   v1 model.** Ship both models in the SDKs so 4.0.0 consumers compile
   unchanged against the old classes while v1 consumers use the new
   ones. Rejected: it means two models to maintain forever, and the
   migration story already exists — the one-way 4.0.0 → 1.0.0 converter
   (ADR-0001, proposal) moves documents, not APIs, so consumers migrate
   their data and their dependency once rather than the repo carrying a
   permanent duplicate model.

## Consequences

- The port tracks re-target rather than rewrite: the Kotlin/JS →
  TypeScript declaration surface and the Go schemancer codegen pipeline
  survive as machinery, adjusted only where ADR-0003 already decided
  (the schema stays hand-maintained and implementations conform; the
  enum-rewriting `TypeScriptModifierTask` post-processing is retired for
  the v1 surface). No port track may propose a new architecture without
  a new ADR superseding this one.
- "Minimal consumer change" becomes a reviewable bar for every port
  track: any consumer-visible delta beyond the name and the extensions
  area is a defect against this ADR, not a design choice.
- The naming mechanics per surface — Gradle modules, Kotlin packages,
  the npm artifact, the Go module path — are decided in ADR-0004; this
  ADR only fixes that the rename happens and is one of the two
  consumer-visible deltas.
- Extension packaging and shape are decided in ADR-0005; because no
  extension validation contract exists today, ADR-0005 designs new
  surface rather than preserving old behaviour.
- ADR-0002's "largest extent possible" phrasing is sharpened, not
  replaced: this ADR is the concrete reading of that constraint, and
  ADR-0002 otherwise stands.

## Later references

- ADR-0009 (docs/adr/0009-ontology-graph-spec-rename.md): renamed the spec "Ontology Spec" → "Ontology Graph Spec"; this ADR's body keeps the historical name.
