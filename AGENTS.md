# AGENTS.md

The Neo4j Ontology Graph Specification v1 + its SDK (Kotlin Multiplatform
model, JS/TS surface, Go module). `ontology-graph-spec.schema.json` (draft
2020-12) is the hand-maintained source of truth; implementations conform to
it (ADR-0003).
The port from graph spec 4.0.0 and its decisions are recorded in
[`docs/adr/`](docs/adr/) (0001–0009); the design record up to 2026-09-25 is
[`docs/ontology-graph-spec-v1-proposal.md`](docs/ontology-graph-spec-v1-proposal.md).

## Non-negotiables

1. **ADR first.** Every change to what a document may say or mean (fields,
   semantics, cardinalities, type system, extension mechanism, interop,
   governance) gets an ADR before implementation: `docs/adr/NNNN-slug.md`,
   sections in order Context / Decision / Alternatives considered (each ends
   in why it was rejected) / Consequences. Status `Proposed`; `Accepted` only
   when agreed. Accepted ADRs are immutable — a later change is a new ADR +
   a **Later references** note on the old one. Next number = highest existing
   + 1; list the directory, don't guess. Typos and meaning-preserving wording
   fixes are the only exemption. When in doubt, write the ADR.
2. **Prose + schema + example in the same change.** No schema field without
   prose, no prose without schema, `description` on every schema field, and
   every format feature exercised by at least one example under
   `src/jvmTest/resources/ontology/`. Never record a wrong answer: if an
   example only passes because the schema is wrong, fix the schema.
3. **Tooling changes update the docs in the same change.** Every CI workflow
   and public Gradle task ships documented usage (README.md + this file):
   what it does, inputs/outputs, flags with defaults, env vars, one
   copy-paste invocation.
4. **The gates stay green.** See the table below; a change lands only with
   its gates passing.

## Commands (the feedback loop)

Gradle needs JDK 17+; if system JDK here is different, prefix every `./gradlew`
with `JAVA_HOME=<compatible-java-path>`
(CI has 17; any 17+ works).

| What | Command |
| --- | --- |
| Spec gate: schema compiles, every example validates | `./gradlew jvmTest` (class `spec.OntologyGraphSpecExamplesTest`) |
| Model conformance: examples round-trip JSON + YAML through the model | same run (`spec.OntologyGraphModelRoundTripTest`, `spec.OntologyGraphYamlRoundTripTest`) |
| Kotlin: JVM + JS/TS + Native compile, all tests | `./gradlew check` |
| Lint (ktlint via spotless + license header) | `./gradlew spotlessCheck` (fix: `./gradlew spotlessApply`) |
| Go | `cd go && go test ./...` (bridge tests skip if no native lib; force-exclude: `-tags ontologygraphspec_noembed`) |
| Go model drift | `./go/scripts/generate-go-models.sh && git diff --exit-code` |
| JS distribution (npm package + `.d.mts`) | `./gradlew jsNodeProductionLibraryDistribution` |

Notes:

- The spec gate also runs inside `./gradlew check` (jvmTest is part of it).
- CI mirrors this table: `pr-guard-kotlin.yaml` (`src/**` or the schema →
  `check spotlessCheck`), `pr-guard-go.yaml` (`go/**` or the schema → go test
  + drift check), `release.yaml` (merged PRs with `release:*` labels →
  validate → npm publish + native-libs + tags; `publish-maven` stays disabled
  pending a release-policy decision).
- Go bridge tests load the committed Kotlin/Native libs
  (`go/internal/bridge/lib/`). The macOS dylib needs full Xcode to build
  (`./go/scripts/generate-kotlin-native-libs.sh`; this host is CLT-only) —
  linux libs build anywhere; `ONTOLOGYGRAPHMODEL_LIB_PATH` overrides the lib path;
  `ontologygraphspec_noembed` builds without the embedded lib.
- `generate-go-models.sh` runs schemancer (+ jq/perl for the sanitised copy
  and the extras injection) against the repo-root schema — no Gradle, no JDK.
  Needs Go, jq, perl, schemancer.

## Repo patterns (where things go)

- `src/commonMain/kotlin/model/` — the v1 model, field-for-field with the
  schema (`@SerialName` for `one_of`/`min_count`/`max_count`/`constraint_type`/
  `$schema`). Open surfaces (`Tool`, `CustomExtension`) keep unknown fields
  via the catch-all helpers in `model/extension/InlineExtras.kt`; the six-kind
  `ExtensionValue` tree is the payload carrier (raw-JSON
  `ExtensionValueSerializer`). Named extensions live in owner-prefixed
  subpackages of `model/extension/` (`neo4j/index/`, `neo4j_importer/table/`…)
  per ADR-0005 — land with their owners.
- `src/commonMain/kotlin/codec/` — `format/` (JSON + YAML, one wire format;
  YAML inline paths live in `YamlFormat.kt`), `schema/` (the `SchemaMap` tree,
  the converter's transform substrate).
- `src/commonMain/kotlin/validate/` — one `object` per rule implementing
  `Validation`, each with its own `commonTest` class, KDoc citing the
  schema/proposal line it enforces. Traversal and issue-path grammar are owned
  once by `validate/ModelWalk.kt` — never re-derive a path in a validator.
  Groups live in `Validations.kt` (`core`, `all`, `namedExtensions`; the
  4.0.0 UPX group names alias `core` for API stability).
- `src/commonMain/kotlin/migrate/` — the `Migration`/`MigrationPath`
  machinery; the one-way 4.0.0 → 1.0.0 converter is
  `migrate/migration/graphSpec/` (ADR-0008).
- `src/jsMain/` + `src/jsTest/` — `@JsExport` twins + editors mirroring
  commonMain (Kotlin/JS's `generateTypeScriptDefinitions` ships the `.d.mts`;
  no post-processor). Renamed-away API survives as `typealias` + delegating
  factory (ADR-0006), e.g. `NodeConstraintJs = ConstraintJs`.
- `src/bridge/` + `src/bridgeTest/` — the Kotlin/Native exports Go calls
  (`@CName("validate")`, `@CName("migrate")`).
- `src/jvmTest/kotlin/spec/` — the spec gate and conformance suites.
  `SpecExamples.kt` owns the `/ontology` classpath glob
  (`ontologyExamples()`); test classes never re-implement it.
- `src/jvmTest/resources/ontology/` — the examples, hand-maintained JSON.
- `go/` — `model/model.go` is GENERATED by `go/scripts/generate-go-models.sh`
  (never hand-edit; the drift check enforces it); `model/extras.go` is the
  hand-written open-surface half; `internal/bridge/` loads the native lib;
  `validation/` + `migration/` bridge into Kotlin.
- Every Kotlin file carries the header from `license-header.txt`
  (spotless enforces it).
- Work is tracked in Linear under ONT (team Ontology Spec Group).

## Changing the format

1. ADR first — no exceptions.
2. Prose: the spec document (today: the proposal doc), dated changelog entry
   naming the ADR.
3. Schema: `ontology-graph-spec.schema.json`, `description` on every new field.
4. Examples: add or extend one under `src/jvmTest/resources/ontology/`.
5. `./gradlew jvmTest` green; if the model/codecs/validators are affected,
   they and their tests change in the same change (conformance suites must
   stay green).

## Branching

- Rebase, never merge. Update branches with `git rebase main`.
- Stack multi-PR work: `main → b1 → b2`, each PR against its predecessor.
- Before push: the gates from the table above that your change touches.

## Design rules (the format's constitution — a change violating one is a format break; ADR it)

1. Minimal and pragmatic: partial RDFS + SHACL, not RDF, not OWL.
2. Least surprise: object orientation to an enterprise developer, not RDF.
3. Schema optional: descriptive first, enforcing over time.
4. No RDF inference. 
5. Interop with RDF tooling and industry standards.
6. Bottom-up and top-down creation both supported.
7. Clean break from 4.0.0 — the converter is the migration story.
8. One wire format; normalisation is a library detail.
9. Extensions are typed objects: first-party as `neo4j:`-prefixed named keys;
   custom under the `custom` envelope (`type` required; `$schema`/`name`/
   `definition` optional; `definition` free-form, never validated). The
   envelope always validates; unknown content is carried untouched.
