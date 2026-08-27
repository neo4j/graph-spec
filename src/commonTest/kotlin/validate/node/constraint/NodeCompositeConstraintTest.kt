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
package validate.node.constraint

import model.GraphModel
import model.node.Labels
import model.node.Node
import model.node.NodeConstraint
import model.property.Property
import model.type.ConstraintType
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeCompositeConstraintTest {

    private val validator = NodeCompositeConstraint
    private val model = GraphModel("4.0.0")

    @Test
    fun `pass when constraint has 2 or more properties`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property(), "name" to Property()),
            constraints = mutableMapOf(
                "comp" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("email", "name")
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateConstraint(model, "personNode", node, "comp", node.constraints["comp"]!!, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when composite has 2+ properties")
    }

    @Test
    fun `pass when constraint has 1 property with name`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property()),
            constraints = mutableMapOf(
                "uniq_email" to NodeConstraint(
                    type = ConstraintType.UNIQUE,
                    label = "Person",
                    properties = mutableSetOf("email"),
                    name = "uniq_email"
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateConstraint(model, "personNode", node, "uniq_email", node.constraints["uniq_email"]!!, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when single constraint has a name")
    }

    @Test
    fun `fail when composite has 1 property and no name`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property()),
            constraints = mutableMapOf(
                "comp" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("email")
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateConstraint(model, "personNode", node, "comp", node.constraints["comp"]!!, issues)

        assertEquals(1, issues.size)
        assertEquals("invalid_node_composite_constraint", issues.first().code)
    }

    @Test
    fun `0 properties is not flagged here - owned by NodeConstraintProperties`() {
        // ARRANGE - 0 props is 'empty', not 'draft composite'
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf(),
            constraints = mutableMapOf(
                "comp" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf()
                )
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        validator.validateConstraint(model, "personNode", node, "comp", node.constraints["comp"]!!, issues)

        // ASSERT
        assertTrue(issues.isEmpty(), "0 properties is empty, not draft - owned by NodeConstraintProperties")
    }
}
