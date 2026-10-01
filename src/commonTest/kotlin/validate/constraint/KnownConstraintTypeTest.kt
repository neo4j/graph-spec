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
import model.relationship.Relationship
import model.relationship.RelationshipTarget
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class KnownConstraintTypeTest {

    private val validator = KnownConstraintType

    private fun modelWithNodeConstraint(constraint: Constraint) = GraphModel(
        schema = "https://neo4j.com/ontology-spec/1.0.0/schema.json",
        id = "test",
        version = 1,
        nodes = mutableMapOf("n" to Node(label = "N", constraints = mutableListOf(constraint))),
    )

    @Test
    fun `pass for every known constraint_type`() {
        for (type in listOf("key", "unique", "mustExist")) {
            val issues = mutableListOf<Issue>()

            validator.validate(
                modelWithNodeConstraint(Constraint(type = type, properties = mutableListOf("p"))),
                issues,
            )

            assertTrue(issues.isEmpty(), "Expected no issues for constraint_type '$type'")
        }
    }

    @Test
    fun `fail for an unknown constraint_type on a node`() {
        val issues = mutableListOf<Issue>()

        validator.validate(
            modelWithNodeConstraint(Constraint(type = "index", properties = mutableListOf("p"))),
            issues,
        )

        assertEquals(1, issues.size)
        assertEquals("unknown_constraint_type", issues.first().code)
        assertEquals("nodes.n.constraints[0].constraint_type", issues.first().path)
    }

    @Test
    fun `fail for an unknown constraint_type on a relationship`() {
        val model = GraphModel(
            schema = "https://neo4j.com/ontology-spec/1.0.0/schema.json",
            id = "test",
            version = 1,
            nodes = mutableMapOf("a" to Node(label = "A"), "b" to Node(label = "B")),
            relationships = mutableMapOf(
                "REL" to Relationship(
                    type = "REL",
                    from = RelationshipTarget(node = "a"),
                    to = RelationshipTarget(node = "b"),
                    constraints = mutableListOf(Constraint(type = "UNIQUE", properties = mutableListOf("p"))),
                ),
            ),
        )
        val issues = mutableListOf<Issue>()

        validator.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("unknown_constraint_type", issues.first().code)
        assertEquals("relationships.REL.constraints[0].constraint_type", issues.first().path)
    }
}
