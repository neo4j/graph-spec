// Generates examples/*.yaml from examples/*.json. JSON is the source of truth.
// Runs automatically before `npm run validate` (prevalidate hook).
import { readFileSync, writeFileSync, readdirSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";
import { stringify } from "yaml";

const root = join(dirname(fileURLToPath(import.meta.url)), "..");
const examplesDir = join(root, "examples");

const files = readdirSync(examplesDir).filter((f) => f.endsWith(".json")).sort();
for (const file of files) {
  const doc = JSON.parse(readFileSync(join(examplesDir, file), "utf8"));
  const out = file.replace(/\.json$/, ".yaml");
  writeFileSync(join(examplesDir, out), stringify(doc, { indent: 2, lineWidth: 0 }));
  console.log(`generated ${out}`);
}
