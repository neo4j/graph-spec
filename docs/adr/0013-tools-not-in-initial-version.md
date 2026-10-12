# ADR-0013: Tools not in the initial version

Status: Proposed

## Context

`tools` is a core field on node and relationship entries by a decision
of 2026-09-25 (proposal changelog: "Tools are core: first-class `tools`
field on node and relationship entries (`{ type: canonicalQuery |
externalRequest | ..., name, description, ... }`), leaving the
extensions section"). The schema carries `tools` on the node and
relationship definitions with an open `$defs/tool`
(`additionalProperties: true`) — the per-type shapes are owner-defined
(proposal open item 4), deliberately unpinned by the schema. The SDK
built on that: `model/tool/` (`Tool`, `ToolSerializer`, the
catch-all-carried open surface) and, per ADR-0011, the predefined tool
types — `ToolType<T>` modules aggregated into `Validations.core`, with
`canonicalQuery` the first typed, fully-validated shape — plus the Js
twins and tool traversal in `ModelWalk` (`tools[<index>]` paths).

The OSG call of 2026-10-07 revisited the question — "Tools: first-class
properties or named extension?" — and decided the opposite for the
initial version: do not include tools in the initial version, go
through the OSP process with it. The 2026-09-25 promotion predates that
call; the call is the later, governing decision.

## Decision

**Tools are not in the initial v1.** The `tools` field on node and
relationship entries leaves the format, and the SDK tool surface goes
with it: `model/tool/` (`Tool.kt`, `ToolType.kt`, `ToolSerializer.kt`,
`CanonicalQueryTool.kt`, `CanonicalQueryToolShape.kt`), the Js twins
under `src/jsMain/kotlin/model/tool/`, the `tools` fields on
`Node.kt`/`Relationship.kt` (and their Js twins), the tool-type module
machinery including `canonicalQuery` per ADR-0011, and the tool
traversal in `validate/ModelWalk.kt` are all removed. Re-introducing
tools — in any form — goes through the OSP process.

This reverses the 2026-09-25 proposal decision that promoted tools to a
core field, and supersedes ADR-0011 (predefined tool types): with no
`tools` field there is no tool surface for the `ToolType` module
pattern to sit on.

## Alternatives considered

1. **Keep tools as a core field** (the 2026-09-25 decision, status quo).
   Rejected: the OSG call of 2026-10-07 — the governing decision body —
   decided against shipping tools in the initial version; keeping them
   would ship a surface the group explicitly deferred, and every core
   field in the initial version is a compatibility promise v1 carries
   forever.
2. **Move tools to a named extension** (the ADR-0005 mechanism, e.g. a
   `neo4j:tools` key). Rejected: the call's instruction is not to
   re-home tools but to exclude them and run them through the OSP
   process; a named extension would ship the same owner-defined shapes
   under a different key without that process, and would pre-empt the
   OSP outcome with a wire key the format then has to carry.
3. **Keep the schema's open `$defs/tool` but drop the SDK surface**
   (schema keeps the field, SDK untypes it). Rejected: half-in is the
   worst split — the schema would pronounce `tools` documents valid
   that no shipped SDK can read back, forking the format from its
   conformance story (non-negotiable 2: every format feature exercised
   by an example that round-trips through the model).

## Consequences

- The schema loses the `tools` field on node and relationship entries
  and the `$defs/tool` definition; `ontology-graph-spec.schema.json` is
  regenerated from the de-annotated model, never hand-edited
  (ADR-0003). Documents carrying `tools` stop being schema-valid.
- The SDK loses the tool packages: `model/tool/` in commonMain, the Js
  twins and factories in jsMain, the `tools` field and its accessors on
  `Node`/`Relationship` (Kotlin and Js), the `ToolType` module list and
  its aggregation into `Validations.core`, `CanonicalQueryToolShape`,
  and `ModelWalk`'s `forEachTool` / `tools[<index>]` path segment.
  Consumers running `core` see no tool validations.
- The examples under `src/jvmTest/resources/ontology/` lose their
  `tools` sections (the proposal's `findCoActors` on `ACTED_IN` among
  them); the proposal doc's tool table, open item 4, and the
  2026-09-25/2026-10-03 tool changelog entries are amended with a dated
  entry naming this ADR.
- The 4.0.0 → 1.0.0 converter is unaffected: it never emitted tools.
- ADR-0011 is superseded and gains a **Later references** note pointing
  here; its body stands as the record of the design while it lived.
- Re-adding tools later — as a core field, a named extension, or any
  other shape — requires the OSP process first, then (being a format
  change) a new ADR here before implementation, per non-negotiable 1.
