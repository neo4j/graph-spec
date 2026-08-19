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
import model.property.Neo4jType
import model.property.Property
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeKeyPropertyTest {

    private val validator = NodeKeyProperty
    private val model = GraphModel("4.0.0")

    @Test
    fun `pass when node has a key property`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("id" to Property(type = Neo4jType.STRING, key = true))
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when node has a key property")
    }

    @Test
    fun `fail when node has no key property`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("name" to Property(type = Neo4jType.STRING))
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_node_key_property", issues.first().code)
    }

    @Test
    fun `pass when key property is STRING`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("id" to Property(type = Neo4jType.STRING, key = true))
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when key property is STRING")
    }

    @Test
    fun `pass when key property is INTEGER`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("id" to Property(type = Neo4jType.INTEGER, key = true))
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when key property is INTEGER")
    }

    @Test
    fun `fail when key property is FLOAT`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("id" to Property(type = Neo4jType.FLOAT, key = true))
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        val keyTypeIssue = issues.find { it.code == "invalid_node_key_property_type" }
        assertTrue(keyTypeIssue != null, "Expected invalid key property type issue")
    }
}
