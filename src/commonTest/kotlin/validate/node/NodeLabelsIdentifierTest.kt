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
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeLabelsIdentifierTest {

    private val validator = NodeLabelsIdentifier

    private fun model(node: Node) = GraphModel(
        schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
        id = "test",
        version = 1,
        nodes = mutableMapOf("n" to node),
    )

    @Test
    fun `pass when labels has an identifier`() {
        val issues = mutableListOf<Issue>()

        validator.validate(model(Node(labels = Labels(identifier = "Person"))), issues)

        assertTrue(issues.isEmpty(), "Expected no issues when identifier label is set")
    }

    @Test
    fun `pass when the node does not use the labels object`() {
        val issues = mutableListOf<Issue>()

        validator.validate(model(Node(label = "Person")), issues)

        assertTrue(issues.isEmpty(), "Expected no issues when labels is absent")
    }

    @Test
    fun `fail when labels has a null identifier`() {
        val issues = mutableListOf<Issue>()

        validator.validate(model(Node(labels = Labels(identifier = null))), issues)

        assertEquals(1, issues.size)
        assertEquals("missing_node_identifier_label", issues.first().code)
        assertEquals("nodes.n.labels.identifier", issues.first().path)
    }

    @Test
    fun `fail when labels has a blank identifier`() {
        val issues = mutableListOf<Issue>()

        validator.validate(model(Node(labels = Labels(identifier = "  "))), issues)

        assertEquals(1, issues.size)
        assertEquals("missing_node_identifier_label", issues.first().code)
    }
}
