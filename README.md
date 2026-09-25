# Ontology spec v1 - JSON Schema + examples

Draft. Mirrors the shared proposal doc's 2026-09-25 state.

- `ontology-spec.schema.json` - JSON Schema (draft 2020-12) for the ontology format.
- `examples/` - example ontologies exercising the full surface, plus `foaf.ttl` and its converted output.
- `scripts/validate.mjs` - validates the schema, then every example (JSON + YAML) against it.
- `scripts/ttl2ontology.mjs` - converts an RDFS/OWL Turtle vocabulary to the ontology format, applying the proposal's RDFS coverage table. Prints what mapped, what expanded, what was dropped and why.

## Run

Requires Node >= 24.

```sh
npm install
npm run validate    # schema + all examples (regenerates YAML first)
npm run convert     # examples/foaf.ttl -> examples/foaf.ontology.json
```

## Notes

- Nodes and relationships are keyed by local ids; the label lives in `label` / `labels.identifier`, the relationship type in `type`. Endpoint `node` references point at node ids. A relationship type shared across endpoint pairs sits under one id key per pair (allowed, but graph-type enforcement keys on the type alone and cannot distinguish pairs yet).
- Property types are tokens (`STRING`, `LIST<STRING>`, `LIST<ANY>`, `VECTOR<FLOAT>` + companion `dimension` field). No union types: a property is a single type or `ANY`.
- Relationship cardinality lives on the endpoints: `count` (exact) / `min_count` / `max_count`; absent = unconstrained.
- `tools` is a core field on node and relationship entries: `{ type: canonicalQuery | externalRequest | ..., name, description, ... }`, per-type shapes owner-defined.
- `extensions` at every level: first-party extensions as `neo4j:`-prefixed named keys with owner-defined shapes (not validated here), custom extensions as the fixed envelope under `custom` (`type` required; `$schema`, `name`, `definition` optional; `definition` free-form, never validated). Unknown extra fields are carried untouched.
- Constraint objects (`{ constraint_type: key|unique|mustExist, name?, properties: [...] }`) are the nameable alternative to the property shorthand flags.
