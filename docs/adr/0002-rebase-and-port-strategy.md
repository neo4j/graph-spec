# ADR-0002: Rebase-and-port strategy

Status: Accepted

## Context

This repository is `github.com/neo4j/graph-spec`. Its `main` branch (tip
`c61fbae`) holds graph spec 4.0.0: a Kotlin Multiplatform model, Go
bindings, a Gradle build, and five CI workflows. The branch
`ontology-spec-v1-jsonschema` holds the new Neo4j Ontology Specification
v1 — a JSON Schema (draft 2020-12), validating examples, and Node-based
tooling — on what was an unrelated history.

ADR-0001 unified the two histories with a
`git merge --allow-unrelated-histories -s ours` merge (commit `99d204f`
and its landing merge `737debf`) and deferred the SDK/converter port to
later workstreams. The proposal doc
([`docs/ontology-spec-v1-proposal.md`](../ontology-spec-v1-proposal.md))
records v1 as a clean break from 4.0.0, with a one-way 4.0.0 → 1.0.0
converter as the migration story.

The spec lead has now reversed that strategy before anything reached a
remote: instead of the unification merge, the branch will be rebased
onto `main` (linear history; the merge commits get dropped), the full
old implementation tree stays present, and the port of the
Kotlin/Go/TypeScript implementation to the ontology spec happens in this
repo now.

## Decision

- **Rebase onto `main` replaces the `-s ours` unification merge.** The
  merge commits `99d204f` and `737debf` will be dropped by the rebase
  and will never reach a remote. The branch lands on `c61fbae` with a
  linear history, per AGENTS.md's "rebase, never merge" rule — no
  exception is needed.
- **The SDK/converter port comes into scope now.** The port of the
  Kotlin/Go/TypeScript implementation (including the 4.0.0 → 1.0.0
  converter) to the ontology spec happens in this repository, superseding
  ADR-0001's deferral of that work.
- **The endgame is renaming this branch to `main` locally.** Every
  remote operation — push, pull request, GitHub settings, remote rename —
  is out of scope for this workstream.
- **API stability.** Existing SDK public APIs (Kotlin, Go, TypeScript)
  stay the same to the largest extent possible through the port.
  Extensions are the named exception: their current validation exposure
  is not fully known, and a follow-up ADR-0005 will decide their
  packaging.
- **Extension packaging principle.** Each named extension
  (`neo4j:index`, `neo4j:display`, `neo4j-importer:table`,
  `neo4j-importer:mapping`) gets its own folder or file with clear
  filesystem separation, while remaining part of the main SDK
  distribution. Details are deferred to ADR-0005.
- **Plumbing reconciliation for the coexistence period.** The rebased
  tree holds both the old implementation and the ontology overlay.
  `.gitignore` regains the Gradle/Kotlin/IntelliJ/macOS blocks alongside
  `node_modules/` (an earlier commit had stripped them for a spec-only
  repo), and `.editorconfig` keeps `indent_size = 2` as the default with
  a 4-space override section for Kotlin files (`[{*.kt,*.kts}]`).

## Alternatives considered

1. **Keep the `-s ours` unification merge and PR into `main`
   (ADR-0001's mechanic).** Rejected: it leaves the old tree
   deleted-by-merge and the SDK port homeless, and the rebase gives a
   linear, reviewable history.
2. **Delete the old implementation tree immediately on rebase.**
   Rejected: the port needs it as reference, and the API-stability
   constraint means much of it survives.
3. **Rename packages/modules to `ontology-*` immediately.** Rejected:
   it conflicts with API stability; naming is weighed in ADR-0004
   instead.

## Consequences

- The branch has a linear history on `c61fbae`; the `-s ours` merge
  commits `99d204f` and `737debf` are dropped and never reach a remote.
- Both trees — the old implementation and the ontology overlay — coexist
  in the repo until the port tracks land.
- ADR-0001's `-s ours` merge mechanic and its deferral of the
  SDK/converter port are superseded by this ADR; the rest of ADR-0001
  stands.
- ADR-0003 (schema source of truth), ADR-0004 (port sequencing), and
  ADR-0005 (extension packaging) follow as Proposed ADRs; the port
  tracks are charted after those land.

## Later references

- ADR-0006 sharpens this ADR's API-stability constraint: "stay the same
  to the largest extent possible through the port" becomes the concrete
  rule that consumer-visible changes are limited to the name and the
  extensions area.
