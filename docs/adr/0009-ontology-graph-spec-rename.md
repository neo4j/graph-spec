# ADR-0009: The Ontology Graph Spec rename

Status: Proposed

## Context

The product this repo builds has been renamed. Linear ONT-24 renames
"Neo4j Ontology Specification" ("Ontology Spec") to **"Neo4j Ontology
Graph Specification"** ("Ontology Graph Spec"). The Linear issue title's
original wording, "Graph Ontology Spec", was considered and corrected by
the spec lead: the name is Ontology Graph Spec, and this ADR records
that wording as decided.

Every consumer-visible identifier in the tree still carries the old
name: the schema file and its `$id` namespace, the format token, the
Kotlin/JS/Go entry points, the npm/Maven/Gradle artifact names, the
Kotlin/Native lib family, and the Go env var and build tag. ADR-0004's
"Naming: mechanics per surface" section picked those identifiers
(graph-spec → ontology-spec); ONT-24 renames them again
(ontology-spec → ontology-graph-spec), and this ADR is the single
source of truth for that second rename — the complete old→new mapping
table below is what the rename executes against.

Facts that shape the decision (verified at planning time):

- **Nothing v1 has shipped under the current names.**
  `npm view @neo4j-importer/ontology-spec` → 404 (the package has never
  been published); `git tag -l` shows only `go/v0.0.x` and
  `go/v4.0.0-alpha.*` tags — no v1 tag of any surface; the
  `publish-maven` job in `.github/workflows/release.yaml` is disabled
  (`if: false`, line 232) pending a release-policy decision; and
  `src/commonMain/kotlin/OntologySpec.kt` has no git history on `main`
  (`git log main -- <path>` is empty — it was created on the port
  branch). No consumer can depend on a v1 artifact under the old name,
  because none exists.
- **The spec lead has decided:** the `ontologymodel` native-lib family
  (baseName, `libontologymodel.{so,dylib}`, `ONTOLOGYMODEL_LIB_PATH`)
  renames too, and **no compatibility aliases or deprecation wrappers
  are kept** anywhere. ADR-0006's renamed-away-API survival mechanism
  (`typealias` + delegating factory, e.g. `NodeConstraintJs =
  ConstraintJs`) exists for renames of *shipped* 4.0.0 API; it does not
  apply here because nothing under the old v1 names ever shipped.

What renames and what stays needs a discrimination rule, because
"ontology" is both the product's name and the domain word the format is
about: the format describes ontologies, the examples *are* ontologies,
and historical records name the old product. The Decision section states
the rule and its explicit stay-list.

## Decision

### 1. The name

The product is the **Neo4j Ontology Graph Specification v1**, short
form **Ontology Graph Spec**. Per-surface identifier forms follow each
surface's existing convention, inserting `graph` after `ontology`:

| convention | form | example |
|---|---|---|
| prose | `Ontology Graph Spec` / `the ontology graph spec` | README, AGENTS.md, pom description |
| kebab-case | `ontology-graph-spec` | schema file, npm/Maven/Gradle names |
| snake_case | `ontology_graph_spec` | format token, Go build tag |
| PascalCase | `OntologyGraphSpec` | Kotlin/JS/Go identifiers |
| native-lib family | `ontologygraphmodel` / `ONTOLOGYGRAPHMODEL` | baseName, lib files, env var |

The GitHub repo rename (`github.com/neo4j/graph-spec` →
`neo4j/ontology-graph-spec`) remains deferred exactly as ADR-0001/ADR-0002
recorded it (an open core-group decision; remote operations out of
scope) — only the target name changes, from the `neo4j/ontology-spec`
ADR-0001/ADR-0004 anticipated to `neo4j/ontology-graph-spec`.

### 2. The mapping table (old → new, complete)

| # | surface | old | new |
|---|---|---|---|
| 1 | Schema file (repo root) | `ontology-spec.schema.json` | `ontology-graph-spec.schema.json` |
| 2 | Schema `$id` (and every document `$schema` pointing at it: `src/jvmTest/resources/ontology/*.json`, `go/model/testdata/ontology-spec-example.json`, `go/validation/testdata/invalid-graph-model.json`, the converter's `SCHEMA_ID` constant) | `https://neo4j.com/ontology-spec/1.0.0/schema.json` | `https://neo4j.com/ontology-graph-spec/1.0.0/schema.json` |
| 3 | First-party extensions namespace in example/testdata `$schema` links (e.g. `movies.ontology.json`, `go/model/testdata/ontology-spec-example.json`) | `https://neo4j.com/ontology-spec/extensions/...` | `https://neo4j.com/ontology-graph-spec/extensions/...` |
| 4 | Schema `title` | `Neo4j Ontology Specification v1` | `Neo4j Ontology Graph Specification v1` |
| 5 | Schema `description` lead sentence (rest unchanged; the generated copy in `go/model/model.go` regenerates via the drift check) | `Draft JSON Schema for the Ontology spec v1 proposal …` | `Draft JSON Schema for the Ontology Graph spec v1 proposal …` |
| 6 | Format token (`src/commonMain/kotlin/model/Type.kt`) | `Type.ONTOLOGY_SPEC = "ontology_spec"` | `Type.ONTOLOGY_GRAPH_SPEC = "ontology_graph_spec"` |
| 7 | Kotlin entry point — sealed class, factory function, private impl (`src/commonMain/kotlin/OntologySpec.kt`) | `OntologySpec` / `OntologySpecImpl`, file `OntologySpec.kt` | `OntologyGraphSpec` / `OntologyGraphSpecImpl`, file `OntologyGraphSpec.kt` |
| 8 | Kotlin config (`src/commonMain/kotlin/OntologySpecConfig.kt`) | `OntologySpecConfig`, file `OntologySpecConfig.kt` | `OntologyGraphSpecConfig`, file `OntologyGraphSpecConfig.kt` |
| 9 | Converter class (`src/commonMain/kotlin/migrate/migration/graphSpec/`) | `GraphSpecV4OntologySpecMigration` | `GraphSpecV4OntologyGraphSpecMigration` |
| 10 | Converter test (`src/jvmTest/kotlin/migrate/`) | `GraphSpecV4OntologySpecMigrationTest` | `GraphSpecV4OntologyGraphSpecMigrationTest` |
| 11 | Spec-gate test (`src/jvmTest/kotlin/spec/`) | `OntologySpecExamplesTest` | `OntologyGraphSpecExamplesTest` |
| 12 | Validation test (`src/jvmTest/kotlin/spec/`) | `OntologySpecValidationTest` | `OntologyGraphSpecValidationTest` |
| 13 | Round-trip test (`src/jvmTest/kotlin/spec/`) | `OntologyModelRoundTripTest` | `OntologyGraphModelRoundTripTest` |
| 14 | YAML round-trip test (`src/jvmTest/kotlin/spec/`) | `OntologyYamlRoundTripTest` | `OntologyGraphYamlRoundTripTest` |
| 15 | Go migration funcs (`go/migration/migration.go`) | `ToOntologySpec` / `FromOntologySpec` | `ToOntologyGraphSpec` / `FromOntologyGraphSpec` |
| 16 | Go model-type constant | `ModelTypeOntologySpec = "ontology_spec"` | `ModelTypeOntologyGraphSpec = "ontology_graph_spec"` |
| 17 | Go model-version constant | `ModelVersionOntologySpecLatest` | `ModelVersionOntologyGraphSpecLatest` |
| 18 | Go testdata file (`go/model/testdata/`) | `ontology-spec-example.json` | `ontology-graph-spec-example.json` |
| 19 | npm package (`build.gradle.kts` `packageJson`) | `@neo4j-importer/ontology-spec` | `@neo4j-importer/ontology-graph-spec` |
| 20 | Maven coordinates + pom (`build.gradle.kts` `mavenPublishing`) | `org.neo4j.importer:ontology-spec`; pom `name = "ontology-spec"`, `description = "Neo4j Ontology Specification Library"` | `org.neo4j.importer:ontology-graph-spec`; pom `name = "ontology-graph-spec"`, `description = "Neo4j Ontology Graph Specification Library"` |
| 21 | Gradle project (`settings.gradle.kts`); the generated `.d.mts` filename follows the root project name (ADR-0004) | `rootProject.name = "ontology-spec"` | `rootProject.name = "ontology-graph-spec"` |
| 22 | Kotlin/Native lib (`build.gradle.kts`, three targets; committed libs under `go/internal/bridge/lib/`; `go/scripts/generate-kotlin-native-libs.sh`) | baseName `ontologymodel`; `libontologymodel.{so,dylib}` | baseName `ontologygraphmodel`; `libontologygraphmodel.{so,dylib}` |
| 23 | Env var (`go/internal/bridge/loader.go` `LibPathEnv`, docs, tests) | `ONTOLOGYMODEL_LIB_PATH` | `ONTOLOGYGRAPHMODEL_LIB_PATH` |
| 24 | Go build tag (all `go/internal/bridge/embed_*.go`, docs) | `ontologyspec_noembed` | `ontologygraphspec_noembed` |
| 25 | Proposal doc | `docs/ontology-spec-v1-proposal.md` | `docs/ontology-graph-spec-v1-proposal.md` |
| 26 | Bridge test fixture `$schema` string (`src/bridgeTest/kotlin/bridge/ValidationTest.kt`) | `https://neo4j.com/ontology-spec.schema.json` | `https://neo4j.com/ontology-graph-spec.schema.json` |
| 27 | Go model generator input (`go/scripts/generate-go-models.sh` `INPUT_SPEC`) | `ontology-spec.schema.json` | `ontology-graph-spec.schema.json` |
| 28 | CI path filters / messages (`.github/workflows/pr-guard-kotlin.yaml`, `pr-guard-go.yaml`, `validate-go.yaml`, `release.yaml` comments) | `ontology-spec.schema.json`, `ontology-spec` references | the row-1/row-20 names |
| 29 | Docs prose (README.md, AGENTS.md, go/README.md, `go/internal/bridge/lib/macos-arm64/README.md`) | "Ontology spec", the row-1/row-25 file names, rows 22–24 tokens | the new names, same passages |

Rows 27–29 are mechanical followers of rows 1–25: they carry no
independent naming decision, they exist so non-negotiable 3 (tooling
changes update the docs in the same change) and the CI guards keep
holding after the rename.

### 3. No aliases, no deprecation wrappers

The rename is clean: every old identifier disappears in the same change
that introduces its successor. No `typealias OntologySpec =
OntologyGraphSpec`, no deprecated `ToOntologySpec` shim, no dual
npm/Maven publication, no `ONTOLOGYMODEL_LIB_PATH` fallback. Evidence:
the four planning facts in Context — nothing v1 has shipped under the
current names (npm 404, no v1 tags, publish-maven `if: false`, no
`main` history for `OntologySpec.kt`), so there is no consumer to be
compatible with. ADR-0006's alias mechanism stays reserved for renames
of shipped 4.0.0 API, which this is not.

### 4. The discrimination rule: product-name identifiers rename; the domain word and history stay

**Renames:** every identifier that names *the product* — the schema
file and its `neo4j.com/ontology-spec/...` namespace, the
`ontology_spec` format token, the `OntologySpec*`/`Ontology*` product
entry points and test classes, the artifact coordinates, the
`ontologymodel` lib family, the proposal doc file name, and prose
mentions of the product ("the Ontology spec" → "the Ontology Graph
spec").

**Stays (domain-word "ontology" — the thing the format describes, not
the product):**

- `src/jvmTest/resources/ontology/` — the examples *are* ontologies;
  the directory, the `/ontology` classpath glob, and
  `SpecExamples.kt`'s `ontologyExamples()` stay.
- `*.ontology.json` example suffixes (`movies.ontology.json`,
  `org.ontology.json`, `foaf.ontology.json`).
- Domain prose: "Unique identifier of the ontology", "The ontology's
  own version" (schema field descriptions), "example ontologies", and
  every "the ontology" that means a document, not the product.
- `https://example.com/ontology-extensions/...` (in
  `org.ontology.json`) — a fictional *third party's* extension
  namespace, not ours; we no more rename it than we would a real
  external reference.
- Neutral model names that never carried the product name:
  `GraphModel`, the `model/`/`go/model` packages,
  `go/validation/testdata/invalid-graph-model.json`, `SpecExamples.kt`.

**Stays (historical records — immutable by rule or by nature):**

- ADR-0001…ADR-0008 bodies: accepted/proposed ADRs are immutable; they
  receive only a "Later references" note where §5 says so.
- `.gbuild` stores and git history.
- The `graph_spec` 4.0.0 input tokens — the converter's input contract
  names the *old* product accurately and must not move:
  `Type.GRAPH_SPEC = "graph_spec"`, `Version.GRAPH_SPEC_V4`, the
  `GraphSpecV4*` class-name prefix, the `migrate/migration/graphSpec/`
  package, `go/migration/testdata/graph-spec-example.json`, the
  `graph-spec-4.0.0` tag.
- The bridge C symbols `@CName("validate")` / `@CName("migrate")` —
  verbs, not product names (ADR-0008 §11).

### 5. Later-references notes

Per the immutability rule, the ADRs whose *decided surface* this rename
moves each get a "Later references" note pointing here, landing with
the rename execution:

- **ADR-0001** — its deferred repo rename anticipated
  `neo4j/ontology-spec`; the target is now `neo4j/ontology-graph-spec`
  (§1).
- **ADR-0003** — the hand-maintained source of truth it installed is
  renamed: `ontology-spec.schema.json` →
  `ontology-graph-spec.schema.json`; the hand-maintained rule itself is
  untouched.
- **ADR-0004** — its "Naming: mechanics per surface and timing" table
  is superseded name-for-name by §2 (every identifier it picked renames
  again); its sequencing and timing decisions stand.
- **ADR-0006** — consumer-visible delta (1), "the name — graph-spec
  becomes ontology-spec", now reads graph-spec → ontology-graph-spec;
  the two-delta limit and the API-stability sharpening stand.
- **ADR-0008** — its `Type.ONTOLOGY_SPEC = "ontology_spec"` output
  token, `GraphSpecV4OntologySpecMigration` class name, and §1 `$id`
  constant rename per §2 rows 6/9/2; the mapping semantics stand.

**No note** on ADR-0002 (rebase mechanics; naming was explicitly
deferred to ADR-0004 — no decided surface is renamed), ADR-0005
(extension packaging: the `neo4j:`/`ui:`/`importer:` named keys and the
custom envelope are untouched), or ADR-0007 (the harness location and
the networknt dependency decision stand; only the schema file path it
references follows row 1 mechanically).

## Alternatives considered

1. **Keep "Ontology Spec".** Rejected: the rename is a spec-lead
   decision already taken (ONT-24); this ADR's job is to record it and
   fix the mapping, not to relitigate it. "Ontology Graph Spec" also
   disambiguates the product from the domain word — the repo describes
   ontologies *and* graphs, and the old name collided with the former.
2. **"Graph Ontology Spec"** (the Linear issue title's original
   wording). Rejected: the spec lead corrected it — the decided name is
   Ontology Graph Spec, and recording the uncorrected wording would
   record a wrong answer.
3. **Rename with backward-compat aliases** (typealiases, deprecated Go
   shims, dual publication, `ONTOLOGYMODEL_LIB_PATH` fallback). Rejected:
   nothing v1 has shipped under the current names — npm 404 for
   `@neo4j-importer/ontology-spec`, no v1 git tags, publish-maven
   disabled with `if: false`, `OntologySpec.kt` absent from `main`'s
   history — so an alias would serve no consumer that exists, while
   doubling the API surface the first real release must then carry
   forever. Aliases cost exactly when they buy something; here they buy
   nothing.
4. **Leave the `ontologymodel` lib family unchanged** (baseName,
   `libontologymodel.*`, `ONTOLOGYMODEL_LIB_PATH`) while renaming
   everything else. Rejected: spec-lead decision — the rename is
   everywhere, and a native lib half-named after the old product is the
   worst of both worlds: consumer-visible (the env var and lib
   filenames are Go-consumer surface per ADR-0004) yet exempt from the
   story every other surface tells.
5. **Rename the domain-word uses too** (`resources/ontology/` →
   `resources/ontology-graph/`, "the ontology's own version" → "the
   ontology graph's own version", the `*.ontology.json` suffixes).
   Rejected: "ontology" there is the domain word — the format describes
   ontologies, the examples are ontologies — and renaming it would make
   the tree claim the format describes "ontology graphs", a term the
   spec prose never defines. Least surprise (design rule 2) cuts the
   other way: product renames, vocabulary stays.

## Consequences

- The rename executes against §2's table as one atomic change per
  surface area (schema + examples, Kotlin, Go, build/CI, docs), each
  landing with its gates green: `./gradlew jvmTest` (spec gate and
  conformance suites — rows 1–5, 11–14), `./gradlew check` and
  `spotlessCheck` (rows 6–10, 26), `cd go && go test ./...` with the
  renamed tag and env var (rows 15–18, 22–24), the Go model drift
  check after `generate-go-models.sh` (rows 5, 27).
- No alias or deprecation wrapper is added anywhere; the first v1
  release of every surface publishes under the new names only.
- The committed native libs under `go/internal/bridge/lib/` are
  rebuilt/renamed to `libontologygraphmodel.*` in the same change as
  the baseName rename (row 22); until then the Go bridge tests skip or
  use `ONTOLOGYGRAPHMODEL_LIB_PATH`, exactly as today's
  `ONTOLOGYMODEL_LIB_PATH` escape hatch works.
- ADR-0001/0003/0004/0006/0008 each gain a "Later references" note
  pointing here (§5); ADR-0002/0005/0007 are deliberately not touched.
- The deferred GitHub repo rename (ADR-0001, core-group decision) now
  targets `neo4j/ontology-graph-spec`; the Go module path's repo
  component and the npm `repository` URL follow it when it lands, as
  ADR-0004 already arranged.
- No format artefact changes: the wire shape, the schema's field
  semantics, and the examples' content are untouched — only names move
  — so non-negotiable 2 requires no new example and no proposal
  changelog entry beyond the rename of the proposal doc file itself
  (row 25); the proposal's own prose keeps saying "ontology" wherever
  it means the domain word (§4).
- The Linear team naming ("Ontology Spec Group", ONT-5) is outside this
  repo; ONT-24 tracks the rename programme this ADR anchors.
