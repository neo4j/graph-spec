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

class NodePropertyVectorDimensionTest {

    private val validator = NodePropertyVectorDimension
    private val model = GraphModel("4.0.0")

    @Test
    fun `fail when node vector property has null dimension`() {
        // ARRANGE
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf(
                "embedding" to Property(type = Neo4jType.VECTOR_FLOAT, dimension = null)
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        validator.validateProperty(model, "personNode", node, "embedding", node.properties["embedding"]!!, issues)

        // ASSERT
        assertEquals(1, issues.size)
        assertEquals("missing_vector_dimension", issues.first().code)
    }

    @Test
    fun `pass when node vector property has a dimension set`() {
        // ARRANGE
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf(
                "embedding" to Property(type = Neo4jType.VECTOR_FLOAT, dimension = 128)
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        validator.validateProperty(model, "personNode", node, "embedding", node.properties["embedding"]!!, issues)

        // ASSERT
        assertTrue(issues.isEmpty(), "Expected no issues when vector property has a dimension")
    }

    @Test
    fun `pass when node property is a non-vector type with null dimension`() {
        // ARRANGE
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf(
                "name" to Property(type = Neo4jType.STRING, dimension = null)
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        validator.validateProperty(model, "personNode", node, "name", node.properties["name"]!!, issues)

        // ASSERT
        assertTrue(issues.isEmpty(), "Expected no issues for non-vector type with null dimension")
    }

    @Test
    fun `fail for each node vector property missing dimension`() {
        // ARRANGE - multiple vector properties without dimension
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf(
                "embedding" to Property(type = Neo4jType.VECTOR_FLOAT32, dimension = null),
                "embedding2" to Property(type = Neo4jType.VECTOR_INTEGER, dimension = null),
                "good" to Property(type = Neo4jType.VECTOR_INTEGER8, dimension = 64)
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        for ((propertyId, property) in node.properties) {
            validator.validateProperty(model, "personNode", node, propertyId, property, issues)
        }

        // ASSERT
        assertEquals(2, issues.size)
        assertTrue(issues.all { it.code == "missing_vector_dimension" })
    }

    @Test
    fun `fail for all vector type variants when dimension is null`() {
        // ARRANGE - each VECTOR_* variant should be flagged when dimension is null
        val vectorTypes = listOf(
            Neo4jType.VECTOR_FLOAT,
            Neo4jType.VECTOR_FLOAT32,
            Neo4jType.VECTOR_INTEGER,
            Neo4jType.VECTOR_INTEGER32,
            Neo4jType.VECTOR_INTEGER16,
            Neo4jType.VECTOR_INTEGER8
        )
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf(
                "v0" to Property(type = vectorTypes[0], dimension = null),
                "v1" to Property(type = vectorTypes[1], dimension = null),
                "v2" to Property(type = vectorTypes[2], dimension = null),
                "v3" to Property(type = vectorTypes[3], dimension = null),
                "v4" to Property(type = vectorTypes[4], dimension = null),
                "v5" to Property(type = vectorTypes[5], dimension = null)
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        for ((propertyId, property) in node.properties) {
            validator.validateProperty(model, "personNode", node, propertyId, property, issues)
        }

        // ASSERT
        assertEquals(6, issues.size)
        assertTrue(issues.all { it.code == "missing_vector_dimension" })
    }
}
