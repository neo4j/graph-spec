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
package org.neo4j.spec.graph.migrate.migration.dataModel

import org.neo4j.spec.graph.codec.schema.schemaMapOf
import kotlin.test.Test
import kotlin.test.assertEquals

class TwoTablesToOneNodeRoundTripTest {

    private val gm3ToGs = DataModelV3GraphSpecMigration()
    private val gsToGm3 = GraphSpecDataModelV3Migration()

    @Test
    fun `roundtrip gm3 to gs to gm3 preserves two nodeMappings on one nodeObject`() {
        val original = schemaMapOf(
            "version" to "3.0",
            "graphSchemaRepresentation" to schemaMapOf(
                "graphSchema" to schemaMapOf(
                    "nodeLabels" to listOf(
                        schemaMapOf(
                            "\$id" to "L1",
                            "token" to "Person",
                            "properties" to emptyList<Any>()
                        )
                    ),
                    "nodeObjectTypes" to listOf(
                        schemaMapOf(
                            "\$id" to "nodeObj1",
                            "labels" to listOf(mapOf("\$ref" to "#L1"))
                        )
                    )
                )
            ),
            "graphMappingRepresentation" to schemaMapOf(
                "nodeMappings" to listOf(
                    schemaMapOf(
                        "node" to mapOf("\$ref" to "#nodeObj1"),
                        "tableName" to "TableA",
                        "propertyMappings" to emptyList<Any>()
                    ),
                    schemaMapOf(
                        "node" to mapOf("\$ref" to "#nodeObj1"),
                        "tableName" to "TableB",
                        "propertyMappings" to emptyList<Any>()
                    )
                )
            )
        )

        val gs = gm3ToGs.migrate(original)

        val gsNodes = gs.map("nodes")
        assertEquals(setOf("nodeObj1"), gsNodes.keys.toSet(), "gm3 to gs should resolve two tables under one node id")

        val gsMappings = gs
            .map("extensions")
            .mapOfListsOfMapsOrNull("mappings")
            ?.values
            ?.flatten()
            .orEmpty()
        assertEquals(2, gsMappings.size, "gm3 to gs should produce two gs mappings, one per table")

        val gsByTable = gsMappings.associateBy { it.string("table") }
        assertEquals(setOf("TableA", "TableB"), gsByTable.keys, "both tables should remain distinct on the gs side")
        assertEquals("nodeObj1", gsByTable["TableA"]?.string("node"))
        assertEquals("nodeObj1", gsByTable["TableB"]?.string("node"))

        val roundTripped = gsToGm3.migrate(gs)

        val nodeObjectTypes = roundTripped
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("nodeObjectTypes")
        assertEquals(1, nodeObjectTypes.size, "expected one nodeObjectType after round trip")

        val nodeMappings = roundTripped
            .map("graphMappingRepresentation")
            .listOfMaps("nodeMappings")
        assertEquals(2, nodeMappings.size, "expected two nodeMappings to survive round trip")

        val byTable = nodeMappings.associateBy { it.string("tableName") }
        assertEquals(setOf("TableA", "TableB"), byTable.keys, "both tables should remain distinct")
        assertEquals("#nodeObj1", byTable["TableA"]?.map("node")?.string("\$ref"))
        assertEquals("#nodeObj1", byTable["TableB"]?.map("node")?.string("\$ref"))
    }
}
