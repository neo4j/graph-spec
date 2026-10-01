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

class RelationshipTypeTest {

    private val validator = RelationshipType

    private fun model(type: String) = GraphModel(
        schema = "https://neo4j.com/ontology-spec/1.0.0/schema.json",
        id = "test",
        version = 1,
        nodes = mutableMapOf("a" to Node(label = "A"), "b" to Node(label = "B")),
        relationships = mutableMapOf(
            "REL" to
                Relationship(type = type, from = RelationshipTarget(node = "a"), to = RelationshipTarget(node = "b")),
        ),
    )

    @Test
    fun `pass when the relationship has a type`() {
        val issues = mutableListOf<Issue>()

        validator.validate(model("ACTED_IN"), issues)

        assertTrue(issues.isEmpty(), "Expected no issues when the relationship type is set")
    }

    @Test
    fun `fail when the relationship type is blank`() {
        val issues = mutableListOf<Issue>()

        validator.validate(model("  "), issues)

        assertEquals(1, issues.size)
        assertEquals("missing_relation_type", issues.first().code)
        assertEquals("relationships.REL.type", issues.first().path)
    }

    @Test
    fun `fail when the relationship type is empty`() {
        val issues = mutableListOf<Issue>()

        validator.validate(model(""), issues)

        assertEquals(1, issues.size)
        assertEquals("missing_relation_type", issues.first().code)
    }
}
