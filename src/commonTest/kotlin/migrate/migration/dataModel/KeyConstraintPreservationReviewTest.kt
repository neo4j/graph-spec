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

import codec.schema.schemaListOf
import codec.schema.schemaMapOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Review test: a KEY constraint in graph spec must survive conversion to the data model.
 * Users set key constraints independently of IDs (constraints UI, IMP-903); the data
 * model represents them fine (constraintType key). Skipping ALL KEY constraints on
 * conversion silently deletes user-declared schema rules.
 *
 * Skipping may be right for constraints that back a mapping key (the UI re-derives
 * those from the ID), but a KEY constraint backing nothing is user state, not derived state.
 *
 * Fails on the reviewed head (convertElements drops every KEY constraint).
 * Goes green when non-backing KEY constraints are preserved.
 */
class KeyConstraintPreservationReviewTest {

    private val migration = GraphSpecDataModelV3Migration()

    @Test
    fun `user KEY constraint survives conversion to data model`() {
        // ARRANGE - a KEY constraint with no mapping key backing it
        val input = schemaMapOf(
            "version" to "2.0",
            "nodes" to schemaMapOf(
                "customer" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "Customer"),
                    "properties" to schemaMapOf(
                        "customerId" to schemaMapOf("type" to "STRING"),
                        "email" to schemaMapOf("type" to "STRING")
                    ),
                    "constraints" to schemaMapOf(
                        "emailKey" to schemaMapOf(
                            "type" to "KEY",
                            "properties" to schemaListOf("email")
                        )
                    )
                )
            )
        )

        // ACT
        val result = migration.migrate(input)

        // ASSERT - the user's constraint is still there
        val constraints = result
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("constraints")
        assertEquals(1, constraints.size, "a user-declared KEY constraint must not be silently dropped")
        assertEquals("key", constraints[0].string("constraintType"))
    }
}
