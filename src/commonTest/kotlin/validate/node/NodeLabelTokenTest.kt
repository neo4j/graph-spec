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

class NodeLabelTokenTest {

    private val validator = NodeLabelToken
    private val model = GraphModel("4.0.0")

    @Test
    fun `pass when label token has no invalid characters`() {
        val node = Node(labels = Labels(identifier = "Person"))
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertTrue(issues.isEmpty(), "Expected no issues when label token is valid")
    }

    @Test
    fun `fail when label token contains colon`() {
        val node = Node(labels = Labels(identifier = "Person:Detail"))
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertEquals(1, issues.size)
        assertEquals("invalid_node_label_token", issues.first().code)
    }

    @Test
    fun `fail when label token contains equals`() {
        val node = Node(labels = Labels(identifier = "Person=Detail"))
        val issues = mutableListOf<Issue>()

        validator.validateNode(model, "personNode", node, issues)

        assertEquals(1, issues.size)
        assertEquals("invalid_node_label_token", issues.first().code)
    }
}
