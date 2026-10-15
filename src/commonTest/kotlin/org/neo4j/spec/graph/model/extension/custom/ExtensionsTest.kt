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
package org.neo4j.spec.graph.model.extension.custom

import org.neo4j.spec.graph.codec.format.JsonFormat
import org.neo4j.spec.graph.model.GraphModel
import org.neo4j.spec.graph.model.extension.GraphModelExtensions
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExtensionsTest {

    private val extensions = GraphModelExtensions()

    @Test
    fun `top level setters store plain values`() {
        extensions.set("string", "text")
        extensions.set("boolean", true)
        extensions.set("int", 5)
        extensions.set("float", 0.5f)
        extensions.setList("list", listOf("a", "b"))
        extensions.setStringMap("strings", mapOf("a" to "b"))
        extensions.setIntMap("ints", mapOf("a" to 1))

        assertEquals(
            mapOf(
                "string" to "text",
                "boolean" to true,
                "int" to 5L,
                "float" to 0.5,
                "list" to listOf("a", "b"),
                "strings" to mapOf("a" to "b"),
                "ints" to mapOf("a" to 1L)
            ),
            extensions.custom
        )
        assertEquals("text", extensions.getString("string"))
        assertEquals(true, extensions.getBoolean("boolean"))
        assertEquals(5, extensions.getInt("int"))
        assertEquals(0.5f, extensions.getFloat("float"))
        assertNull(extensions.getLong("float"))
        assertNull(extensions.getString("int"))
    }

    @Test
    fun `section values survive serialization`() {
        extensions.set("section", "name", "value")
        extensions.set("section", "count", 3)
        extensions.set("section", "ratio", 2.5)
        extensions.custom["section_list"] = mutableMapOf<String, Any>("ids" to mutableListOf(1L, 2L))
        extensions.custom["section_map"] = mutableMapOf<String, Any>("weights" to mutableMapOf("a" to 1.5, "b" to 2L))

        val format = JsonFormat.default
        val model = GraphModel(
            schema = "https://neo4j.com/ontology-graph-spec/4.0.0/",
            version = "1",
            id = "test",
            extensions = extensions
        )
        val decoded = format.decodeModelFromString(format.encodeModelToString(model)).extensions

        assertEquals("value", decoded.getString("section", "name"))
        assertEquals(3L, decoded.getLong("section", "count"))
        assertEquals(2.5, decoded.getDouble("section", "ratio"))
        assertEquals(3.0, decoded.getDouble("section", "count"))
        assertEquals(listOf(1, 2), decoded.getIntList("section_list", "ids"))
        assertEquals(mapOf("a" to 1.5, "b" to 2.0), decoded.getDoubleMap("section_map", "weights"))
        assertEquals(mapOf("b" to 2L), decoded.getLongMap("section_map", "weights"))
    }

    @Test
    fun `immutable section maps can be read and extended`() {
        extensions.custom["section"] = mapOf("a" to 1L)

        assertEquals(1L, extensions.getLong("section", "a"))
        extensions.set("section", "b", "x")

        assertEquals(1L, extensions.getLong("section", "a"))
        assertEquals("x", extensions.getString("section", "b"))
    }

    @Test
    fun `typed map getters skip values of other types`() {
        extensions.custom["section"] = mapOf("m" to mapOf("a" to 1L, "b" to "x", "c" to 2L))

        assertEquals(mapOf("a" to 1L, "c" to 2L), extensions.getLongMap("section", "m"))
        assertEquals(mapOf("b" to "x"), extensions.getStringMap("section", "m"))
        assertNull(extensions.getLongMap("section", "missing"))
    }
}
