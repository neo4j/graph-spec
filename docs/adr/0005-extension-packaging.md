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
(`neo4j:index`, `ui:display`, `importer:table`,
`importer:mapping`) gets its own folder or file with clear
filesystem separation, while remaining part of the main SDK
distribution; details were deferred to this ADR. ADR-0006 made
extensions the named exception to the API-stability rule and recorded
the research verdict: extension validation does not exist anywhere on
main, so any per-named-extension validation is new surface, not a
behaviour change to an existing contract.

The spec lead has since sharpened the layout requirement: the named
extensions are namespaced by owner prefix (the proposal already
namespaces extension type names by owner — `importer:table` — "to
avoid collisions", proposal changelog 2026-09-25), and the SDK layout
must mirror that scheme. Named extensions are grouped **by owner
prefix** into per-owner folders: a `neo4j/` owner folder holds
`neo4j:index`; a `ui/` owner folder holds `ui:display` (the display
owner is the UI/console surface, hence the generic `ui` prefix); an
`importer/` owner folder holds `importer:table` and
`importer:mapping`. Each
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
    ui/                           owner folder: ui
      display/                    ui:display
        DisplayExtension.kt
        ...
    importer/                     owner folder: importer
      table/                      importer:table
        TableExtension.kt
        ...
      mapping/                    importer:mapping
        MappingExtension.kt
        ...
  ```

  **Owner-folder rule.** The extension type `owner:name` maps to the
  path `<owner>/<name>/` under the extension tree; a new owner prefix
  means a new top-level owner folder, with no changes to existing owner
  folders. Owner prefixes carry no hyphens, so folder name always
  equals package segment and the type→path mapping is a single rule —
  `:` → `/` — honouring Kotlin's directory-matches-package convention
  (`model.extension.neo4j.index`, `model.extension.ui.display`,
  `model.extension.importer.table`). The rejected spelling — nesting
  the importer owner under `neo4j/` (`neo4j/importer/table`) — is
  alternative 5 below; keeping the original hyphenated importer prefix
  is alternative 6. The npm package coordinate is owned by ADR-0009's
  product rename, not by the extension owner prefix (ADR-0009, revised
  pre-merge, moved it to `@neo4j/ontology-graph-spec` — the spec is a
  company-wide artefact); the remaining `neo4j-importer` mentions in
  the repo are the immutable accepted-ADR records (ADR-0004, ADR-0009)
  of the pre-rename names.

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
  rule puts `importer:table` and `importer:mapping` at the
  top level; this is additive API in the extensions area. Consumers may
  keep using the raw map; the typed modules are the recommended path.

- **Payload cardinality: object or list.** A named extension's payload
  under its key is one payload object OR a list of payload objects; a
  single object is equivalent to a one-element list (one wire rule —
  normalisation is a library detail, design rule 8). Typed access
  reflects this: `getNamed` decodes the single-object form and stays
  strict (a shape mismatch throws); `getNamedList(module)`
  normalises either form to a list — object → one-element list, list →
  element-wise decode, malformed → empty list, with
  `MalformedNamedExtensionPayload` the rule that reports what no module
  can decode. `neo4j:index` is the first module whose payloads are
  routinely several per element (the converter fans a multi-label
  4.0.0 index out to one payload per labeled node), so its typed access
  and validators go through `getNamedList`.

- **Placement levels.** `neo4j:index` payloads are legal at the root,
  on nodes, on relationships, and on properties — always under the
  element's `extensions` key (the placement rule: an extension lives on
  the element it describes; a property-level index describes that
  property, e.g. a vector index on an embedding property). Root-level
  indexes describe the model as a whole; the index validators check
  options/type coherence there but skip property-reference resolution —
  the root has no element properties to resolve against.

- **The index `type` field.** The `neo4j:index` payload's index-kind
  field is `type`, valued with the lowercase v1 tokens (`range`,
  `fulltext`, `point`, `text`, `vector`, `lookup`) — the 4.0.0 field
  name and the spec's own vocabulary (`type` on relationships, tools,
  and the custom envelope).

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
  owner-prefixed keys (`neo4j:`, `ui:`, `importer:`, future owner
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
   the `importer` extensions are owned by the importer team, not the
   core `neo4j` extensions team — so ownership and review tooling would
   target the wrong boundary, the path would falsely claim `neo4j`
   ownership, and a future owner split would force a package move; the
   type name's owner prefix is `importer`, a peer of `neo4j`, and
   the tree must show them as peers.
6. **Keep the importer owner's original hyphenated, `neo4j-`-qualified
   prefix** (no rename). Rejected: the hyphen is illegal in a Kotlin
   package segment, so the owner folder and package would keep an
   underscore-substitution spelling plus a permanent ktlint
   package-name exemption in `.editorconfig` — two standing carve-outs
   carried for one prefix — and the `neo4j-` qualifier adds no
   collision safety the owner-prefix scheme doesn't already provide:
   owner prefixes are unique by construction, so `importer` collides
   with `neo4j` exactly as little as the hyphenated form did.
7. **List-only payloads for multi-instance named extensions** (ADR-0008
   §8's interim call: "Multi-instance extensions are always a **list**
   of payload objects under the named key — one wire form, never
   'object for one, list for many'"). Rejected: the typed modules and
   the examples landed single-object, the single-index case is the
   common one, and forcing `[ { … } ]` on it reads as ceremony against
   the least-surprise principle; normalising object-or-list in the
   library (`getNamedList`) keeps one semantic rule — a single object
   is a one-element list — without taxing the common case.
8. **`kind` as the index type field name.** Rejected: `type` is the
   4.0.0 field name (`Index.type`) and the spec's own word for the same
   concept (relationship `type`, tool `type`, custom-envelope `type`);
   a second word violates least surprise, and the converter's
   UPPERCASE-enum → lowercase-token mapping is a value mapping, not a
   field rename.

## Consequences

- Port tracks create the three owner folders (`neo4j/`, `ui/`,
  `importer/`), the four extension folders beneath them, and the
  two root files (`NamedExtension.kt`, `CustomExtension.kt`); each named
  extension's shape, serializer, module object, and validators land in
  its own folder within its owner folder, owned by its team.
- Future owners follow the owner-folder rule: a new owner prefix gets a
  new top-level folder under `model/extension/` (folder name equals
  package segment; owner prefixes carry no hyphens), and the extension
  type's `:` → `/` mapping gives the per-extension folder; no existing
  owner folder changes.
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
- `neo4j:index` payloads decode and validate at the root, node,
  relationship, and property levels, in object or list form; the
  4.0.0 → 1.0.0 converter emits lowercase `type` tokens and fans a
  multi-label 4.0.0 index out to one payload per labeled element (the
  4.0.0 `labels` field is dropped). The object-or-list rule, the
  placement levels, and the `type` field are recorded in the proposal
  changelog (2026-10-03) and exercised by the org example; the schema's
  `extensionsMap` stays owner-defined and unvalidating, so the schema
  artefact is untouched.
- ADR-0002's deferral ("details are deferred to ADR-0005") and
  ADR-0006's pointer ("whose packaging and shape ADR-0005 decides") are
  resolved by this ADR.
