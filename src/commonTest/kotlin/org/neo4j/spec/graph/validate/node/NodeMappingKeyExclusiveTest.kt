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
package org.neo4j.spec.graph.validate.node

import org.neo4j.spec.graph.model.GraphModel
import org.neo4j.spec.graph.model.extension.mapping.NodeMapping
import org.neo4j.spec.graph.model.node.Labels
import org.neo4j.spec.graph.model.node.Node
import org.neo4j.spec.graph.model.property.Property
import org.neo4j.spec.graph.validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeMappingKeyExclusiveTest {

    private val node = Node(
        labels = Labels(identifier = "Person"),
        properties = mutableMapOf("name" to Property())
    )

    private fun validate(columns: Set<String>, properties: Set<String>): List<Issue> {
        val model = GraphModel(version = "1", id = "test")
        model.nodes["person"] = node
        model.extensions.mappings["mapping0"] = mutableListOf(
            NodeMapping(
                node = "person",
                table = "people",
                properties = mutableMapOf(),
                keyColumns = columns.toMutableSet(),
                keyProperties = properties.toMutableSet()
            )
        )
        val issues = mutableListOf<Issue>()
        NodeMappingKeyExclusive.validateNode(model, "person", node, issues)
        return issues
    }

    @Test
    fun `pass when only key columns are used`() {
        assertTrue(validate(setOf("id"), emptySet()).isEmpty())
    }

    @Test
    fun `pass when only key properties are used`() {
        assertTrue(validate(emptySet(), setOf("name")).isEmpty())
    }

    @Test
    fun `fail when both key columns and key properties are used`() {
        val issues = validate(setOf("id"), setOf("name"))

        assertEquals(1, issues.size)
        assertEquals("conflicting_node_mapping_key", issues.first().code)
        assertEquals("extensions.mappings.mapping0[0]", issues.first().path)
    }
}
