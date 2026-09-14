# Ontology spec v1 - JSON Schema + examples

Draft. Mirrors the shared proposal doc's 2026-09-14 state.

- `ontology-spec.schema.json` - JSON Schema (draft 2020-12) for the ontology format.
- `examples/` - example ontologies exercising the full surface.
- `scripts/validate.mjs` - validates the schema, then every example against it.

## Run

Requires Node >= 24.

```sh
npm install
npm run validate
```

## Notes

- Extensions validate the envelope only (`type` required; `$schema`, `name`, `definition` optional). `definition` is free-form by design and never validated here. Unknown extra fields on an extension are carried untouched.
- Composite constraint shape (`{ kind: key|unique, properties: [...] }`) is provisional, aligned with graph-spec 4.0.0's constraint object; the proposal names the construct but does not pin the shape yet.
- Example type names in extensions (`neo4j:index`, `neo4j-importer:table`, ...) are illustrative; their definitions are owned and versioned by their owners, not by this spec.
