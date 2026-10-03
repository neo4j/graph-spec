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
package model

import model.extension.MapValue
import model.extension.StringValue
import model.node.Node
import model.property.Property
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * v1 re-home of the still-applicable 4.0.0 JsEdgeCaseTests cases: container tokens
 * (LIST<STRING>), field retention through encode/decode, and the absent-means-none
 * rule (empty maps/lists are omitted on the wire).
 */
class V1EdgeCaseJsTest {
    @Test
    fun `encodeToString handles LIST_of_STRING tokens`() {
        val model =
            GraphModel(
                schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
                id = "edge",
                version = 1,
                nodes =
                    mutableMapOf(
                        "n" to
                            Node(
                                label = "N",
                                properties = mutableMapOf("tags" to Property(type = "LIST<STRING>")),
                            ),
                    ),
            )

        val encoded = OntologyGraphSpec.Json.encodeToString(model)
        val decoded = OntologyGraphSpec.Json.decodeFromString(encoded)

        assertEquals("LIST<STRING>", decoded.nodes["n"]!!.properties["tags"]!!.type)
    }

    @Test
    fun `encodeToString does not drop fields`() {
        val model =
            GraphModel(
                schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
                id = "edge",
                version = 1,
                name = "named",
                description = "described",
                nodes =
                    mutableMapOf(
                        "n" to
                            Node(
                                label = "N",
                                description = "a node",
                                aliases = mutableListOf("N1", "N2"),
                                reference = "https://example.com/N",
                            ),
                    ),
            )

        val decoded = OntologyGraphSpec.Json.decodeFromString(OntologyGraphSpec.Json.encodeToString(model))
        val node = decoded.nodes["n"]!!

        assertEquals("named", decoded.name)
        assertEquals("described", decoded.description)
        assertEquals("a node", node.description)
        assertEquals(listOf("N1", "N2"), node.aliases.toList())
        assertEquals("https://example.com/N", node.reference)
    }

    @Test
    fun `absent means none - empty maps and lists are omitted on the wire`() {
        val model =
            GraphModel(
                schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
                id = "edge",
                version = 1,
                nodes = mutableMapOf("n" to Node(label = "N")),
            )

        val encoded = OntologyGraphSpec.Json.encodeToString(model)

        assertFalse(encoded.contains("\"properties\""), "empty properties map must be omitted: $encoded")
        assertFalse(encoded.contains("\"constraints\""), "empty constraints list must be omitted: $encoded")
        assertFalse(encoded.contains("\"extensions\""), "empty extensions map must be omitted: $encoded")
    }

    @Test
    fun `unknown extension content survives an encode-decode round trip`() {
        val model =
            GraphModel(
                schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
                id = "edge",
                version = 1,
                nodes = mutableMapOf("n" to Node(label = "N")),
            )
        model.nodes["n"]!!.extensions["future:thing"] =
            MapValue(mutableMapOf("deep" to StringValue("value")))

        val decoded = OntologyGraphSpec.Json.decodeFromString(OntologyGraphSpec.Json.encodeToString(model))

        val carried = decoded.nodes["n"]!!.extensions["future:thing"] as MapValue
        assertEquals(StringValue("value"), carried.value["deep"])
    }
}
