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

class NodeConstraintDuplicatePropertySetTest {

    private val validator = NodeConstraintDuplicatePropertySet
    private val model = GraphModel("4.0.0")

    @Test
    fun `pass when no duplicate composite property sets`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property(), "name" to Property()),
            constraints = mutableMapOf(
                "key_email_name" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("email", "name")
                ),
                "uniq_email" to NodeConstraint(
                    type = ConstraintType.UNIQUE,
                    label = "Person",
                    properties = mutableSetOf("email")
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when no duplicate property sets")
    }

    @Test
    fun `fail when two composite constraints share same property set`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property(), "name" to Property()),
            constraints = mutableMapOf(
                "key1" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("email", "name")
                ),
                "key2" to NodeConstraint(
                    type = ConstraintType.UNIQUE,
                    label = "Person",
                    properties = mutableSetOf("email", "name")
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertEquals(2, issues.size)
        assertEquals("duplicate_node_constraint_property_set", issues.first().code)
    }

    @Test
    fun `fail when property sets are equal in different order`() {
        // ARRANGE - order-independent comparison like UPX isSamePropertyList
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property(), "name" to Property()),
            constraints = mutableMapOf(
                "key1" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("email", "name")
                ),
                "key2" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("name", "email")
                )
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        validator.validateNode(model, "personNode", node, issues)

        // ASSERT
        assertEquals(2, issues.size)
    }

    @Test
    fun `pass when two single-property constraints share the same property`() {
        // ARRANGE - 1-prop constraints are never checked (UPX: propertyIds.length < 2 returns false)
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property()),
            constraints = mutableMapOf(
                "key1" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("email")
                ),
                "key2" to NodeConstraint(
                    type = ConstraintType.UNIQUE,
                    label = "Person",
                    properties = mutableSetOf("email")
                )
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        validator.validateNode(model, "personNode", node, issues)

        // ASSERT
        assertTrue(issues.isEmpty(), "1-prop constraints are never checked for duplicates")
    }
}
