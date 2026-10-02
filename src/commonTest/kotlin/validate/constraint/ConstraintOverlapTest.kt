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

class ConstraintOverlapTest {

    private val validator = ConstraintOverlap

    private fun modelWithNode(node: Node) = GraphModel(
        schema = "https://neo4j.com/ontology-spec/1.0.0/schema.json",
        id = "test",
        version = 1,
        nodes = mutableMapOf("n" to node),
    )

    private fun modelWithRelationship(relationship: Relationship) = GraphModel(
        schema = "https://neo4j.com/ontology-spec/1.0.0/schema.json",
        id = "test",
        version = 1,
        nodes = mutableMapOf("a" to Node(label = "A"), "b" to Node(label = "B")),
        relationships = mutableMapOf("REL" to relationship),
    )

    private fun relationshipWith(constraints: List<Constraint>, properties: Map<String, Property> = emptyMap()) =
        Relationship(
            type = "REL",
            from = RelationshipTarget(node = "a"),
            to = RelationshipTarget(node = "b"),
            properties = properties.toMutableMap(),
            constraints = constraints.toMutableList(),
        )

    private fun validate(model: GraphModel): List<Issue> {
        val issues = mutableListOf<Issue>()
        validator.validate(model, issues)
        return issues
    }

    @Test
    fun `fail for a unique constraint object covered by a key constraint on a node`() {
        val issues = validate(
            modelWithNode(
                Node(
                    label = "N",
                    constraints = mutableListOf(
                        Constraint(type = "key", properties = mutableListOf("p")),
                        Constraint(type = "unique", properties = mutableListOf("p"), name = "uniq_p"),
                    ),
                ),
            ),
        )

        assertEquals(1, issues.size)
        assertEquals("redundant_constraint_key_overlap", issues.first().code)
        assertEquals("nodes.n.constraints[1]", issues.first().path)
    }

    @Test
    fun `fail for a mustExist constraint object covered by a key constraint on a relationship`() {
        val issues = validate(
            modelWithRelationship(
                relationshipWith(
                    listOf(
                        Constraint(type = "key", properties = mutableListOf("p")),
                        Constraint(type = "mustExist", properties = mutableListOf("p")),
                    ),
                ),
            ),
        )

        assertEquals(1, issues.size)
        assertEquals("redundant_constraint_key_overlap", issues.first().code)
        assertEquals("relationships.REL.constraints[1]", issues.first().path)
    }

    @Test
    fun `pass for a unique constraint object whose property set no key constraint covers`() {
        val issues = validate(
            modelWithNode(
                Node(
                    label = "N",
                    constraints = mutableListOf(
                        Constraint(type = "key", properties = mutableListOf("p")),
                        Constraint(type = "unique", properties = mutableListOf("q")),
                    ),
                ),
            ),
        )

        assertTrue(issues.isEmpty(), "Expected no issues, got $issues")
    }

    @Test
    fun `fail for a key flag duplicated by a key constraint object on a node property`() {
        val issues = validate(
            modelWithNode(
                Node(
                    label = "N",
                    properties = mutableMapOf("p" to Property(key = true)),
                    constraints = mutableListOf(Constraint(type = "key", properties = mutableListOf("p"))),
                ),
            ),
        )

        assertEquals(1, issues.size)
        assertEquals("duplicate_shorthand_constraint_flag", issues.first().code)
        assertEquals("nodes.n.properties.p.key", issues.first().path)
    }

    @Test
    fun `fail for unique and mustExist flags duplicated by same-type constraint objects on a relationship`() {
        val issues = validate(
            modelWithRelationship(
                relationshipWith(
                    constraints = listOf(
                        Constraint(type = "unique", properties = mutableListOf("u")),
                        Constraint(type = "mustExist", properties = mutableListOf("m")),
                    ),
                    properties = mapOf(
                        "u" to Property(unique = true),
                        "m" to Property(mustExist = true),
                    ),
                ),
            ),
        )

        assertEquals(2, issues.size)
        assertEquals(setOf("duplicate_shorthand_constraint_flag"), issues.map { it.code }.toSet())
        assertEquals(
            setOf("relationships.REL.properties.u.unique", "relationships.REL.properties.m.mustExist"),
            issues.map { it.path }.toSet(),
        )
    }

    @Test
    fun `pass for a false or absent flag next to a same-type constraint object`() {
        val issues = validate(
            modelWithNode(
                Node(
                    label = "N",
                    properties = mutableMapOf(
                        "p" to Property(unique = false),
                        "q" to Property(),
                    ),
                    constraints = mutableListOf(
                        Constraint(type = "unique", properties = mutableListOf("p")),
                        Constraint(type = "mustExist", properties = mutableListOf("q")),
                    ),
                ),
            ),
        )

        assertTrue(issues.isEmpty(), "Expected no issues, got $issues")
    }

    @Test
    fun `pass for a flag duplicated only by a different-type constraint object`() {
        val issues = validate(
            modelWithNode(
                Node(
                    label = "N",
                    properties = mutableMapOf("p" to Property(unique = true)),
                    constraints = mutableListOf(Constraint(type = "mustExist", properties = mutableListOf("p"))),
                ),
            ),
        )

        assertTrue(issues.isEmpty(), "Expected no issues, got $issues")
    }

    @Test
    fun `fail for a single-property mustExist constraint on a property in a composite constraint on a node`() {
        val issues = validate(
            modelWithNode(
                Node(
                    label = "N",
                    constraints = mutableListOf(
                        Constraint(type = "key", properties = mutableListOf("p", "q")),
                        Constraint(type = "mustExist", properties = mutableListOf("p")),
                    ),
                ),
            ),
        )

        assertEquals(1, issues.size)
        assertEquals("existence_composite_conflict", issues.first().code)
        assertEquals("nodes.n.constraints[1]", issues.first().path)
    }

    @Test
    fun `pass for a single-property mustExist constraint on a property in no composite constraint`() {
        val issues = validate(
            modelWithNode(
                Node(
                    label = "N",
                    constraints = mutableListOf(
                        Constraint(type = "key", properties = mutableListOf("p", "q")),
                        Constraint(type = "mustExist", properties = mutableListOf("r")),
                    ),
                ),
            ),
        )

        assertTrue(issues.isEmpty(), "Expected no issues, got $issues")
    }

    @Test
    fun `fail for two composite constraints over the same property set on a node`() {
        val issues = validate(
            modelWithNode(
                Node(
                    label = "N",
                    constraints = mutableListOf(
                        Constraint(type = "unique", properties = mutableListOf("p", "q")),
                        Constraint(type = "unique", properties = mutableListOf("q", "p")),
                    ),
                ),
            ),
        )

        assertEquals(2, issues.size)
        assertEquals(setOf("duplicate_constraint_property_set"), issues.map { it.code }.toSet())
        assertEquals(
            setOf("nodes.n.constraints[0].properties", "nodes.n.constraints[1].properties"),
            issues.map { it.path }.toSet(),
        )
    }

    @Test
    fun `pass for two composite constraints over different property sets`() {
        val issues = validate(
            modelWithNode(
                Node(
                    label = "N",
                    constraints = mutableListOf(
                        Constraint(type = "key", properties = mutableListOf("p", "q")),
                        Constraint(type = "unique", properties = mutableListOf("p", "r")),
                    ),
                ),
            ),
        )

        assertTrue(issues.isEmpty(), "Expected no issues, got $issues")
    }

    @Test
    fun `pass for a clean model`() {
        val issues = validate(
            modelWithNode(
                Node(
                    label = "N",
                    properties = mutableMapOf(
                        "id" to Property(key = true),
                        "email" to Property(unique = true),
                        "name" to Property(mustExist = true),
                    ),
                    constraints = mutableListOf(
                        Constraint(type = "key", properties = mutableListOf("p", "q"), name = "composite_key"),
                    ),
                ),
            ),
        )

        assertTrue(issues.isEmpty(), "Expected no issues, got $issues")
    }
}
