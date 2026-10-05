# ADR-0012: VECTOR element types are numeric

Status: Proposed

## Context

The v1 `propertyType` pattern
(`ontology-graph-spec.schema.json` `$defs.propertyType`, generated from
`model/property/Property.kt`'s `@SpecPattern`) admitted any scalar as a
VECTOR element type:
`(LIST|VECTOR)<(STRING|INTEGER|FLOAT|BOOLEAN|DATE|TIME|LOCALTIME|DATETIME|LOCALDATETIME|DURATION|POINT|BYTES)>`.
That over-generalises: Neo4j vectors are fixed-dimension arrays of
numbers, so `VECTOR<TIME>`, `VECTOR<STRING>`, `VECTOR<BOOLEAN>`, etc.
are tokens the schema accepted that no Neo4j value can ever satisfy.
PR #118 review comment r4182676002 (GregHib, relaying Greg K) flagged
exactly this: "this is incorrect, VECTOR<TIME> isn't valid etc... The
existing enum structure was fine and only missing LIST<ANY>".

4.0.0 never had this problem: its `Neo4jType` enum listed the legal
vectors exhaustively (`VECTOR<FLOAT32>`, `VECTOR<FLOAT>`,
`VECTOR<INTEGER8>`, `VECTOR<INTEGER16>`, `VECTOR<INTEGER32>`,
`VECTOR<INTEGER>`) — all numeric. v1's simplified type system widens
every 4.0.0 float width to `FLOAT` and every integer width to `INTEGER`
(ADR-0008 §3), so the numeric element types in v1 are exactly `INTEGER`
and `FLOAT`. The LIST arm is correct as it stands: a list of any scalar
is a valid Neo4j value, and 4.0.0 had them all.

## Decision

**VECTOR element types are numeric in v1: the `propertyType` pattern's
VECTOR arm becomes `VECTOR<(INTEGER|FLOAT)>`.** The rest of the pattern
is unchanged: `ANY`, `LIST<ANY>`, the twelve bare scalars, and
`LIST<scalar>` for every scalar. The change lands in the usual trio:

- Schema: `Property.kt`'s `@SpecPattern` re-splits the
  `(LIST|VECTOR)<...>` arm into `LIST<(scalar)>` and
  `VECTOR<(INTEGER|FLOAT)>`, and its `@SpecDoc` records the numeric
  restriction; `ontology-graph-spec.schema.json` is regenerated, never
  hand-edited (ADR-0003).
- Prose: the proposal's Data types section states VECTOR takes numeric
  element types only, with a dated changelog entry naming this ADR.
- Model validation: `validate/property/PropertyTypeToken.kt`'s
  `TYPE_TOKEN` regex stays character-identical to the schema pattern
  (the VECTOR arm composes from a numeric-subset const), and its test
  rejects `VECTOR<TIME>`/`VECTOR<STRING>` while accepting
  `VECTOR<INTEGER>`/`VECTOR<FLOAT>`.

The 4.0.0 → 1.0.0 converter is unaffected: every 4.0.0 vector token is
numeric, so the ADR-0008 §3 mapping table needs no new rows — only its
prose description of the v1 pattern is amended (that ADR is still
Proposed, so the amendment is in place). Every example in the repo
already uses `VECTOR<FLOAT>`, which stays valid.

## Alternatives considered

1. **Keep `VECTOR<scalar>` for every scalar** (status quo). Rejected:
   the schema would keep pronouncing `VECTOR<TIME>`-class tokens valid
   that no Neo4j value can satisfy — a wrong answer recorded in the
   format's most-checked artefact, and exactly what PR #118 review
   comment r4182676002 flagged. Design rule 2 (least surprise) cuts
   against blessing tokens the database rejects.
2. **Restore 4.0.0's width-qualified vector tokens**
   (`VECTOR<FLOAT32>`, `VECTOR<INTEGER8>`, ...). Rejected: v1
   deliberately dropped the width tokens and widens them all to
   `FLOAT`/`INTEGER` (ADR-0008 §3; proposal 2026-09-25 token decision);
   reintroducing widths only inside VECTOR would fork the type system
   into "wide scalars, narrow vector elements" and strand the
   converter's widening rows.
3. **Drop VECTOR from the token grammar entirely** and model vectors as
   `LIST<FLOAT>` + `dimension`. Rejected: vectors are a distinct Neo4j
   type with a distinct storage and index story (vector indexes,
   `dimension` companion); collapsing them into lists would lose the
   type distinction the token exists to carry, and 4.0.0's VECTOR
   tokens would have no mapping target.
4. **Leave the schema permissive and reject non-numeric vectors only in
   the model validators.** Rejected: the type-token grammar is
   schema-enforced today (README "Validation levels": the type-token
   pattern lives in the JSON Schema layer, mirrored model-side as a
   safety net); pushing a grammar fact into the model-only layer would
   let schema-valid documents carry meaningless tokens and would split
   one rule across two layers for no gain.

## Consequences

- `ontology-graph-spec.schema.json` `$defs.propertyType.pattern` gains
  `VECTOR<(INTEGER|FLOAT)>`; documents using `VECTOR<TIME>`,
  `VECTOR<STRING>`, `VECTOR<BOOLEAN>`, `VECTOR<DATE>`,
  `VECTOR<LOCALTIME>`, `VECTOR<DATETIME>`, `VECTOR<LOCALDATETIME>`,
  `VECTOR<DURATION>`, `VECTOR<POINT>`, or `VECTOR<BYTES>` stop being
  schema-valid. No example in the repo uses any of those, so the spec
  gate is unaffected.
- The Kotlin model validator `PropertyTypeToken` rejects the same
  tokens model-side, keeping its regex character-identical to the
  schema pattern.
- ADR-0008 §3's prose is amended in place (it is Proposed): the v1
  pattern description no longer says bare `VECTOR<scalar>`; the token
  mapping table is unchanged — all 4.0.0 vector tokens were already
  numeric.
- The Go model embeds the `propertyType` description text (not the
  pattern), so `go/model/model.go` is regenerated in the same change to
  pick up the amended `@SpecDoc`.
- This is a tightening of the format's type system — a format change,
  hence this ADR before implementation (non-negotiable 1).
