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

import org.neo4j.spec.graph.codec.schema.schemaListOf
import org.neo4j.spec.graph.codec.schema.schemaMapOf
import kotlin.test.Test
import kotlin.test.assertTrue

class ConstraintOnNonFirstLabelSurvivesGm3ToGsTest {

    private val gsToGm3 = GraphSpecDataModelV3Migration()
    private val gm3ToGs = DataModelV3GraphSpecMigration()

    @Test
    fun `gm3 to gs propagates constraint attached to non-first node label`() {
        val gm3Input = schemaMapOf(
            "version" to "3.0.0",
            "graphMappingRepresentation" to schemaMapOf(
                "dataSourceSchema" to schemaMapOf(
                    "type" to "cloud",
                    "tableSchemas" to schemaListOf()
                )
            ),
            "graphSchemaRepresentation" to schemaMapOf(
                "graphSchema" to schemaMapOf(
                    "nodeLabels" to schemaListOf(
                        schemaMapOf("\$id" to "l0", "token" to "Person", "properties" to schemaListOf()),
                        schemaMapOf("\$id" to "l1", "token" to "PremiumMember", "properties" to schemaListOf())
                    ),
                    "nodeObjectTypes" to schemaListOf(
                        schemaMapOf(
                            "\$id" to "n1",
                            "labels" to schemaListOf(
                                schemaMapOf("\$ref" to "l0"),
                                schemaMapOf("\$ref" to "l1")
                            )
                        )
                    ),
                    "constraints" to schemaListOf(
                        schemaMapOf(
                            "\$id" to "c_unique_ssn",
                            "name" to "unique_premium_ssn",
                            "entityType" to "node",
                            "type" to "UNIQUE",
                            "nodeLabel" to schemaMapOf("\$ref" to "l1"),
                            "properties" to schemaListOf()
                        )
                    )
                )
            )
        )

        val gs = gm3ToGs.migrate(gm3Input)
        val n1Constraints = gs.mapOfMaps("nodes").values.first().listOfMapsOrNull("constraints") ?: emptyList()
        val survivingIds = n1Constraints.mapNotNull { it.literalOrNull("\$id")?.string }

        assertTrue(
            survivingIds.contains("c_unique_ssn"),
            "constraint on non-first label l1 should survive gm3 to gs - loop-all TODO at DataModelV3GraphSpecMigration.kt:142 filters to first labelRef only"
        )
    }
}
