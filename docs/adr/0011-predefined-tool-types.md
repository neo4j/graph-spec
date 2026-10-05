# ADR-0011: Predefined tool types

Status: Proposed

## Context

`tools` is a core field on node and relationship entries (decided
2026-09-25): `{ type, name?, description?, ... }`, where `type`
discriminates (`canonicalQuery`, `externalRequest`, ...) and the remaining
per-type fields are owner-defined, carried in `Tool.extra` and inlined on
the wire (`model/tool/Tool.kt`, `ToolSerializer`). The proposal is
authoritative on the token forms — camelCase `canonicalQuery` with a
`cypher` field (proposal tool table and example: `findCoActors` on
`ACTED_IN`) — and records the governance split as open item 4: the per-type
shapes are owned by the agent-surface teams, not this spec. The schema
deliberately does not pin them: `$defs/tool` is an open surface
(`additionalProperties: true`) documenting exactly that.

That leaves the SDK holding an untyped surface for a shape everyone uses.
A `canonicalQuery` tool without `cypher` parses fine, validates fine, and
fails only when an agent tries to run it; TypeScript consumers building
tools get no autocomplete beyond the three core fields. This is the same
gap ADR-0005 closed for named extensions — owner-defined shapes, typed in
the SDK — and it admits the same cure.

## Decision

**The SDK ships predefined, fully-validated tool-type shapes, starting
with `canonicalQuery`, mirroring the ADR-0005 named-extension module
pattern on the tool surface.**

- **Contract.** `model/tool/ToolType.kt` declares the per-tool-type
  contract, mirroring `NamedExtension<T>`:

  ```kotlin
  interface ToolType<T : Any> {
      val type: String                    // wire token, e.g. "canonicalQuery"
      val serializer: KSerializer<T>      // shape codec
      val validations: List<Validation>   // the tool type's validators
  }
  ```

  The contract is static: modules are aggregated by explicit lists, not
  runtime discovery.

- **Shape and module.** `model/tool/CanonicalQueryTool.kt` declares
  `@Serializable data class CanonicalQueryTool(type = "canonicalQuery",
  name?, description?, cypher)` — `cypher` required, the token camelCase
  per the proposal — and `CanonicalQueryToolModule : ToolType<
  CanonicalQueryTool>` carrying the token, the codec, and the type's
  validators. The typed shape is the `canonicalQuery` owner's contract in
  the SDK; format-wise the shape stays owner-defined (open item 4), so the
  schema and the wire form do not change.

- **Typed access.** `Tool.asTyped(module)` decodes the tool's full
  payload — `type`/`name`/`description` plus the inlined extras — through
  the module's serializer, returning null when the tool's type token is
  not the module's. The raw `Tool` (with its `extra` map) remains
  available; the typed module is the recommended path. The reverse
  direction, `CanonicalQueryTool.toTool()`, is the authoring bridge: the
  typed shape becomes the open `Tool` with `cypher` in `extra`, inlined on
  the wire by `ToolSerializer`.

- **Validation.** `CanonicalQueryToolShape` (one object, next to the
  shape, per the ADR-0005 owner-folder convention) checks every tool of
  type `canonicalQuery` in full: `cypher` present, a string, non-blank.
  Tools are core, so the rule registers in `Validations.core` — not in
  `namedExtensions`, which stays the ADR-0005 group. Paths come from
  `ModelWalk` (`tools[<index>]` beneath the element path), never
  re-derived.

- **JS/TS surface.** `CanonicalQueryToolJs` is the `@JsExport
  @JsPlainObject` twin with the factory `canonicalQueryToolJs(cypher,
  name, description)`. Kotlin/JS emits `String` as `string` in the
  `.d.mts` — a literal type cannot be pinned from Kotlin — so the factory
  is the autocomplete entry point: it sets `type = "canonicalQuery"`,
  giving TS consumers the literal at construction. `toJs`/`toClass`
  convert to and from the Kotlin shape; `toToolJs()` bridges to the open
  `ToolJs` for attaching to a node's `tools` array.

- **Future tool types** follow the same pattern: a typed shape + module
  object + validators per type, in owner-prefixed subpackages of
  `model/tool/` when a second owner arrives (the ADR-0005 owner-folder
  rule applied to tools). Until then, predefined types live directly in
  `model/tool/`.

## Alternatives considered

1. **Leave tools untyped** (status quo: `Tool` + the `extra` map only).
   Rejected: no validation — a `canonicalQuery` tool missing `cypher`
   sails through every gate and fails at agent runtime — and no
   autocomplete, which is the stated reason the SDK types owner-defined
   shapes at all (ADR-0005 alternative 2, same verdict).
2. **Pin the per-type shapes in the spec schema's `$defs`** (a
   `canonicalQuery` definition with `cypher` required, discriminated on
   `type`). Rejected: the per-type shapes are owner-defined (proposal
   open item 4) and `$defs/tool` is deliberately open — the spec schema
   does not pin them, for the same reason `extensionsMap` does not pin
   named-extension shapes (ADR-0005): owner evolution must not force a
   spec release. A schema-side shape would also fork the source of truth
   between the schema and the owner's SDK module.
3. **Extension-style owner folders under `model/tool/` for future
   types** (e.g. `model/tool/neo4j/canonicalQuery/` once a second owner
   arrives). Accepted as the pattern for future tool types: the ADR-0005
   owner-folder rule transfers unchanged, and `canonicalQuery` moves into
   its owner folder when a second owner's type lands. Rejected for today:
   with one predefined type and one owner, a nested folder adds depth
   without a boundary to show for it.
4. **Model tool types as named extensions** (a `neo4j:canonicalQuery`
   key in `extensions`). Rejected: the format already decided tools are a
   core field, not extensions (2026-09-25); moving them would be a format
   break against design rule 7's spirit and the proposal's tool table.

## Consequences

- `model/tool/` gains `ToolType.kt` (contract + `Tool.asTyped`),
  `CanonicalQueryTool.kt` (shape + module + `toTool()`), and
  `CanonicalQueryToolValidators.kt` (`CanonicalQueryToolShape`);
  `ModelWalk` gains the `tools[<index>]` path segment and `forEachTool`;
  `Validations.core` grows the rule, so consumers running `core` (or any
  group containing it) may see new `missing_canonical_query_cypher`
  issues — intended new surface, mirroring ADR-0005's validation
  exposure.
- The jsMain twin (`CanonicalQueryToolJs` + `canonicalQueryToolJs`
  factory) ships in the npm package's `.d.mts` through the existing
  `generateTypeScriptDefinitions` pipeline; no new machinery.
- No format artefact changes: the schema's `$defs/tool` stays open and
  byte-identical (the drift check proves it), the wire form is unchanged,
  and the examples already exercise `canonicalQuery` (the proposal's
  `findCoActors` in `movies.ontology.json`). The proposal records the SDK
  surface in a dated changelog entry naming this ADR.
- The `canonicalQuery` owner's shape contract now lives in this repo's
  SDK; a shape change (new optional field, renamed field) is an SDK
  change owned by that team, never a schema change. Future tool types
  (`externalRequest`, ...) get their own module objects under
  `model/tool/`, moving to owner folders per alternative 3 when a second
  owner arrives.
