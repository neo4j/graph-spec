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
package migrate.migration.dataModel

import codec.schema.schemaMapOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Review test for PR #23: constraint ids generated from shorthands must be unique
 * within the document's flat constraints array.
 * Asserts the desired behavior - fails on the current code, regression test after the fix.
 */
class ConstraintIdCollisionReviewTest {

    private val toDataModel = GraphSpecDataModelV3Migration()

    private fun node(label: String, property: String) = schemaMapOf(
        "labels" to schemaMapOf("identifier" to label),
        "properties" to mapOf(
            property to schemaMapOf("type" to "STRING", "unique" to true)
        )
    )

    @Test
    fun `generated constraint ids are unique within the document`() {
        // ARRANGE - two nodes with differently named unique properties (no dedup interference)
        val spec = schemaMapOf(
            "version" to "4.0.0",
            "nodes" to mapOf(
                "Person" to node("Person", "email"),
                "Company" to node("Company", "orgnr")
            )
        )

        // ACT
        val ids = toDataModel.migrate(spec)
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("constraints")
            .map { it.string("\$id") }

        // ASSERT
        assertEquals(ids.toSet().size, ids.size, "Constraint \$ids must be unique within the document: $ids")
    }
}
