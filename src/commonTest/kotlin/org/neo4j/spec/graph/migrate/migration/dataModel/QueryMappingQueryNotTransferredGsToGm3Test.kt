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
import kotlin.test.assertFailsWith

class QueryMappingQueryNotTransferredGsToGm3Test {

    private val gsToGm3 = GraphSpecDataModelV3Migration()

    @Test
    fun `gs to gm3 hard-errors on QueryMapping entries via convertGraphMapping`() {
        val gsInput = schemaMapOf(
            "version" to "1.0.0",
            "nodes" to schemaMapOf(
                "n1" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "Person")
                )
            ),
            "extensions" to schemaMapOf(
                "mappings" to schemaMapOf(
                    "mapping0" to listOf(
                        schemaMapOf(
                            "table" to "raw_events",
                            "query" to "UNWIND \$rows AS row CREATE (n:Person {id: row.id})"
                        )
                    )
                )
            )
        )

        assertFailsWith<IllegalStateException>(
            "QueryMapping entries should produce a hard error at GraphSpecDataModelV3Migration.kt:97 - today this surfaces a runtime crash on save"
        ) {
            gsToGm3.migrate(gsInput)
        }
    }

    @Test
    fun `gm3 to gs cannot reconcile a query form because gm3 lacks the primitive`() {
        val gm3Input = schemaMapOf(
            "version" to "3.0.0",
            "graphSchemaRepresentation" to schemaMapOf(
                "version" to "1.0.0",
                "graphSchema" to schemaMapOf(
                    "nodeLabels" to emptyList<Any>(),
                    "nodeObjectTypes" to emptyList<Any>(),
                    "relationshipTypes" to emptyList<Any>(),
                    "relationshipObjectTypes" to emptyList<Any>(),
                    "constraints" to emptyList<Any>(),
                    "indexes" to emptyList<Any>()
                )
            ),
            "graphSchemaExtensionsRepresentation" to schemaMapOf(
                "queryMappings" to listOf(
                    schemaMapOf("table" to "raw_events", "query" to "UNWIND \$rows AS row CREATE (n:Person {id: row.id})")
                )
            ),
            "graphMappingRepresentation" to schemaMapOf(
                "dataSourceSchema" to schemaMapOf("type" to "local", "tableSchemas" to emptyList<Any>()),
                "nodeMappings" to emptyList<Any>(),
                "relationshipMappings" to emptyList<Any>()
            ),
            "configurations" to schemaMapOf("idsToIgnore" to emptyList<String>())
        )

        val gm3ToGs = DataModelV3GraphSpecMigration()
        val result = gm3ToGs.migrate(gm3Input)

        val extensions = result.mapOrNull("extensions") ?: org.neo4j.spec.graph.codec.schema.schemaMapOf()
        val mappingsContainer = extensions.mapOrNull("mappings")
        val mappingEntries = mappingsContainer?.listOfMapsOrNull("mapping0") ?: emptyList()
        assertEquals(0, mappingEntries.size)
    }
}
