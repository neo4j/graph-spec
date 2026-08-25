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

class NodeExistenceCompositeConflictTest {

    private val validator = NodeExistenceCompositeConflict
    private val model = GraphModel("4.0.0")

    @Test
    fun `pass when existence does not conflict with composite`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property(), "name" to Property()),
            constraints = mutableMapOf(
                "exist_email" to NodeConstraint(
                    type = ConstraintType.EXISTS,
                    label = "Person",
                    properties = mutableSetOf("email")
                ),
                "key_name" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("name")
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when existence does not conflict")
    }

    @Test
    fun `fail when existence property is in a composite constraint`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property(), "name" to Property()),
            constraints = mutableMapOf(
                "exist_email" to NodeConstraint(
                    type = ConstraintType.EXISTS,
                    label = "Person",
                    properties = mutableSetOf("email")
                ),
                "comp_email_name" to NodeConstraint(
                    type = ConstraintType.KEY,
                    label = "Person",
                    properties = mutableSetOf("email", "name")
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertEquals(1, issues.size)
        assertEquals("node_existence_composite_conflict", issues.first().code)
    }
}
