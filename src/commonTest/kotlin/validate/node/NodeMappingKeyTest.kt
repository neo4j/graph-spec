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
import model.mapping.NodeMapping
import model.node.Labels
import model.node.Node
import model.property.Property
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeMappingKeyTest {

    private val validator = NodeMappingKey
    private val model = GraphModel("4.0.0")

    @Test
    fun `pass when node has no mapping`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("name" to Property())
        )
        model.nodes["person"] = node
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "person", node, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when node has no mapping")
    }

    @Test
    fun `pass when node mapping has key`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("name" to Property())
        )
        model.nodes["person"] = node
        model.mappings.add(
            NodeMapping(
                node = "person",
                table = "people.csv",
                properties = mutableMapOf(),
                key = mutableSetOf("name")
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "person", node, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when node mapping has key")
    }

    @Test
    fun `fail when node mapping has no key`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("name" to Property())
        )
        model.nodes["person"] = node
        model.mappings.add(
            NodeMapping(
                node = "person",
                table = "people.csv",
                properties = mutableMapOf(),
                key = mutableSetOf()
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "person", node, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_node_mapping_key", issues.first().code)
        assertEquals("missing_node_key_property", issues.first().legacyCode)
    }
}
