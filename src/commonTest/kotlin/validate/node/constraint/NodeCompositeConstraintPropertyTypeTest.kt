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
import model.property.Neo4jType
import model.property.Property
import model.type.ConstraintType
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeCompositeConstraintPropertyTypeTest {

    private val validator = NodeCompositeConstraintPropertyType
    private val model = GraphModel("4.0.0")

    @Test
    fun `pass when composite properties are STRING and INTEGER`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf(
                "email" to Property(type = Neo4jType.STRING),
                "age" to Property(type = Neo4jType.INTEGER)
            ),
            constraints = mutableMapOf(
                "comp" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("email", "age")
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateConstraint(model, "personNode", node, "comp", node.constraints["comp"]!!, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when composite properties are STRING/INTEGER")
    }

    @Test
    fun `fail when composite property is FLOAT`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf(
                "email" to Property(type = Neo4jType.STRING),
                "score" to Property(type = Neo4jType.FLOAT)
            ),
            constraints = mutableMapOf(
                "comp" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("email", "score")
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateConstraint(model, "personNode", node, "comp", node.constraints["comp"]!!, issues)

        assertEquals(1, issues.size)
        assertEquals("invalid_node_composite_constraint_property_type", issues.first().code)
    }
}
