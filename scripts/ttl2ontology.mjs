// Converts an RDFS/OWL Turtle vocabulary into an Ontology spec v1 document.
// Applies the proposal doc's RDFS coverage table; prints a coverage report.
// Usage: node scripts/ttl2ontology.mjs [input.ttl] [output.json]
import { readFileSync, writeFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { dirname, join } from "node:path";
import { createHash, randomUUID } from "node:crypto";
import { Parser } from "n3";

const root = join(dirname(fileURLToPath(import.meta.url)), "..");
const input = process.argv[2] ?? join(root, "examples", "foaf.ttl");
const output = process.argv[3] ?? join(root, "examples", "foaf.ontology.json");

const RDFS = "http://www.w3.org/2000/01/rdf-schema#";
const RDF = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
const OWL = "http://www.w3.org/2002/07/owl#";
const XSD = "http://www.w3.org/2001/XMLSchema#";

const quads = new Parser().parse(readFileSync(input, "utf8"));

const term = (q, p) => quads.filter((x) => x.subject.value === q && x.predicate.value === p).map((x) => x.object);
const first = (q, p) => term(q, p)[0]?.value;
const types = (q) => term(q, RDF + "type").map((o) => o.value);
const local = (uri) => uri.split(/[#/]/).pop();
const upperSnake = (s) => s.replace(/([a-z0-9])([A-Z])/g, "$1_$2").replace(/[^a-zA-Z0-9]+/g, "_").toUpperCase();

// The vocabulary's own namespace = namespace of the owl:Ontology subject.
const ontSubject = quads.find((q) => q.predicate.value === RDF + "type" && q.object.value === OWL + "Ontology")?.subject.value;
const ontNs = quads.find((q) => q.subject.value === ontSubject && q.predicate.value === RDF + "type")?.subject.value;
// ontSubject for foaf is "http://xmlns.com/foaf/0.1/" itself; its terms live under the same IRI prefix.
const NS = ontSubject?.endsWith("/") || ontSubject?.endsWith("#") ? ontSubject : ontSubject + "/";

const report = { nodes: [], properties: [], relationships: [], carried: [], dropped: [] };

// --- collect classes and properties in our namespace
const subjects = [...new Set(quads.map((q) => q.subject.value))].filter((s) => s.startsWith(NS) && s !== NS);
const isClass = (s) => types(s).some((t) => t === RDFS + "Class" || t === OWL + "Class");
const isDatatype = (s) => types(s).includes(OWL + "DatatypeProperty");
const isObject = (s) => types(s).includes(OWL + "ObjectProperty");
const isProp = (s) => types(s).includes(RDF + "Property") || isDatatype(s) || isObject(s);

const xsdToNeo4j = (t) => ({
  [XSD + "string"]: "STRING", [RDFS + "Literal"]: "STRING", [XSD + "anyURI"]: "STRING",
  [XSD + "integer"]: "INTEGER", [XSD + "int"]: "INTEGER", [XSD + "long"]: "INTEGER",
  [XSD + "float"]: "FLOAT", [XSD + "double"]: "FLOAT", [XSD + "decimal"]: "FLOAT",
  [XSD + "boolean"]: "BOOLEAN",
  [XSD + "date"]: "DATE", [XSD + "time"]: "TIME", [XSD + "dateTime"]: "DATETIME",
}[t]);

const classes = subjects.filter(isClass);
const classSet = new Set(classes);
const props = subjects.filter((s) => isProp(s) && !isClass(s));

// --- annotations carried as an extension (doc: seeAlso/isDefinedBy carried, not core)
const annotations = (s) => {
  const def = {};
  const label = first(s, RDFS + "label");
  const status = first(s, "http://www.w3.org/2003/06/sw-vocab-status/ns#term_status");
  const definedBy = first(s, RDFS + "isDefinedBy");
  const seeAlso = term(s, RDFS + "seeAlso").map((o) => o.value);
  if (label) def.label = label;
  if (status) def.term_status = status;
  if (definedBy) def.isDefinedBy = definedBy;
  if (seeAlso.length) def.seeAlso = seeAlso;
  return Object.keys(def).length ? { type: "rdfs:annotations", definition: def } : null;
};

// --- nodes
const nodes = {};
for (const c of classes) {
  const key = local(c);
  const entry = {};
  const comment = first(c, RDFS + "comment");
  if (comment) entry.description = comment;

  const implied = term(c, RDFS + "subClassOf").map((o) => o.value).filter((s) => classSet.has(s));
  const extSupers = term(c, RDFS + "subClassOf").map((o) => o.value).filter((s) => !classSet.has(s));
  if (implied.length) entry.labels = { implied: implied.map(local) };
  for (const s of extSupers) report.dropped.push(`${key}: subClassOf external ${s} (hierarchies excluded; only internal supers flatten to implied)`);

  const equiv = term(c, OWL + "equivalentClass").map((o) => o.value);
  if (equiv.length) entry.aliases = equiv.map((e) => e.replace(/^https?:\/\//, ""));
  for (const d of term(c, OWL + "disjointWith")) report.dropped.push(`${key}: disjointWith ${local(d.value)} (excluded)`);

  const ann = annotations(c);
  if (ann) entry.extensions = [ann];
  nodes[key] = entry;
  report.nodes.push(key);
}

// --- datatype properties -> node properties, object properties -> relationships
const relationships = {};
const unmapped = [];

for (const p of props) {
  const name = local(p);
  const comment = first(p, RDFS + "comment");
  const domains = term(p, RDFS + "domain").map((o) => o.value);
  const ranges = term(p, RDFS + "range").map((o) => o.value);
  const isFp = types(p).includes(OWL + "FunctionalProperty");
  const isIfp = types(p).includes(OWL + "InverseFunctionalProperty");
  if (types(p).includes(OWL + "SymmetricProperty")) report.dropped.push(`${name}: SymmetricProperty (excluded, inference-adjacent)`);
  if (term(p, RDFS + "subPropertyOf").length) report.dropped.push(`${name}: subPropertyOf (hierarchies excluded)`);

  const ann = annotations(p);
  const exts = ann ? [ann] : [];

  const objectish = isObject(p) || ranges.some((r) => classSet.has(r) || (!xsdToNeo4j(r) && r !== RDFS + "Literal"));

  if (!objectish) {
    // datatype property -> property on each domain class (v1: no domain list; expand per class)
    const neo4jType = ranges.length ? xsdToNeo4j(ranges[0]) : "STRING";
    if (ranges.length && !xsdToNeo4j(ranges[0])) report.dropped.push(`${name}: range ${ranges[0]} mapped to STRING (fallback)`);
    if (!ranges.length) report.dropped.push(`${name}: no range declared, typed STRING (fallback)`);
    if (isFp) report.dropped.push(`${name}: FunctionalProperty (v1 has no single-valued flag for properties)`);

    let targets = domains.filter((d) => classSet.has(d));
    if (domains.includes(OWL + "Thing")) {
      targets = classes; // owl:Thing domain: applies to every converted class
      report.carried.push(`${name}: owl:Thing domain expanded onto all ${classes.length} classes`);
    }
    if (!targets.length) {
      unmapped.push({ property: name, reason: "no in-vocabulary domain", range: ranges.map((r) => local(r))[0], description: comment });
      report.dropped.push(`${name}: no in-vocabulary domain, carried in rdf:unmapped extension`);
      continue;
    }
    for (const d of targets) {
      const node = nodes[local(d)];
      node.properties ??= {};
      const spec = { type: neo4jType };
      if (isIfp) spec.unique = true; // IFP: value identifies the subject -> unique
      if (comment) spec.description = comment;
      if (exts.length) spec.extensions = exts;
      node.properties[name] = spec;
    }
    report.properties.push(`${name} -> ${targets.map(local).join(", ")} (${neo4jType}${isIfp ? ", unique" : ""})`);
  } else {
    // object property -> one relationship entry per (domain, range) pair (v1: single from/to)
    let doms = domains.filter((d) => classSet.has(d));
    if (domains.includes(OWL + "Thing")) {
      doms = classes;
      report.carried.push(`${name}: owl:Thing domain expanded onto all ${classes.length} classes`);
    }
    const rangesOk = ranges.filter((r) => classSet.has(r));
    for (const r of ranges.filter((r) => !classSet.has(r))) {
      if (r === OWL + "Thing") report.dropped.push(`${name}: range owl:Thing (unconstrained target not expressible in v1)`);
      else report.dropped.push(`${name}: range external ${r} (not converted)`);
    }
    if (!doms.length || !rangesOk.length) {
      unmapped.push({ property: name, reason: "no in-vocabulary domain or range", domain: domains.map(local), range: ranges.map(local), description: comment });
      report.dropped.push(`${name}: no in-vocabulary domain/range pair, carried in rdf:unmapped extension`);
      continue;
    }
    const relType = upperSnake(name);
    const card = isFp && isIfp ? "ONE_TO_ONE" : isFp ? "MANY_TO_ONE" : isIfp ? "ONE_TO_MANY" : undefined;
    for (const d of doms) {
      for (const r of rangesOk) {
        const key = doms.length > 1 || rangesOk.length > 1 ? `${relType}__${upperSnake(local(d))}__${upperSnake(local(r))}` : relType;
        const entry = { from: { node: local(d) }, to: { node: local(r) } };
        if (card) entry.cardinality_type = card;
        if (comment) entry.description = comment;
        const label = first(p, RDFS + "label");
        if (label && label !== relType) entry.aliases = [label];
        if (exts.length) entry.extensions = exts;
        relationships[key] = entry;
        report.relationships.push(`${key}: ${local(d)} -> ${local(r)}${card ? ` (${card})` : ""}`);
      }
    }
  }
}

// --- assemble
const extensions = [
  {
    type: "rdf:source",
    name: "foaf",
    definition: {
      namespace: NS,
      note: "Original element URIs are namespace + map key. Preserved per the URI/namespace discussion; static identity, not lineage.",
    },
  },
];
if (unmapped.length) extensions.push({ type: "rdf:unmapped", definition: { properties: unmapped } });

const doc = {
  $schema: "https://neo4j.com/ontology-spec/1.0.0/schema.json",
  id: createHash("sha256").update(NS).digest("hex").slice(0, 32).replace(/^(.{8})(.{4})(.{4})(.{4})(.{12})$/, "$1-$2-$3-$4-$5"),
  version: 1,
  name: "foaf",
  description: first(ontSubject, "http://purl.org/dc/elements/1.1/description") ?? "Friend of a Friend vocabulary, converted from RDF",
  nodes,
  relationships,
  extensions,
};

writeFileSync(output, JSON.stringify(doc, null, 2) + "\n");

console.log(`converted ${input.split("/").pop()} -> ${output.split("/").pop()}`);
console.log(`  nodes: ${report.nodes.length} (${report.nodes.join(", ")})`);
console.log(`  datatype properties mapped: ${report.properties.length}`);
console.log(`  relationship entries: ${report.relationships.length}`);
if (report.carried.length) console.log(`  expanded: ${report.carried.join("; ")}`);
console.log(`  dropped/deferred (${report.dropped.length}):`);
for (const d of report.dropped) console.log(`    - ${d}`);
