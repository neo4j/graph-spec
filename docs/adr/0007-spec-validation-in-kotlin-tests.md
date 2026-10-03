# ADR-0007: Spec validation moves into the Kotlin test suites

Status: Accepted

## Context

The v1 overlay's validation harness predates the rebase onto `main`
(ADR-0002): `scripts/validate.mjs` (Ajv + ajv-formats) compiles
`ontology-spec.schema.json` against the 2020-12 meta-schema and
validates every `examples/*.{json,yaml}`; `scripts/generate-yaml.mjs`
generates the `*.ontology.yaml` forms from the JSON sources of truth;
`scripts/ttl2ontology.mjs` is the RDFS/OWL → ontology converter whose
deterministic output (`npm run convert && git diff --exit-code`) is a
second gate. The whole harness runs on Node >= 24 via npm, with
`package.json` / `package-lock.json` at the repo root and a dedicated CI
workflow (`.github/workflows/validate.yaml`). It was early-testing
scaffolding, and it works — but it is a second toolchain in a repo whose
implementation gates are `./gradlew check` and `go test`.

ADR-0003 already decided the direction: implementations **conform** to
the hand-maintained schema, and "the conformance tests are the
mitigation and are mandatory, not optional". It named
`scripts/validate.mjs` as the schema-side mechanism only because no
other harness existed at the time. The 4.0.0 tree the spec now coexists
with already holds the pattern this decision needs:
`src/jvmTest/resources/` keeps migration testdata
(`migrate/migration/dataModel`) that JVM tests load and check.

The spec lead has decided in conversation: the examples move into the
Kotlin test suites, `examples/` and `scripts/` are removed, and the npm
tooling goes with them. The spec-validation gate becomes a JVM test
validating the example resources against the root schema.

One genuinely new thing is required: a JSON Schema validator on the JVM.
The repo has none today — the `kotlinx-schema-generator-json` entry in
`gradle/libs.versions.toml` is a schema *generator* (4.0.0-only per
ADR-0003), not a validator. AGENTS.md's toolchain rule is explicit: "a
new dependency needs an ADR". This ADR is that ADR.

## Decision

- **(a) The validation harness moves from the npm/AJV scripts into the
  Kotlin test suites.** A JVM test (jvmTest) loads
  `ontology-spec.schema.json` from the repo root, compiles it against
  the draft 2020-12 meta-schema, and validates every example resource
  against it — the same checks `scripts/validate.mjs` performs today.
  The spec gate becomes `./gradlew jvmTest`, which `./gradlew check`
  already runs.
- **(b) `examples/` and `scripts/` are removed.** The hand-maintained
  JSON examples (movies, org, foaf) live on as test resources under
  `src/jvmTest/resources/ontology/`, following the existing
  `migrate/migration/dataModel` testdata pattern. They remain the
  source of truth for example content; only their location and their
  gate change.
- **(c) New dependency: `com.networknt:json-schema-validator`, jvmTest
  scope only**, admitted via `gradle/libs.versions.toml`.
  Justification: the schema targets JSON Schema draft 2020-12, which
  eliminates most of the JVM field — `org.everit.json:json-schema`
  stops at draft 7, `com.github.java-json-tools:json-schema-validator`
  is draft 4 and unmaintained, and the repo's existing `kotlinx-schema`
  dependency is a generator, not a validator. Of the two serious
  2020-12-capable candidates, `com.networknt:json-schema-validator` and
  `com.github.erosb:json-sKema`, networknt is the widely adopted,
  actively maintained option — the de-facto standard JVM validator
  across the Spring/OpenAPI ecosystem — with explicit 2020-12 support
  (`SpecVersion.VersionFlag.V202012`); json-sKema is a much smaller,
  younger project. jvmTest scope keeps the dependency out of every
  published artifact: it exists only to run the spec gate.
- **(d) The generated `*.ontology.yaml` forms are dropped.** They were
  a generated view of the JSON sources of truth produced by
  `scripts/generate-yaml.mjs`; with the generator gone they have no
  maintenance path, and hand-maintained YAML duplicates would violate
  one-wire-format (design rule 8). YAML returns when the v1 model lands
  (port track 1, ADR-0004) via the Kotlin kotlinx-serialization YAML
  codec, serialising the same model that reads the JSON — a library
  detail, not spec surface, exactly as design rule 8 intends.
- **(e) The npm/Node spec toolchain is removed with `scripts/`**:
  `package.json`, `package-lock.json`, and `node_modules` go, and
  `.github/workflows/validate.yaml` (the Node 24 spec gate in CI) is
  retired; the schema path folds into the Kotlin guard
  (`validate-kotlin.yaml`, which runs `./gradlew check`). Node remains
  in the repo only as the Kotlin/JS build's internal toolchain — an
  implementation detail of the Gradle build, not a spec surface.

This ADR supersedes ADR-0003's incidental naming of
`scripts/validate.mjs` as the schema-side conformance mechanism; the
conformance-tests requirement itself is unchanged and is what this move
implements.

## Alternatives considered

1. **Keep the npm gate alongside the Kotlin tests.** `npm run validate`
   would keep running in CI next to a new JVM test covering the same
   schema and examples. Rejected: two toolchains (Node 24 + npm +
   lockfile maintenance on one side, JDK + Gradle on the other) for one
   gate, with the duplicated checks free to disagree — the exact
   two-sources-of-truth failure ADR-0003's conformance tests exist to
   prevent, recreated at the tooling level. One gate, one toolchain.
2. **Validate via the not-yet-existing v1 Kotlin model.** Wait for port
   track 1 (ADR-0004) and validate the examples by round-tripping them
   through the v1 Kotlin model and its validators instead of adding a
   JSON Schema validator. Rejected: it blocks the harness move on the
   entire port track 1 schedule, keeping the npm scaffolding alive for
   the duration, and a model round-trip does not test the schema anyway
   — it tests the model's reading of the schema, which is ADR-0003's
   SDK-side conformance check, a different thing. The schema-side gate
   must validate documents against the schema itself, which is what a
   JSON Schema validator does; when the v1 model lands, its round-trip
   tests join this gate rather than replace it.
3. **Keep the examples as repo-root files without schema validation.**
   Retain `examples/` as documentation-only artefacts and drop the
   gate. Rejected: it violates AGENTS.md non-negotiable 4 ("No format
   feature without a validating example") — the validating example is
   the mechanism that keeps prose, schema, and examples from drifting,
   and Appendix B's undocumented 4.0.0 schema is the recorded cost of
   letting that slide. Examples without validation are wrong answers
   waiting to be recorded.
4. **Hand-roll a minimal JSON Schema checker in Kotlin commonTest.**
   Write a small in-repo validator covering only the keywords
   `ontology-spec.schema.json` uses, avoiding the new dependency.
   Rejected: draft 2020-12 semantics (`$ref`/`$defs` resolution,
   `pattern`, format assertions, annotation-dependent keywords) are
   easy to get subtly wrong, and a partial checker would pass documents
   the real meta-schema rejects — recording wrong answers, which
   non-negotiable 4 forbids. A maintained, spec-complete validator
   dependency is strictly safer than in-repo code for a gate whose
   entire job is correctness.

## Consequences

- The spec gate becomes `./gradlew jvmTest` (inside `./gradlew check`);
  the "How to run checks" table, the README commands, and AGENTS.md
  non-negotiable 4's `npm run validate` wording are updated in the
  implementing change. The converter-determinism gate
  (`npm run convert && git diff --exit-code`) is retired with the
  converter.
- CI folds the schema path into the Kotlin guard:
  `.github/workflows/validate.yaml` is removed and
  `validate-kotlin.yaml`'s `./gradlew check` becomes the single spec +
  implementation gate for the JVM/JS/Native surfaces. `validate-go.yaml`
  is untouched.
- Node remains in the repo only as the Kotlin/JS build's internal
  detail (the Gradle build's own toolchain); no contributor-facing
  command requires npm.
- The converter scripts are retired: `scripts/ttl2ontology.mjs` and its
  `foaf.ttl` input go with `scripts/` and `examples/`. The RDFS/OWL →
  ontology mapping table the converter implemented lives on in
  [`docs/ontology-spec-v1-proposal.md`](../ontology-spec-v1-proposal.md)
  as the design record; a Kotlin converter is port work, not spec
  scaffolding, and is charted there when needed.
- The new dependency is jvmTest-scoped: it appears in no published POM,
  no JS artifact, and no Go module. `gradle/libs.versions.toml` gains
  one entry; the dependency-admission rule (AGENTS.md toolchain: "a new
  dependency needs an ADR") is satisfied by this ADR.
- ADR-0003 stands unchanged in substance — the schema stays
  hand-maintained and implementations conform via CI tests; this ADR
  replaces only the named schema-side mechanism
  (`scripts/validate.mjs`) with the JVM test and records the
  replacement here.
- ADR-0004's port track 1 regains the YAML surface through the Kotlin
  YAML codec; nothing in the port sequencing changes, but track 1's
  scope now explicitly includes the examples-as-resources YAML
  round-trip.
