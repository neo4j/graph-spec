/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package migrate

import OntologyGraphSpec
import codec.format.JsonFormat
import codec.schema.SchemaMap
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import migrate.migration.graphSpec.GraphSpecV4OntologyGraphSpecMigration
import model.Type
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Focused assertions for the ADR-0008 mapping table: the §3 token table (all 38 rows,
 * each lossy row spot-asserted), the §1 root mapping, the §6 flag-vs-object rule, the
 * §5 endpoint rule, the §8 extension payloads, the §9 untag + named-vs-custom rule, and
 * the §10 rejection of pre-4.0.0 inputs. Corpus-level conversion, schema validation and
 * pretty/internal convergence live in [GraphSpecV4CorpusTest].
 */
class GraphSpecV4OntologyGraphSpecMigrationTest {

    private val migration = GraphSpecV4OntologyGraphSpecMigration()
    private val format = JsonFormat.default

    private fun convert(json: String): SchemaMap = migration.migrate(format.decodeFromString(json) as SchemaMap)

    private fun properties(out: SchemaMap, node: String): SchemaMap =
        out.map("nodes").map(node).map("properties")

    @Test
    fun `all 38 Neo4jType tokens map per the ADR-0008 table`() {
        val expected = linkedMapOf(
            "ANY" to "ANY",
            "BOOLEAN" to "BOOLEAN",
            "LIST<BOOLEAN>" to "LIST<BOOLEAN>",
            "DATE" to "DATE",
            "LIST<DATE>" to "LIST<DATE>",
            "DURATION" to "DURATION",
            "LIST<DURATION>" to "LIST<DURATION>",
            "FLOAT32" to "FLOAT", // lossy: 32-bit precision class dropped
            "LIST<FLOAT32>" to "LIST<FLOAT>", // lossy
            "FLOAT" to "FLOAT",
            "LIST<FLOAT>" to "LIST<FLOAT>",
            "INTEGER8" to "INTEGER", // lossy: width dropped
            "LIST<INTEGER8>" to "LIST<INTEGER>", // lossy
            "INTEGER16" to "INTEGER", // lossy
            "LIST<INTEGER16>" to "LIST<INTEGER>", // lossy
            "INTEGER32" to "INTEGER", // lossy
            "LIST<INTEGER32>" to "LIST<INTEGER>", // lossy
            "INTEGER" to "INTEGER",
            "LIST<INTEGER>" to "LIST<INTEGER>",
            "LOCAL DATETIME" to "LOCALDATETIME",
            "LIST<LOCAL DATETIME>" to "LIST<LOCALDATETIME>",
            "LOCAL TIME" to "LOCALTIME",
            "LIST<LOCAL TIME>" to "LIST<LOCALTIME>",
            "POINT" to "POINT",
            "LIST<POINT>" to "LIST<POINT>",
            "STRING" to "STRING",
            "LIST<STRING>" to "LIST<STRING>",
            "VECTOR<FLOAT>" to "VECTOR<FLOAT>",
            "VECTOR<FLOAT32>" to "VECTOR<FLOAT>", // lossy: element precision dropped
            "VECTOR<INTEGER>" to "VECTOR<INTEGER>",
            "VECTOR<INTEGER32>" to "VECTOR<INTEGER>", // lossy: element width dropped
            "VECTOR<INTEGER16>" to "VECTOR<INTEGER>", // lossy
            "VECTOR<INTEGER8>" to "VECTOR<INTEGER>", // lossy
            "ZONED DATETIME" to "DATETIME",
            "LIST<ZONED DATETIME>" to "LIST<DATETIME>",
            "ZONED TIME" to "TIME",
            "LIST<ZONED TIME>" to "LIST<TIME>",
            "UUID" to "STRING", // lossy: uuid-ness dropped
        )
        assertEquals(38, expected.size, "the 4.0.0 Neo4jType enum has 38 tokens")
        val propertiesJson = expected.keys.withIndex().joinToString(",") { (i, token) ->
            val dimension = if (token.startsWith("VECTOR<")) ", \"dimension\": 7" else ""
            "\"p$i\": { \"type\": \"$token\"$dimension }"
        }
        val input = """{ "version": "4.0.0", "nodes": { "n": { "label": "n", "properties": { $propertiesJson } } } }"""

        val properties = properties(convert(input), "n")

        expected.values.withIndex().forEach { (i, token) ->
            assertEquals(token, properties.map("p$i").string("type"), "token row ${expected.keys.elementAt(i)}")
        }
        // The VECTOR companion dimension carries unchanged.
        assertEquals("7", properties.map("p28").string("dimension"))
    }

    @Test
    fun `root mapping - schema link, fresh UUID, own version 1, name and description rules`() {
        val out = convert(
            """{ "version": "4.0.0", "name": "shop", "description": "", "nodes": { "n": { "label": "n" } } }""",
        )

        assertEquals(GraphSpecV4OntologyGraphSpecMigration.SCHEMA_ID, out.string("\$schema"))
        assertEquals("1", out.literal("version").string)
        assertFalse(out.literal("version").isString, "v1 version is the ontology's own Int, not the 4.0.0 format string")
        val id = out.string("id")
        assertTrue(
            id.matches(Regex("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")),
            "id is a random UUID v4 per conversion, got: $id",
        )
        assertEquals("shop", out.string("name"))
        assertNull(out.literalOrNull("description"), "4.0.0's empty-string description is omitted (absent means none)")

        val again = convert(
            """{ "version": "4.0.0", "name": "shop", "nodes": { "n": { "label": "n" } } }""",
        )
        assertTrue(again.string("id") != id, "re-converting the same input mints a fresh id (ADR-0008 §1)")
    }

    @Test
    fun `nodes - label shorthand vs labels object, name consumed, description rules`() {
        val out = convert(
            """
            { "version": "4.0.0",
              "nodes": {
                "a": { "label": "A", "description": "node a" },
                "node1": { "labels": { "identifier": "B", "implied": ["A"], "optional": ["C"] }, "name": "b" }
              } }
            """.trimIndent(),
        )
        val nodes = out.map("nodes")
        assertEquals("A", nodes.map("a").string("label"))
        assertEquals("node a", nodes.map("a").string("description"))
        assertFalse(nodes.map("a").containsKey("labels"), "no implied/optional labels: shorthand label form")
        assertTrue(nodes.containsKey("b"), "the §2 id rule: name wins over the node1-style key")
        val labels = nodes.map("b").map("labels")
        assertEquals("B", labels.string("identifier"))
        assertEquals(listOf("A"), labels.list("implied").map { it.toString() })
        assertEquals(listOf("C"), labels.list("optional").map { it.toString() })
        assertFalse(nodes.map("b").containsKey("label"), "implied/optional labels: full labels object form")
        assertFalse(nodes.map("b").containsKey("name"), "name is consumed by the §2 id rule, never emitted")
    }

    @Test
    fun `internal form - node0-style keys resolve to names, references rewrite`() {
        val out = convert(
            """
            { "version": "4.0.0",
              "nodes": {
                "node0": { "labels": { "identifier": "A" }, "name": "a",
                           "properties": { "nodeProperty0": { "type": "UUID", "name": "aid" } } },
                "node1": { "labels": { "identifier": "B" }, "name": "b" }
              },
              "relationships": {
                "relationship0": { "type": "REL", "name": "REL",
                                   "from": { "node": "node1", "label": "B" },
                                   "to": { "node": "node0", "label": "A" } }
              } }
            """.trimIndent(),
        )
        val nodes = out.map("nodes")
        assertTrue(nodes.containsKey("a") && nodes.containsKey("b"), "internal keys resolve to names: $nodes")
        assertEquals("STRING", properties(out, "a").map("aid").string("type"), "UUID widens to STRING (lossy)")
        val rel = out.map("relationships").map("REL")
        assertEquals("b", rel.map("from").string("node"))
        assertEquals("a", rel.map("to").string("node"))
        assertFalse(rel.map("from").containsKey("label"), "endpoint label is dropped (ADR-0008 §5)")
        assertFalse(rel.map("from").containsKey("count"), "4.0.0 has no cardinality; nothing is synthesised")
        assertFalse(rel.containsKey("name"), "relationship name is consumed by the §2 id rule")
    }

    @Test
    fun `constraints - the flag vs object rule`() {
        val out = convert(
            """
            { "version": "4.0.0",
              "nodes": { "n": { "label": "n",
                "properties": {
                  "a": { "type": "STRING" },
                  "b": { "type": "STRING" },
                  "c": { "type": "STRING" },
                  "d": { "type": "STRING", "unique": true },
                  "e": { "type": "STRING" }
                },
                "constraints": {
                  "c0": { "type": "UNIQUE", "properties": ["a"], "name": "unique_n_a" },
                  "c1": { "type": "EXISTS", "properties": ["b"], "name": "human-name" },
                  "c2": { "type": "KEY", "properties": ["b", "c"] },
                  "c3": { "type": "UNIQUE", "properties": ["b", "c"], "name": "unique_n_b_c" },
                  "c4": { "type": "PROPERTY_TYPE", "properties": ["a"] },
                  "c5": { "type": "UNIQUE", "properties": ["d"], "name": "unique_n_d" },
                  "c6": { "type": "UNIQUE", "properties": ["missing"] }
                } } } }
            """.trimIndent(),
        )
        val node = out.map("nodes").map("n")
        val properties = node.map("properties")

        // c0: single property, deterministic machine name -> flag.
        assertEquals("true", properties.map("a").string("unique"))
        // c4: PROPERTY_TYPE is dropped (redundant with the property's type).
        // c5: deterministic object describing the already-flagged d merges into the flag.
        assertEquals("true", properties.map("d").string("unique"))

        val constraints = node.list("constraints")
        assertEquals(4, constraints.size, "c1, c2, c3 and c6 survive as objects, input order preserved")
        val objects = constraints.map { it as SchemaMap }
        // c1: human-given name keeps the object form and the name.
        assertEquals("mustExist", objects[0].string("constraint_type"))
        assertEquals("human-name", objects[0].string("name"))
        assertEquals(listOf("b"), objects[0].list("properties").map { it.toString() })
        // c2: composite -> object; key was arbitrary (nameless), so name-or-key carries it.
        assertEquals("key", objects[1].string("constraint_type"))
        assertEquals("c2", objects[1].string("name"))
        assertEquals(listOf("b", "c"), objects[1].list("properties").map { it.toString() })
        // c3: composite with a machine-generated name -> object, name dropped.
        assertEquals("unique", objects[2].string("constraint_type"))
        assertNull(objects[2].literalOrNull("name"))
        assertEquals(listOf("b", "c"), objects[2].list("properties").map { it.toString() })
        // c6: single property but dangling reference and non-deterministic name -> object.
        assertEquals("unique", objects[3].string("constraint_type"))
        assertEquals("c6", objects[3].string("name"))
        assertEquals(listOf("missing"), objects[3].list("properties").map { it.toString() })
    }

    @Test
    fun `constraints - dangling property reference stays an object`() {
        val out = convert(
            """
            { "version": "4.0.0",
              "nodes": { "n": { "label": "n",
                "properties": { "a": { "type": "STRING" } },
                "constraints": {
                  "c0": { "type": "UNIQUE", "properties": ["gone"], "name": "unique_n_gone" }
                } } } }
            """.trimIndent(),
        )
        val constraints = out.map("nodes").map("n").list("constraints")
        assertEquals(1, constraints.size, "a constraint on a missing property cannot become a flag")
        assertEquals(listOf("gone"), (constraints[0] as SchemaMap).list("properties").map { it.toString() })
    }

    @Test
    fun `extensions - untag, named-vs-custom, hoisting from closed shapes`() {
        val out = convert(
            """
            { "version": "4.0.0",
              "nodes": { "n": {
                "labels": { "identifier": "n", "implied": ["x"],
                            "extensions": { "acme:fromLabels": { "String": { "value": "lv" } } } },
                "properties": { "a": { "type": "STRING" } },
                "constraints": {
                  "c0": { "type": "UNIQUE", "properties": ["a"], "name": "human",
                          "extensions": { "acme:fromConstraint": { "Long": { "value": 7 } } } }
                },
                "extensions": {
                  "ui:display": { "Map": { "value": { "color": { "String": { "value": "#fff" } },
                                                          "pinned": { "Boolean": { "value": true } } } } },
                  "acme:fromElement": { "List": { "value": [ { "Double": { "value": 1.5 } },
                                                             { "String": { "value": "s" } } ] } }
                } } } }
            """.trimIndent(),
        )
        val extensions = out.map("nodes").map("n").map("extensions")

        // Named key under a declared owner prefix: carried, untagged, not shape-checked.
        val display = extensions.map("ui:display")
        assertEquals("#fff", display.string("color"))
        assertEquals("true", display.string("pinned"))

        // Every other key wraps in the custom envelope, input order per source map;
        // hoisted entries (constraints first, then labels) precede the element's own.
        val custom = extensions.list("custom").map { it as SchemaMap }
        assertEquals(listOf("acme:fromConstraint", "acme:fromLabels", "acme:fromElement"), custom.map { it.string("type") })
        assertEquals("7", custom[0].literal("definition").string)
        assertFalse(custom[0].literal("definition").isString, "Long untags to a JSON number")
        assertEquals("lv", custom[1].string("definition"))
        assertEquals("[1.5,s]", custom[2].list("definition").toString())
    }

    @Test
    fun `tables mappings display and indexes become named extension payloads`() {
        val out = convert(
            """
            { "version": "4.0.0",
              "nodes": {
                "node0": { "labels": { "identifier": "A" }, "name": "a",
                  "properties": { "nodeProperty0": { "type": "STRING", "name": "aid" } },
                  "indexes": { "nodeIndex0": { "type": "TEXT", "name": "a_text", "labels": ["A"],
                                               "properties": ["nodeProperty0"],
                                               "options": { "analyzer": { "String": { "value": "english" } } } } } },
                "node1": { "labels": { "identifier": "B" }, "name": "b",
                  "properties": { "nodeProperty0": { "type": "STRING", "name": "bid" } } }
              },
              "relationships": {
                "relationship0": { "type": "REL", "name": "r", "from": { "node": "node0" }, "to": { "node": "node1" } }
              },
              "tables": { "t": { "source": "cloud",
                  "columns": { "c": { "type": "INT", "suggested": "FLOAT32",
                                      "supported": ["UUID", "ZONED DATETIME"] } },
                  "primaryKeys": ["c"],
                  "foreignKeys": { "fk": { "columns": ["c"], "references": { "table": "t2", "columns": ["c2"] } } } } },
              "mappings": [
                { "node": "node0", "table": "t",
                  "properties": { "nodeProperty0": { "column": "c" } }, "key": ["nodeProperty0"] },
                { "relationship": "relationship0", "table": "t",
                  "from": { "node": "node0", "label": "A", "properties": { "nodeProperty0": { "column": "c" } } },
                  "to": { "node": "node1" } },
                { "table": "t", "query": "SELECT 1" }
              ],
              "display": { "nodes": { "node0": { "x": 1.5, "y": -2 } } }
            }
            """.trimIndent(),
        )
        val extensions = out.map("extensions")

        // Tables: list payloads, name from the map key, 4.0.0 tokens remapped per §3.
        val table = extensions.list("importer:table").single() as SchemaMap
        assertEquals("t", table.string("name"))
        assertEquals("cloud", table.string("source"))
        val column = table.map("columns").map("c")
        assertEquals("FLOAT", column.string("suggested"), "suggested remaps through the §3 table")
        assertEquals(listOf("STRING", "DATETIME"), column.list("supported").map { it.toString() })
        assertEquals(listOf("c"), table.list("primaryKeys").map { it.toString() })
        assertEquals("t2", table.map("foreignKeys").map("fk").map("references").string("table"))

        // Mappings: list payloads, structural kind, references rewritten per §2.
        val mappings = extensions.list("importer:mapping").map { it as SchemaMap }
        assertEquals(listOf("node", "relationship", "query"), mappings.map { it.string("kind") })
        assertEquals("a", mappings[0].string("node"))
        assertEquals("c", mappings[0].map("properties").map("aid").string("column"))
        assertEquals(listOf("aid"), mappings[0].list("key").map { it.toString() })
        assertEquals("r", mappings[1].string("relationship"))
        assertEquals("a", mappings[1].map("from").string("node"))
        assertEquals("A", mappings[1].map("from").string("label"), "TargetMapping.label is carried verbatim")
        assertEquals("c", mappings[1].map("from").map("properties").map("aid").string("column"))
        assertEquals("b", mappings[1].map("to").string("node"))
        assertEquals("SELECT 1", mappings[2].string("query"))

        // Display: a single object, node keys rewritten.
        val display = extensions.map("ui:display")
        assertEquals("1.5", display.map("nodes").map("a").string("x"))
        assertEquals("-2", display.map("nodes").map("a").string("y"))

        // Indexes: a list on the owning element, name-or-key, references rewritten,
        // options untagged. `type` maps to the lowercase v1 token; the 4.0.0 `labels`
        // field is gone — the label resolved through the §2 maps picked the owning
        // element ("A" is node a's identifier label).
        val index = (out.map("nodes").map("a").map("extensions").list("neo4j:index").single() as SchemaMap)
        assertEquals("text", index.string("type"))
        assertEquals("a_text", index.string("name"))
        assertFalse(index.containsKey("labels"), "the 4.0.0 labels field is dropped")
        assertEquals(listOf("aid"), index.list("properties").map { it.toString() })
        assertEquals("english", index.map("options").string("analyzer"))
    }

    @Test
    fun `a multi-label node index lands one payload per labeled node`() {
        val out = convert(
            """{ "version": "4.0.0",
                 "nodes": {
                   "node0": { "labels": { "identifier": "A" }, "name": "a",
                     "properties": { "p0": { "type": "STRING", "name": "shared" } },
                     "indexes": { "i0": { "type": "FULLTEXT", "name": "a_b_text",
                                          "labels": ["A", "B"], "properties": ["p0"] } } },
                   "node1": { "labels": { "identifier": "B" }, "name": "b",
                     "properties": { "p1": { "type": "STRING", "name": "shared" } } }
                 } }""",
        )

        val a = (out.map("nodes").map("a").map("extensions").list("neo4j:index").single() as SchemaMap)
        val b = (out.map("nodes").map("b").map("extensions").list("neo4j:index").single() as SchemaMap)
        assertEquals("fulltext", a.string("type"))
        assertEquals("fulltext", b.string("type"))
        assertEquals("a_b_text", a.string("name"))
        assertEquals("a_b_text", b.string("name"))
        assertFalse(a.containsKey("labels"))
        assertFalse(b.containsKey("labels"))
        assertEquals(listOf("shared"), a.list("properties").map { it.toString() })
        assertEquals(listOf("shared"), b.list("properties").map { it.toString() })
    }

    @Test
    fun `a node index whose labels resolve to no node stays on the owning element`() {
        val out = convert(
            """{ "version": "4.0.0", "nodes": { "n": { "label": "n",
                 "properties": { "p": { "type": "STRING" } },
                 "indexes": { "i": { "type": "RANGE", "labels": ["ghost"], "properties": ["p"] } } } } }""",
        )
        val index = (out.map("nodes").map("n").map("extensions").list("neo4j:index").single() as SchemaMap)
        assertEquals("range", index.string("type"))
        assertFalse(index.containsKey("labels"))
    }

    @Test
    fun `a relationship index stays on the owning relationship`() {
        val out = convert(
            """{ "version": "4.0.0",
                 "nodes": { "n": { "label": "n" } },
                 "relationships": { "r": { "type": "R", "from": { "node": "n" }, "to": { "node": "n" },
                   "properties": { "p": { "type": "STRING" } },
                   "indexes": { "ri": { "type": "POINT", "properties": ["p"] } } } } }""",
        )
        val index = (out.map("relationships").map("r").map("extensions").list("neo4j:index").single() as SchemaMap)
        assertEquals("point", index.string("type"))
        assertEquals("ri", index.string("name"))
        assertEquals(listOf("p"), index.list("properties").map { it.toString() })
        assertFalse(out.map("nodes").map("n").containsKey("extensions"), "no fan-out without labels")
    }

    @Test
    fun `index names resolve name-or-key like every 4-0-0 identity`() {
        val pretty = convert(
            """{ "version": "4.0.0", "nodes": { "n": { "label": "n",
                 "properties": { "p": { "type": "STRING" } },
                 "indexes": { "n_text": { "type": "TEXT", "labels": ["n"], "properties": ["p"] } } } } }""",
        )
        val index = (pretty.map("nodes").map("n").map("extensions").list("neo4j:index").single() as SchemaMap)
        assertEquals("n_text", index.string("name"), "pretty form: the map key is the name")
    }

    @Test
    fun `root extensions are omitted when tables mappings and display are absent`() {
        val out = convert("""{ "version": "4.0.0", "nodes": { "n": { "label": "n" } } }""")
        assertFalse(out.containsKey("extensions"), "absent means none")
    }

    @Test
    fun `end to end - OntologyGraphSpec decodes graph spec 4-0-0 JSON into the v1 model`() {
        val model = OntologyGraphSpec.Json.decodeFromString(
            """{ "version": "4.0.0", "name": "shop",
                 "nodes": { "n": { "label": "n", "properties": { "id": { "type": "UUID", "key": true } } } },
                 "relationships": { "R": { "type": "R", "from": { "node": "n" }, "to": { "node": "n" } } } }""",
            Type.GRAPH_SPEC,
        )
        assertEquals(GraphSpecV4OntologyGraphSpecMigration.SCHEMA_ID, model.schema)
        assertEquals(1, model.version)
        assertEquals("shop", model.name)
        val node = model.nodes.getValue("n")
        assertEquals("n", node.label)
        assertEquals("STRING", node.properties.getValue("id").type)
        assertEquals(true, node.properties.getValue("id").key)
        assertEquals("n", model.relationships.getValue("R").from.node)
    }

    @Test
    fun `json and yaml forms of the same document convert to the same v1 output`() {
        val json = """{ "version": "4.0.0", "nodes": { "n": { "label": "n",
                     "properties": { "id": { "type": "INTEGER", "unique": true } } } } }"""
        val yaml = "version: \"4.0.0\"\nnodes:\n  n:\n    label: \"n\"\n" +
            "    properties:\n      id: { type: \"INTEGER\", unique: true }\n"
        val mapper = ObjectMapper()
        val fromJson = mapper.readTree(
            OntologyGraphSpec.Json.encodeToString(OntologyGraphSpec.Json.decodeFromString(json, Type.GRAPH_SPEC)),
        ) as ObjectNode
        val fromYaml = mapper.readTree(
            OntologyGraphSpec.Json.encodeToString(OntologyGraphSpec.Yaml.decodeFromString(yaml, Type.GRAPH_SPEC)),
        ) as ObjectNode
        fromJson.remove("id")
        fromYaml.remove("id")
        assertEquals(fromJson, fromYaml)
    }

    @Test
    fun `unsupported inputs are rejected with migrate-to-4-0-0 guidance`() {
        val legacy = assertFailsWith<IllegalArgumentException> {
            convert("""{ "version": "3.0.0", "nodes": {} }""")
        }
        assertTrue(legacy.message!!.contains("migrate to graph spec 4.0.0 first"), legacy.message)

        val missing = assertFailsWith<IllegalStateException> {
            convert("""{ "nodes": {} }""")
        }
        assertTrue(missing.message!!.contains("migrate to graph spec 4.0.0 first"), missing.message)

        val endToEnd = assertFailsWith<IllegalStateException> {
            OntologyGraphSpec.Json.decodeFromString("""{ "version": "3.0.0" }""", Type.GRAPH_SPEC)
        }
        assertTrue(endToEnd.message!!.contains("Unsupported migration from graph_spec:3.0"), endToEnd.message)
    }

    @Test
    fun `id collisions after name-or-key resolution are a hard error`() {
        val exception = assertFailsWith<IllegalArgumentException> {
            convert("""{ "version": "4.0.0", "nodes": { "a": { "label": "A" }, "b": { "label": "B", "name": "a" } } }""")
        }
        assertTrue(exception.message!!.contains("collision"), exception.message)
    }

    @Test
    fun `graph spec 4-0-x patch versions are accepted`() {
        val out = convert("""{ "version": "4.0.3", "nodes": { "n": { "label": "n" } } }""")
        assertEquals(GraphSpecV4OntologyGraphSpecMigration.SCHEMA_ID, out.string("\$schema"))
    }
}
