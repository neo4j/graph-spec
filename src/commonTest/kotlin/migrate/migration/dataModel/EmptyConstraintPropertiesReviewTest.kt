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
 * Review test for PR #23: a constraint with a present-but-empty properties list must be
 * skipped by shorthand derivation, not crash on properties.first().
 * Asserts the desired behavior - fails on the current code, regression test after the fix.
 */
class EmptyConstraintPropertiesReviewTest {

    private val toGraphSpec = DataModelV3GraphSpecMigration()

    @Test
    fun `constraint with empty properties list is skipped not crashed`() {
        // ARRANGE - present-but-empty properties list on a constraint
        val labels = listOf(
            schemaMapOf(
                "properties" to listOf(
                    schemaMapOf("\$id" to "p1", "token" to "email", "type" to schemaMapOf("type" to "string"))
                )
            )
        )
        val constraints = mapOf(
            "c1" to schemaMapOf("type" to "UNIQUE", "properties" to emptyList<String>())
        )

        // ACT - today: NoSuchElementException at properties.first()
        val properties = toGraphSpec.convertProperties(labels, constraints)

        // ASSERT - no shorthand derived from a constraint with no properties
        assertEquals(1, properties.size)
    }
}
