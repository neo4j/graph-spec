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
package validate.node

import model.GraphModel
import model.node.Labels
import model.node.Node
import model.node.NodeConstraint
import model.node.NodeIndex
import model.property.Property
import model.type.ConstraintType
import model.type.IndexType
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeConstraintNameConflictTest {

    private val validator = NodeConstraintNameConflict
    private val model = GraphModel("4.0.0")

    @Test
    fun `pass when names are unique`() {
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
            ),
            indexes = mutableMapOf(
                "idx_email" to NodeIndex(
                    type = IndexType.RANGE,
                    labels = mutableSetOf("Person"),
                    properties = mutableSetOf("email"),
                    name = "idx_email"
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when names are unique")
    }

    @Test
    fun `pass when same name with same properties label and type`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property()),
            constraints = mutableMapOf(
                "c1" to NodeConstraint(
                    type = ConstraintType.UNIQUE,
                    label = "Person",
                    properties = mutableSetOf("email"),
                    name = "my_name"
                ),
                "c2" to NodeConstraint(
                    type = ConstraintType.UNIQUE,
                    label = "Person",
                    properties = mutableSetOf("email"),
                    name = "my_name"
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        // Same name with same properties/label/type is allowed
        assertTrue(issues.isEmpty(), "Expected no issues when same name with same properties/label/type")
    }

    @Test
    fun `fail when same name with different types`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property()),
            constraints = mutableMapOf(
                "c1" to NodeConstraint(
                    type = ConstraintType.UNIQUE,
                    label = "Person",
                    properties = mutableSetOf("email"),
                    name = "my_name"
                )
            ),
            indexes = mutableMapOf(
                "i1" to NodeIndex(
                    type = IndexType.RANGE,
                    labels = mutableSetOf("Person"),
                    properties = mutableSetOf("email"),
                    name = "my_name"
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertEquals(1, issues.size)
        assertEquals("duplicate_node_constraint_name", issues.first().code)
    }
}
