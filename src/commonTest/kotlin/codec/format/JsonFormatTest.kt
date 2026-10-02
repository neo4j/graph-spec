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
package codec.format

import codec.schema.SchemaLiteral
import codec.schema.SchemaMap
import codec.schema.SchemaNull
import model.GraphModel
import model.extension.BooleanValue
import model.extension.StringValue
import model.node.Node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertTrue

class JsonFormatTest {

    private val jsonFormat: Format = JsonFormat.default

    @Test
    fun `test nested path generation for maps and lists`() {
        val json = """
        {
            "nodes": {
                "user": {
                    "properties": {
                        "age": "30"
                    }
                }
            },
            "mappings": [
                { "id": "m1" }
            ]
        }
        """.trimIndent()

        val schema = jsonFormat.decodeFromString(json) as SchemaMap

        // Verify Map Pathing
        assertEquals(
            "nodes.user.properties.age",
            schema.map("nodes").map("user").map("properties").literal("age").path,
        )

        // Verify List Pathing
        val mappings = schema.list("mappings")
        assertEquals("mappings", mappings.path)
        val firstMapping = mappings[0] as SchemaMap
        assertEquals("mappings[0]", firstMapping.path)
        assertEquals("mappings[0].id", firstMapping.literal("id").path)
    }

    @Test
    fun `test SchemaNull edge case in maps`() {
        val json = """{ "missing": null, "present": "value" }"""
        val schema = jsonFormat.decodeFromString(json) as SchemaMap

        assertIs<SchemaNull>(schema.content["missing"])
        assertEquals("missing", schema.content["missing"]?.path)

        val backToJson = jsonFormat.encodeToString(schema)
        assertTrue(backToJson.contains("\"missing\": null"))
    }

    @Test
    fun `test list and map extraction helpers`() {
        val json = """
        {
            "list_of_maps": [{ "id": 1 }, { "id": 2 }],
            "map_of_maps": { 
                "a": { "v": "hi" },
                "b": { "v": "bye" }
            }
        }
        """.trimIndent()
        val schema = jsonFormat.decodeFromString(json) as SchemaMap

        val listOfMaps = schema.listOfMaps("list_of_maps")
        assertEquals(2, listOfMaps.size)
        assertEquals("1", listOfMaps[0].string("id"))

        val mapOfMaps = schema.mapOfMaps("map_of_maps")
        assertEquals("hi", mapOfMaps["a"]?.string("v"))
    }

    @Test
    fun `test empty collections round-trip`() {
        val model = GraphModel(
            schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
            id = "empty",
            version = 1,
            nodes = mutableMapOf(),
        )

        val schema = jsonFormat.encodeToSchema(model)
        val decoded = jsonFormat.decodeFromSchema(schema)

        assertEquals(model.version, decoded.version)
        assertTrue(decoded.nodes.isEmpty())
        assertTrue(decoded.relationships.isEmpty())
    }

    @Test
    fun `test untagged ExtensionValue serialization`() {
        val node = Node(
            extensions = mutableMapOf(
                "ext_str" to StringValue("hello"),
                "ext_bool" to BooleanValue(true),
            ),
        )
        val model = GraphModel(
            schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
            id = "extensions",
            version = 1,
            nodes = mutableMapOf("n1" to node),
        )

        val schema = jsonFormat.encodeToSchema(model) as SchemaMap
        val n1 = schema.map("nodes").map("n1")
        val extensions = n1.map("extensions")

        // v1 extension payloads are untagged raw JSON: no {"String": {...}} wrapper
        val extStr = extensions.content["ext_str"]
        assertIs<SchemaLiteral>(extStr)
        assertEquals("hello", extStr.string)
        val extBool = extensions.content["ext_bool"]
        assertIs<SchemaLiteral>(extBool)
        assertEquals("true", extBool.string)
    }

    @Test
    fun `test error handling for missing keys or wrong types`() {
        val json = """{ "id": "123", "meta": { "active": true } }"""
        val schema = jsonFormat.decodeFromString(json) as SchemaMap

        println(schema)
        // Try to access a literal as a map
        val ex1 = assertFailsWith<IllegalStateException> { schema.map("id") }
        println(ex1.message)
        assertTrue(ex1.message!!.contains("Expected map, found invalid type SchemaLiteral at id"))

        // Try to access a missing key
        val ex2 = assertFailsWith<IllegalStateException> { schema.list("ghost") }
        assertTrue(ex2.message!!.contains("Missing required list at ghost"))
    }
}
