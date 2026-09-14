// Validates the ontology spec JSON Schema itself, then every example against it.
// Usage: npm run validate
import { readFileSync, readdirSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";
import Ajv2020 from "ajv/dist/2020.js";
import addFormats from "ajv-formats";

const root = join(dirname(fileURLToPath(import.meta.url)), "..");
const schemaPath = join(root, "ontology-spec.schema.json");
const examplesDir = join(root, "examples");

const major = process.versions.node.split(".")[0];
if (Number(major) < 24) {
  console.error(`Node >= 24 required, running ${process.versions.node}`);
  process.exit(2);
}

const schema = JSON.parse(readFileSync(schemaPath, "utf8"));

const ajv = new Ajv2020({ strict: true, allErrors: true });
addFormats(ajv);

// 1. Schema validates against the 2020-12 meta-schema and compiles.
ajv.validateSchema(schema);
const validate = ajv.compile(schema);
console.log(`schema: OK (${schemaPath.split("/").pop()})`);

// 2. Every examples/*.json validates against the schema.
let failed = 0;
const files = readdirSync(examplesDir).filter((f) => f.endsWith(".json")).sort();
for (const file of files) {
  const doc = JSON.parse(readFileSync(join(examplesDir, file), "utf8"));
  const ok = validate(doc);
  if (ok) {
    console.log(`example: OK (${file})`);
  } else {
    failed++;
    console.error(`example: FAIL (${file})`);
    for (const err of validate.errors) {
      console.error(`  ${err.instancePath || "/"} ${err.message} (${JSON.stringify(err.params)})`);
    }
  }
}

if (failed > 0) {
  console.error(`${failed} example(s) failed`);
  process.exit(1);
}
console.log(`all good: schema + ${files.length} examples`);
