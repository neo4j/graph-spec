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

class RelationshipMappingTargetsTest {

    private fun validate(from: TargetMapping, to: TargetMapping): List<Issue> {
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
                        RelationshipMapping(relationship = "rel0", table = "t", fromNode = from, toNode = to)
                    )
                )
            )
        )
        return mutableListOf<Issue>().also { RelationshipMappingTargets.validate(model, it) }
    }

    private val column = TargetMapping(nodeKeyColumn = "id", relationshipColumn = "a")
    private val property = TargetMapping(nodeKeyProperty = "id", relationshipColumn = "b")

    @Test
    fun `pass when both ends use key columns`() {
        assertTrue(validate(column, column).isEmpty())
    }

    @Test
    fun `pass when both ends use key properties`() {
        assertTrue(validate(property, property).isEmpty())
    }

    @Test
    fun `pass when targets are unset`() {
        assertTrue(validate(TargetMapping(), TargetMapping()).isEmpty())
    }

    @Test
    fun `fail when one end uses a column and the other a property`() {
        val issues = validate(column, property)

        assertEquals(1, issues.size)
        assertEquals("mismatched_target_mapping_key", issues.first().code)
        assertEquals("extensions.mappings.m0[0]", issues.first().path)
    }

    @Test
    fun `fail when a target uses both a column and a property`() {
        val both = TargetMapping(nodeKeyColumn = "id", nodeKeyProperty = "id", relationshipColumn = "a")

        val issues = validate(property, both)

        assertEquals(listOf("conflicting_target_mapping_key"), issues.map { it.code })
        assertEquals("extensions.mappings.m0[0].to_node", issues.first().path)
    }
}
