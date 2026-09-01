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
package validate.node.index

import model.GraphModel
import model.node.Labels
import model.node.Node
import model.node.NodeIndex
import model.property.Property
import model.type.IndexType
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeIndexDuplicatePropertySetTest {

    private val validator = NodeIndexDuplicatePropertySet
    private val model = GraphModel("4.0.0")

    private fun nodeWithIndexes(vararg indexes: Pair<String, Set<String>>): Node = Node(
        labels = Labels(identifier = "Person"),
        properties = mutableMapOf(
            "email" to Property(),
            "name" to Property(),
            "age" to Property()
        ),
        indexes = indexes.associate { (id, properties) ->
            id to NodeIndex(
                type = IndexType.RANGE,
                labels = mutableSetOf("Person"),
                properties = properties.toMutableSet()
            )
        }.toMutableMap()
    )

    private fun validate(node: Node): List<Issue> {
        val issues = mutableListOf<Issue>()
        for ((indexId, index) in node.indexes) {
            validator.validateIndex(model, "personNode", node, indexId, index, issues)
        }
        return issues
    }

    @Test
    fun `pass when index property sets are distinct`() {
        // ARRANGE
        val node = nodeWithIndexes("idx1" to setOf("email", "name"), "idx2" to setOf("email"))

        // ACT
        val issues = validate(node)

        // ASSERT
        assertTrue(issues.isEmpty(), "Expected no issues when index property sets are distinct")
    }

    @Test
    fun `fail when two indexes share the same property set`() {
        // ARRANGE
        val node = nodeWithIndexes("idx1" to setOf("email", "name"), "idx2" to setOf("email", "name"))

        // ACT
        val issues = validate(node)

        // ASSERT
        assertEquals(2, issues.size)
        assertEquals("duplicate_index_property_set", issues.first().code)
        assertEquals("nodes.personNode.indexes.idx1.properties", issues.first().path)
    }

    @Test
    fun `fail when property sets are equal in different order`() {
        // ARRANGE - order-independent comparison, like UPX isSamePropertyList
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("email" to Property(), "name" to Property()),
            indexes = mutableMapOf(
                "idx1" to NodeIndex(
                    type = IndexType.RANGE,
                    labels = mutableSetOf("Person"),
                    properties = mutableSetOf("email", "name")
                ),
                "idx2" to NodeIndex(
                    type = IndexType.RANGE,
                    labels = mutableSetOf("Person"),
                    properties = mutableSetOf("name", "email")
                )
            )
        )

        // ACT
        val issues = validate(node)

        // ASSERT
        assertEquals(2, issues.size)
    }

    @Test
    fun `fail when two single-property indexes share the same property`() {
        // ARRANGE - unlike constraints, indexes have no composite gate in UPX
        val node = nodeWithIndexes("idx1" to setOf("email"), "idx2" to setOf("email"))

        // ACT
        val issues = validate(node)

        // ASSERT
        assertEquals(2, issues.size)
        assertEquals("duplicate_index_property_set", issues.first().code)
    }

    @Test
    fun `fail for every index in a group of three duplicates`() {
        // ARRANGE
        val node = nodeWithIndexes(
            "idx1" to setOf("email"),
            "idx2" to setOf("email"),
            "idx3" to setOf("email")
        )

        // ACT
        val issues = validate(node)

        // ASSERT
        assertEquals(3, issues.size)
    }

    @Test
    fun `pass when index property sets are empty`() {
        // ARRANGE - UPX returns false for an empty set; emptiness is a separate rule
        val node = nodeWithIndexes("idx1" to emptySet(), "idx2" to emptySet())

        // ACT
        val issues = validate(node)

        // ASSERT
        assertTrue(issues.isEmpty(), "Empty property sets are never duplicates")
    }

    @Test
    fun `pass when one property set is a strict subset of another`() {
        // ARRANGE
        val node = nodeWithIndexes(
            "idx1" to setOf("email", "name"),
            "idx2" to setOf("email", "name", "age")
        )

        // ACT
        val issues = validate(node)

        // ASSERT
        assertTrue(issues.isEmpty(), "A subset is not an exact match")
    }

    @Test
    fun `pass when the node has a single index`() {
        // ARRANGE
        val node = nodeWithIndexes("idx1" to setOf("email"))

        // ACT
        val issues = validate(node)

        // ASSERT
        assertTrue(issues.isEmpty(), "A lone index cannot duplicate anything")
    }
}
