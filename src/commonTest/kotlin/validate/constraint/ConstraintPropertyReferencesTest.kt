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
package validate.constraint

import model.GraphModel
import model.node.Constraint
import model.node.Node
import model.property.Property
import model.relationship.Relationship
import model.relationship.RelationshipTarget
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ConstraintPropertyReferencesTest {

    private val validator = ConstraintPropertyReferences

    private fun modelWithNode(node: Node) = GraphModel(
        schema = "https://neo4j.com/ontology-spec/1.0.0/schema.json",
        id = "test",
        version = 1,
        nodes = mutableMapOf("n" to node)
    )

    private fun modelWithRelationship(relationship: Relationship) = GraphModel(
        schema = "https://neo4j.com/ontology-spec/1.0.0/schema.json",
        id = "test",
        version = 1,
        nodes = mutableMapOf("a" to Node(label = "A"), "b" to Node(label = "B")),
        relationships = mutableMapOf("REL" to relationship)
    )

    private fun relationshipWithConstraint(constraint: Constraint) = Relationship(
        type = "REL",
        from = RelationshipTarget(node = "a"),
        to = RelationshipTarget(node = "b"),
        properties = mutableMapOf("p" to Property(type = "STRING")),
        constraints = mutableListOf(constraint)
    )

    @Test
    fun `fail for a dangling property reference on a node constraint`() {
        val issues = mutableListOf<Issue>()

        validator.validate(
            modelWithNode(
                Node(
                    label = "N",
                    properties = mutableMapOf("p" to Property(type = "STRING")),
                    constraints = mutableListOf(Constraint(type = "unique", properties = mutableListOf("p", "q")))
                )
            ),
            issues
        )

        assertEquals(1, issues.size)
        assertEquals("unknown_constraint_property", issues.first().code)
        assertEquals("nodes.n.constraints[0].properties[1]", issues.first().path)
    }

    @Test
    fun `fail for a dangling property reference on a relationship constraint`() {
        val issues = mutableListOf<Issue>()

        validator.validate(
            modelWithRelationship(
                relationshipWithConstraint(Constraint(type = "key", properties = mutableListOf("missing")))
            ),
            issues
        )

        assertEquals(1, issues.size)
        assertEquals("unknown_constraint_property", issues.first().code)
        assertEquals("relationships.REL.constraints[0].properties[0]", issues.first().path)
    }

    @Test
    fun `fail for an empty properties list on a constraint`() {
        val issues = mutableListOf<Issue>()

        validator.validate(
            modelWithNode(
                Node(
                    label = "N",
                    constraints = mutableListOf(Constraint(type = "mustExist", properties = mutableListOf()))
                )
            ),
            issues
        )

        assertEquals(1, issues.size)
        assertEquals("empty_constraint_properties", issues.first().code)
        assertEquals("nodes.n.constraints[0].properties", issues.first().path)
    }

    @Test
    fun `pass for a clean model`() {
        val model = modelWithRelationship(
            relationshipWithConstraint(Constraint(type = "unique", properties = mutableListOf("p")))
        )
        model.nodes["n"] = Node(
            label = "N",
            properties = mutableMapOf("p" to Property(type = "STRING")),
            constraints = mutableListOf(Constraint(type = "key", properties = mutableListOf("p")))
        )
        val issues = mutableListOf<Issue>()

        validator.validate(model, issues)

        assertTrue(issues.isEmpty(), "Expected no issues, got $issues")
    }
}
