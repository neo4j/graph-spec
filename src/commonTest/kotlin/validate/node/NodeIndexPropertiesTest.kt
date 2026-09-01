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
import model.node.Node
import model.node.NodeIndex
import model.property.Property
import model.type.IndexType
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeIndexPropertiesTest {

    private val validator = NodeIndexProperties
    private val model = GraphModel("4.0.0")

    @Test
    fun `pass when index has properties`() {
        // ARRANGE
        val nodeId = "userNode"
        val indexId = "idx_user_email"
        val node = Node(
            properties = mutableMapOf("email" to Property())
        )
        val index = NodeIndex(
            type = IndexType.TEXT,
            labels = mutableSetOf(),
            properties = mutableSetOf("email")
        )

        // ACT
        val issues = mutableListOf<Issue>()
        validator.validateIndex(model, nodeId, node, indexId, index, issues)

        // ASSERT
        assertTrue(issues.isEmpty(), "Expected no issues when index has properties")
    }

    @Test
    fun `fail when index has no properties`() {
        // ARRANGE
        val nodeId = "userNode"
        val indexId = "idx_empty"
        val node = Node(
            properties = mutableMapOf("email" to Property())
        )
        val index = NodeIndex(
            type = IndexType.TEXT,
            labels = mutableSetOf(),
            properties = mutableSetOf()
        )

        // ACT
        val issues = mutableListOf<Issue>()
        validator.validateIndex(model, nodeId, node, indexId, index, issues)

        // ASSERT
        assertEquals(1, issues.size)
        val issue = issues.first()
        assertEquals("missing_index_properties", issue.code)
        assertEquals("nodes.userNode.indexes.idx_empty.properties", issue.path)
    }
}
