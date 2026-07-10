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
 * Review test for PR #23: constraints of different entities must not dedup against each other.
 * Asserts the desired behavior - fails on the current code, regression test after the fix.
 */
class CrossEntityConstraintDedupReviewTest {

    private val toDataModel = GraphSpecDataModelV3Migration()

    private fun node(label: String, property: String) = schemaMapOf(
        "labels" to schemaMapOf("identifier" to label),
        "properties" to mapOf(
            property to schemaMapOf("type" to "STRING", "unique" to true)
        )
    )

    @Test
    fun `shorthand constraints from different nodes both survive`() {
        // ARRANGE - two nodes, same-named property, each with its own uniqueness shorthand
        val spec = schemaMapOf(
            "version" to "4.0.0",
            "nodes" to mapOf(
                "Person" to node("Person", "email"),
                "Company" to node("Company", "email")
            )
        )

        // ACT
        val constraints = toDataModel.migrate(spec)
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("constraints")

        // ASSERT - Person.email and Company.email are distinct Neo4j constraints
        assertEquals(2, constraints.size, "Each node's uniqueness constraint must survive: $constraints")
    }
}
