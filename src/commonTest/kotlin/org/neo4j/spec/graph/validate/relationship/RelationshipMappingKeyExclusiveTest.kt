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
package org.neo4j.spec.graph.validate.relationship

import org.neo4j.spec.graph.model.GraphModel
import org.neo4j.spec.graph.model.extension.GraphModelExtensions
import org.neo4j.spec.graph.model.extension.mapping.RelationshipMapping
import org.neo4j.spec.graph.model.extension.mapping.TargetMapping
import org.neo4j.spec.graph.model.node.Node
import org.neo4j.spec.graph.model.relationship.Relationship
import org.neo4j.spec.graph.model.relationship.RelationshipTarget
import org.neo4j.spec.graph.validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationshipMappingKeyExclusiveTest {

    private fun validate(columns: Set<String>, properties: Set<String>): List<Issue> {
        val model = GraphModel(
            version = "1",
            id = "test",
            nodes = mutableMapOf("node0" to Node()),
            relationships = mutableMapOf(
                "rel0" to Relationship(
                    type = "KNOWS",
                    from = RelationshipTarget(node = "node0"),
                    to = RelationshipTarget(node = "node0")
                )
            ),
            extensions = GraphModelExtensions(
                mappings = mutableMapOf(
                    "m0" to mutableListOf(
                        RelationshipMapping(
                            relationship = "rel0",
                            table = "t",
                            fromNode = TargetMapping(),
                            toNode = TargetMapping(),
                            keyColumns = columns.toMutableSet(),
                            keyProperties = properties.toMutableSet()
                        )
                    )
                )
            )
        )
        return mutableListOf<Issue>().also { RelationshipMappingKeyExclusive.validate(model, it) }
    }

    @Test
    fun `pass when only key columns are used`() {
        assertTrue(validate(setOf("id"), emptySet()).isEmpty())
    }

    @Test
    fun `pass when only key properties are used`() {
        assertTrue(validate(emptySet(), setOf("since")).isEmpty())
    }

    @Test
    fun `fail when both key columns and key properties are used`() {
        val issues = validate(setOf("id"), setOf("since"))

        assertEquals(1, issues.size)
        assertEquals("conflicting_relationship_mapping_key", issues.first().code)
        assertEquals("extensions.mappings.m0[0]", issues.first().path)
    }
}
