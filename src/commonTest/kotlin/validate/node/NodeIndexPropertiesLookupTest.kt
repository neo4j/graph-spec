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
import model.type.IndexType
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals

class NodeIndexPropertiesLookupTest {

    private val validator = NodeIndexProperties
    private val model = GraphModel("4.0.0")

    @Test
    fun `LOOKUP index with no properties is flagged`() {
        // ARRANGE - a Neo4j lookup index has no properties by definition
        val nodeId = "userNode"
        val indexId = "idx_lookup"
        val node = Node(properties = mutableMapOf())
        val index = NodeIndex(
            type = IndexType.LOOKUP,
            labels = mutableSetOf(),
            properties = mutableSetOf()
        )

        // ACT
        val issues = mutableListOf<Issue>()
        validator.validateIndex(model, nodeId, node, indexId, index, issues)

        // ASSERT - confirms the concern: importReady flags every lookup index
        assertEquals(1, issues.size, "LOOKUP index with empty properties is flagged missing_index_properties")
        assertEquals("missing_index_properties", issues.first().code)
    }
}
