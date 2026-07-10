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
package model.mapping

import codec.format.Format
import codec.format.JsonFormat
import codec.schema.SchemaMap
import model.GraphModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MappingKeyTest {

    private val jsonFormat: Format = JsonFormat.default

    @Test
    fun `node mapping key can be set without a data source`() {
        // ARRANGE
        val json = """
        {
            "version": "4.0.0",
            "mappings": [
                {
                    "type": "node",
                    "node": "n:0",
                    "table": "",
                    "properties": {},
                    "key": ["id"]
                }
            ]
        }
        """.trimIndent()

        // ACT
        val model = jsonFormat.decodeFromSchema(jsonFormat.decodeFromString(json) as SchemaMap)

        // ASSERT
        val mapping = model.mappings.filterIsInstance<NodeMapping>().single()
        assertEquals("", mapping.table)
        assertEquals(setOf("id"), mapping.key)
    }

    @Test
    fun `relationship mapping key holds the parts of one composite key`() {
        // ARRANGE
        val json = """
        {
            "version": "4.0.0",
            "mappings": [
                {
                    "type": "relationship",
                    "relationship": "r:0",
                    "table": "",
                    "from": { "node": "n:0" },
                    "to": { "node": "n:1" },
                    "key": ["a", "b"]
                }
            ]
        }
        """.trimIndent()

        // ACT
        val model = jsonFormat.decodeFromSchema(jsonFormat.decodeFromString(json) as SchemaMap)

        // ASSERT
        val mapping = model.mappings.filterIsInstance<RelationshipMapping>().single()
        assertEquals(setOf("a", "b"), mapping.key)
    }

    @Test
    fun `node mapping key round-trips`() {
        // ARRANGE
        val model = GraphModel(
            version = "4.0.0",
            mappings = mutableListOf(
                NodeMapping(node = "n:0", table = "", properties = mutableMapOf(), key = mutableSetOf("id"))
            )
        )

        // ACT
        val encoded = jsonFormat.encodeToString(jsonFormat.encodeToSchema(model))
        val decoded = jsonFormat.decodeFromSchema(jsonFormat.decodeFromString(encoded) as SchemaMap)

        // ASSERT
        assertTrue(encoded.contains("\"key\""), "Expected key field in: $encoded")
        assertEquals(setOf("id"), decoded.mappings.filterIsInstance<NodeMapping>().single().key)
    }
}
