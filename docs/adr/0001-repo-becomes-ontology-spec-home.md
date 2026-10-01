# ADR-0001: Repo becomes the ontology spec home

Status: Accepted

## Context

This repository is `github.com/neo4j/graph-spec`. Its `main` branch (tip
`c61fbae`) holds graph spec 4.0.0: a Kotlin Multiplatform model, Go
bindings, a Gradle build, and Kotlin/Go CI workflows — 580 commits of
history.

The branch `ontology-spec-v1-jsonschema` holds the new Neo4j Ontology
Specification v1: a JSON Schema (draft 2020-12), validating examples,
and Node-based validation tooling. It sits on a history completely
unrelated to `main`'s.

The proposal doc ([`docs/ontology-spec-v1-proposal.md`](../ontology-spec-v1-proposal.md))
decides that v1 is a clean break from 4.0.0: no backwards compatibility
in the format, with a one-way 4.0.0 → 1.0.0 converter as the migration
story. The October deliverable is a written spec, not a patch of the old
one (proposal, Appendix B). That deliverable needs a home, and the two
unrelated histories in one repo make a normal pull request from the
ontology branch into `main` impossible.

## Decision

- `main` becomes the ontology spec home. The ontology spec's tree
  replaces the graph spec 4.0.0 tree on `main`.
- Graph spec 4.0.0 is frozen at the tag `graph-spec-4.0.0` pointing at
  `c61fbae`. Tag only — no maintenance branch.
- The unrelated histories are unified by merging `main` into the
  ontology branch with `git merge --allow-unrelated-histories -s ours`
  (tree unchanged), so a clean PR from the branch into `main` becomes
  possible. This merge commit is the sanctioned one-time exception to
  AGENTS.md's "rebase, never merge" rule.
- The Kotlin/Go/Gradle tree and its five CI workflows are not ported.
  Ported from `main` are only: `LICENSE`, a trimmed `.editorconfig`,
  and a merged `.gitignore` — plus a new Node-based validate workflow.
- Deferred to later workstreams, each with its own ADR: the
  4.0.0 → 1.0.0 converter, the written spec document, the
  release/publish machinery, the SDK, and the repo rename
  (`neo4j/ontology-spec` — pending the core group, proposal decision #1).

## Alternatives considered

1. **Reset `main` to the ontology branch and force-push.** This would
   put the ontology tree on `main` directly, with no merge commit.
   Rejected: `main`'s 580-commit history stops being reachable from
   `main`, and force-pushing `main` needs a protection-rule bypass. The
   `-s ours` merge keeps full ancestry reachable and lands on `main`
   with a normal fast-forward.
2. **A separate new repo `neo4j/ontology-spec`.** This would give the
   ontology spec a clean home with no history surgery at all. Rejected
   for now: the repo question is still an open core-group decision
   (proposal decision #1), and unifying in place keeps one home for the
   work. A later rename remains possible, since git history carries
   over.

## Consequences

- The Kotlin/Go CI workflows vanish from `main`'s tree. GitHub required
  checks must be repointed to the new Node validate workflow at PR time.
- The local tag `graph-spec-4.0.0` at `c61fbae` preserves graph spec
  4.0.0 in full; it remains reachable and checkable-out, but receives no
  further maintenance.
- The unification merge commit is the one-time exception to the
  "rebase, never merge" rule; every branch after it returns to linear,
  rebase-only history.
- The converter, the written spec document, the release/publish
  machinery, the SDK, and the repo rename each land in their own
  workstream with their own ADR before implementation.

## Later references

- ADR-0002 supersedes (i) the `-s ours` unification merge mechanic
  recorded here and (ii) the deferral of the SDK/converter port to later
  workstreams.
