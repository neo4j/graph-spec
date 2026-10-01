# ADR-0005: Extension packaging

Status: Proposed

## Context

The v1 format splits extensions in two (proposal §Extensions): named
first-party extensions as `neo4j:`-prefixed keys of an element's (or the
top-level) `extensions` object — each shape owned by its team and
shipped in the SDK — and custom extensions riding the fixed envelope
under the reserved `custom` key (`type` required;
`$schema`/`name`/`definition` optional; `definition` free-form, never
validated). The ontology schema deliberately does not validate named
shapes: `extensionsMap` allows any additional properties and describes
named shapes as "defined by its owner, available in the ontology spec
SDK" (`ontology-spec.schema.json`, `$defs.extensionsMap`); only the
custom envelope has a fixed shape (`$defs.extension`: four fields,
`type` required, unknown extra fields carried untouched). The SDK is
therefore where named-extension shapes live.

ADR-0002 fixed the packaging principle: each named extension
(`neo4j:index`, `neo4j:display`, `neo4j-importer:table`,
`neo4j-importer:mapping`) gets its own folder or file with clear
filesystem separation, while remaining part of the main SDK
distribution; details were deferred to this ADR. ADR-0006 made
extensions the named exception to the API-stability rule and recorded
the research verdict: extension validation does not exist anywhere on
main, so any per-named-extension validation is new surface, not a
behaviour change to an existing contract.

The spec lead has since sharpened the layout requirement: the named
extensions are namespaced by owner prefix (the proposal already
namespaces extension type names by owner — `neo4j-importer:table` — "to
avoid collisions", proposal changelog 2026-09-25), and the SDK layout
must mirror that scheme. Named extensions are grouped **by owner
prefix** into per-owner folders: a `neo4j/` owner folder holds
`neo4j:index` and `neo4j:display`; a `neo4j-importer/` owner folder
holds `neo4j-importer:table` and `neo4j-importer:mapping`. Each
extension lives in its own file/folder within its owner folder, and a
new owner (future extension teams) gets its own top-level owner folder
under the extension tree. The filesystem layout mirrors the extension
type naming scheme.

The extension model on main (map-extension-model research, spot-checked
against the rebased tree):

- One generic value tree, no per-extension-type structure:
  `ExtensionValue` is a `@JsExport @Serializable` sealed class with six
  leaves (`StringValue`, `BooleanValue`, `LongValue`, `DoubleValue`,
  `ListValue`, `MapValue`,
  `src/commonMain/kotlin/model/extension/ExtensionValue.kt`);
  `ExtensionType` is six string constants with no registry
  (`ExtensionType.kt`).
- Addressing is by map key: the `Extensions` mixin exposes
  `val extensions: MutableMap<String, ExtensionValue>`
  (`Extensions.kt`); 13 model classes implement it; `GraphModel` itself
  has no top-level extensions today (`GraphModel.kt`) — v1 adds one
  (schema root `extensions`; placement rule: table/mapping extensions
  live at the top level).
- No validation: nothing under `src/commonMain/kotlin/validate/` touches
  extensions; `Validations.kt` aggregates only
  node/relationship/constraint/index/property/table validators. The only
  structural check today is deserialization-time discriminator dispatch
  in `ExtensionValueSerializer.kt`.
- Codecs are extension-agnostic; the JS surface mirrors the tree
  (`ExtensionValueJs.kt`) plus a minimal string-only `ExtensionsEditor`
  (`src/jsMain/kotlin/model/extension/ExtensionsEditor.kt`); the Go
  model is generated (`go/model/model.go`) with discriminator dispatch.
- A second untyped channel, `options: MutableMap<String, ExtensionValue>`,
  exists on `NodeIndex`/`RelationshipIndex`/`RelationshipConstraint`
  (`model/node/NodeIndex.kt`).
- v1 turns today's core index/display/table/mapping objects into the
  four named extensions (proposal §Extensions; the format invariants
  keep indexes out of core).

## Decision

**Packaging model: owner-prefixed per-extension subpackages of the
existing `model.extension` package, shipped in the main SDK distribution
artifact.**

- **Layout.** One top-level owner folder per owner prefix under
  `src/commonMain/kotlin/model/extension/`; each named extension type
  gets its own folder within its owner folder:

  ```
  src/commonMain/kotlin/model/extension/
    ExtensionValue.kt             unchanged: six-kind value tree
    ExtensionType.kt              unchanged
    Extensions.kt                 unchanged mixin (additive accessor below)
    ExtensionValueSerializer.kt   unchanged
    CustomExtension.kt            new: the fixed custom envelope
    NamedExtension.kt             new: the per-named-extension contract
    neo4j/                        owner folder: neo4j
      index/                      neo4j:index
        IndexExtension.kt         shape + serializer + module object
        ...                       its validators
      display/                    neo4j:display
        DisplayExtension.kt
        ...
    neo4j_importer/               owner folder: neo4j-importer (spelling rule below)
      table/                      neo4j-importer:table
        TableExtension.kt
        ...
      mapping/                    neo4j-importer:mapping
        MappingExtension.kt
        ...
  ```

  **Owner-folder rule.** The extension type `owner:name` maps to the
  path `<owner>/<name>/` under the extension tree; a new owner prefix
  means a new top-level owner folder, with no changes to existing owner
  folders. **Spelling rule.** Hyphens are illegal in Kotlin package
  segments, so the owner prefix's hyphens become underscores in both the
  folder name and the package segment: `neo4j-importer` →
  `neo4j_importer` (package `model.extension.neo4j_importer.table`).
  The mapping from type name to path is therefore deterministic —
  `:` → `/`, `-` → `_` — and folder name always equals package segment,
  honouring Kotlin's directory-matches-package convention. The `neo4j`
  prefix needs no substitution (`model.extension.neo4j.index`). The
  rejected spellings — nesting the importer owner under `neo4j/`
  (`neo4j/importer/table`) and a literal hyphenated `neo4j-importer/`
  directory holding `neo4j_importer` packages — are alternatives 5 and
  6 below.

  The JS mirrors under `src/jsMain/kotlin/model/extension/` follow the
  same owner-folder structure with the existing
  `@JsExport`/`@JsPlainObject` twin pattern; the Go and TypeScript
  surfaces follow from the existing schemancer codegen and JS IR
  declaration pipelines pointed at this layout — no new machinery
  (ADR-0006). Everything ships in the one SDK distribution artifact per
  surface (Gradle module, npm package, Go module); there are no
  per-extension or per-owner published packages.

- **Shape and serializer declaration.** Each named-extension folder
  contains a `@Serializable` data class for its shape — owned by the
  extension's team, versioned with the SDK — and a module object
  implementing the new contract in `NamedExtension.kt`:

  ```kotlin
  interface NamedExtension<T : Any> {
      val key: String                    // wire key, e.g. "neo4j:index"
      val serializer: KSerializer<T>     // shape codec
      val validations: List<Validation>  // the extension's validators
  }
  ```

  For example `model.extension.neo4j.index.IndexExtensionModule`
  declares `key = "neo4j:index"`, the generated kotlinx serializer for
  `IndexExtension`, and the index validators. The contract is static:
  modules are aggregated by explicit lists, not runtime discovery.

- **Storage and access.** The per-element `extensions` map and map-key
  addressing remain the storage mechanism: a named extension's payload
  is stored under its declared key as `ExtensionValue` tree nodes,
  exactly as today. Typed access is additive: an extension function on
  the `Extensions` mixin (`fun <T : Any> Extensions.getNamed(module:
  NamedExtension<T>): T?`) decodes `extensions[module.key]` through the
  module's serializer. The document root gains the same `extensions`
  map (the `Extensions` mixin on `GraphModel`) because the v1 placement
  rule puts `neo4j-importer:table` and `neo4j-importer:mapping` at the
  top level; this is additive API in the extensions area. Consumers may
  keep using the raw map; the typed modules are the recommended path.

- **Validation exposure.** Extension validation is new surface: no
  validation contract exists on main (nothing under `validate/` touches
  extensions), so this ADR designs the hook rather than preserving one.
  Each named-extension folder ships its validators as ordinary
  `validate/Validation` implementations living in that folder, exposed
  through the module object's `validations` list and aggregated into the
  existing `Validations` lists: `Validations.all` gains them, and a new
  `Validations.namedExtensions` group exposes only the extension
  validators for consumers that want to run them separately. No new
  validation framework: the hook is main's
  `Validation`/`Issue`/`ValidationTree` machinery (ADR-0006). The
  `custom` envelope's `definition` and any `neo4j:`-prefixed (or other
  owner-prefixed) key without a declared module are never validated —
  carried untouched, per the format invariant.

- **The custom envelope.** `CustomExtension.kt` lives at the extension
  package root — the envelope shape is fixed by the format, not owned by
  a team — as a `@Serializable data class CustomExtension` mapping the
  four envelope fields (`type` required; `@SerialName("$schema")`,
  `name`, `definition` optional), with `definition: ExtensionValue?`.
  On the wire `custom` holds a list of envelopes; the typed accessor
  decodes it to `List<CustomExtension>`. The envelope always validates;
  its content is never inspected by SDK validators.

- **What happens to the six-kind `ExtensionValue` tree.** It stays,
  unchanged, in three roles: carrier of custom-envelope `definition`
  payloads, at-rest representation of named-extension payloads (typed
  modules decode from it), and carrier of unknown extension content. It
  is no longer the recommended authoring API for the four named
  extensions; the typed modules are.

- **API stability (the ADR-0002 exception).** The current extension API
  stays stable: the `Extensions` mixin, the `ExtensionValue` tree, the
  `ExtensionType` constants, map-key addressing, and the JS
  `ExtensionsEditor` are unchanged; everything above is additive. The
  named exception is exercised for exactly one behaviour change:
  owner-prefixed keys (`neo4j:`, `neo4j-importer:`, future owner
  prefixes with declared modules) become reserved — a payload under a
  declared key that does not match the module's shape is reported by the
  new validators — and `custom` is reserved for the envelope (already
  decided by the format). Consumers running `Validations.all` may see
  new extension issues; that is the intended new surface, not a
  regression. The untyped `options` channel does not carry over onto the
  named extensions: index/display/table/mapping configuration becomes
  typed fields of the module shapes; `options` survives only where the
  v1 core model still has it.

- **What changes for existing extension consumers.** Minimal, per
  ADR-0006: code that reads and writes extensions through the
  `Extensions` map, the `ExtensionValue` tree, or `ExtensionsEditor`
  compiles and behaves as today. Consumers opting into v1 named
  extensions gain typed modules and validation. Consumers with payloads
  under owner-prefixed keys must move them to `custom` or conform to
  the declared shape — a document-level migration the one-way
  4.0.0 → 1.0.0 converter handles, not an API break.

## Alternatives considered

1. **Separate published packages per named extension or per owner** (a
   Gradle module and npm package per extension — `ontology-ext-index`,
   `ontology-ext-display`, … — or per owner folder). Rejected: ADR-0002
   already fixed that named extensions ship in the main SDK
   distribution, and for good reason — they are first-party and
   versioned with the spec, so a consumer needing `neo4j:index` should
   not need a second coordinated dependency; N packages multiply release
   and version-skew coupling for no consumer benefit. Owner folders are
   a filesystem and ownership boundary inside the one artifact, not a
   packaging boundary.
2. **Keep the single generic value tree with no per-extension
   structure.** Rejected: it gives named extensions no typed shapes and
   no autocomplete — the proposal's stated reason for named extensions
   is exactly that developer experience — leaves per-extension
   validation with no home, and makes the format's named/custom split
   invisible in the SDK.
3. **Flat files in the existing package, or a new sibling top-level
   tree.** Flat: one file per extension directly in `model/extension/`.
   Sibling: a new `src/commonMain/kotlin/extension/` tree beside
   `model/`. Rejected: flat files fail ADR-0002's clear-separation
   requirement as soon as an extension grows a serializer, validators,
   and JS twins — and fail the owner-grouping requirement outright,
   since four extensions across two owners would interleave in one
   directory with no per-owner or per-type boundary for review and
   ownership tooling to target — and a sibling tree forks the
   extensions home away from the existing `model.extension` package for
   no architectural gain, against ADR-0006's keep-main's-architecture
   rule.
4. **A dynamic registry with runtime discovery** (modules self-register
   into a central `ExtensionRegistry`, service-loader style). Rejected:
   no registry exists on main — `ExtensionType` is six constants — and
   runtime discovery adds machinery the JS and Go surfaces cannot mirror
   cleanly; static per-folder modules aggregated by explicit lists are
   simpler, tree-shakeable, and match main's way of working (ADR-0006).
5. **Nest the importer owner under the `neo4j/` folder**
   (`neo4j/importer/table/`, `neo4j/importer/mapping/`). Rejected: it
   collapses the owner boundary the layout exists to mirror —
   `neo4j-importer` extensions are owned by the importer team, not the
   core `neo4j` extensions team — so ownership and review tooling would
   target the wrong boundary, the path would falsely claim `neo4j`
   ownership, and a future owner split would force a package move; the
   type name's owner prefix is `neo4j-importer`, a peer of `neo4j`, and
   the tree must show them as peers.
6. **Literal hyphenated owner directory with a mismatched package**
   (`neo4j-importer/` on disk, package `model.extension.neo4j_importer`
   inside). Rejected: legal in Kotlin — the compiler does not enforce
   directory-package matching — but it breaks the
   directory-matches-package convention that IDEs, refactoring tools,
   and main's own tree rely on, and it makes the type→path mapping
   two rules (folder spelling, package spelling) instead of one;
   applying the single `-` → `_` substitution to both folder and
   package keeps the mapping deterministic and the tree unsurprising.

## Consequences

- Port tracks create the two owner folders (`neo4j/`,
  `neo4j_importer/`), the four extension folders beneath them, and the
  two root files (`NamedExtension.kt`, `CustomExtension.kt`); each named
  extension's shape, serializer, module object, and validators land in
  its own folder within its owner folder, owned by its team.
- Future owners follow the owner-folder rule: a new owner prefix gets a
  new top-level folder under `model/extension/`, spelled with the
  `-` → `_` substitution, and the extension type's `:` → `/` mapping
  gives the per-extension folder; no existing owner folder changes.
- `Validations.all` grows the named-extension validators and a new
  `Validations.namedExtensions` group appears; consumers running `all`
  may see new extension issues — intended new surface under the
  ADR-0002 extensions exception, to be called out in the port's release
  notes.
- Owner-prefixed extension keys become reserved for declared modules
  and `custom` for the envelope; the 4.0.0 → 1.0.0 converter moves any
  colliding 4.0.0 payloads.
- `GraphModel` gains a top-level `extensions` map via the existing
  `Extensions` mixin, for top-level named extensions per the placement
  rule; additive, no change to existing element-level maps.
- The six-kind `ExtensionValue` tree, the `Extensions` mixin, map-key
  addressing, and `ExtensionsEditor` are unchanged; the typed modules
  are additive. Any consumer-visible delta in the extensions area beyond
  what this ADR lists is a defect against ADR-0006's minimal-change bar.
- The untyped `options` maps on the index classes are replaced by typed
  module fields when indexes become `neo4j:index`; core-model classes
  that keep `options` in v1 are unaffected by this ADR.
- No format artefact changes: the wire shape, the schema's
  `extensionsMap`, and the examples are untouched — this ADR governs SDK
  surface only, so no proposal changelog entry, schema edit, or new
  example is required by non-negotiables 2 and 4.
- ADR-0002's deferral ("details are deferred to ADR-0005") and
  ADR-0006's pointer ("whose packaging and shape ADR-0005 decides") are
  resolved by this ADR.
