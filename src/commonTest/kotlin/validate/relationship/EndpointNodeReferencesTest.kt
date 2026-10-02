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
package validate.relationship

import model.GraphModel
import model.node.Node
import model.relationship.Relationship
import model.relationship.RelationshipTarget
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EndpointNodeReferencesTest {

    private val validator = EndpointNodeReferences

    private fun model(relationship: Relationship) = GraphModel(
        schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
        id = "test",
        version = 1,
        nodes = mutableMapOf("a" to Node(label = "A"), "b" to Node(label = "B")),
        relationships = mutableMapOf("REL" to relationship),
    )

    @Test
    fun `pass when both endpoints resolve to nodes map keys`() {
        val model =
            model(
                Relationship(type = "REL", from = RelationshipTarget(node = "a"), to = RelationshipTarget(node = "b")),
            )
        val issues = mutableListOf<Issue>()

        validator.validate(model, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when both endpoint references resolve")
    }

    @Test
    fun `fail when the from endpoint references a missing node`() {
        val model =
            model(
                Relationship(
                    type = "REL",
                    from = RelationshipTarget(node = "missing"),
                    to = RelationshipTarget(node = "b"),
                ),
            )
        val issues = mutableListOf<Issue>()

        validator.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_relation_from_node", issues.first().code)
        assertEquals("relationships.REL.from.node", issues.first().path)
    }

    @Test
    fun `fail when the to endpoint references a missing node`() {
        val model =
            model(
                Relationship(
                    type = "REL",
                    from = RelationshipTarget(node = "a"),
                    to = RelationshipTarget(node = "missing"),
                ),
            )
        val issues = mutableListOf<Issue>()

        validator.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_relation_to_node", issues.first().code)
        assertEquals("relationships.REL.to.node", issues.first().path)
    }

    @Test
    fun `fail with one issue per dangling endpoint when both are missing`() {
        val model =
            model(
                Relationship(type = "REL", from = RelationshipTarget(node = "x"), to = RelationshipTarget(node = "y")),
            )
        val issues = mutableListOf<Issue>()

        validator.validate(model, issues)

        assertEquals(listOf("missing_relation_from_node", "missing_relation_to_node"), issues.map { it.code })
    }
}
