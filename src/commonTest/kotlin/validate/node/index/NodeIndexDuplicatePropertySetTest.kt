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
import model.node.NodeConstraint
import model.node.NodeIndex
import model.property.Property
import model.type.ConstraintType
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
        val node = nodeWithIndexes("idx1" to setOf("email", "name"), "idx2" to setOf("email"))

        val issues = validate(node)

        assertTrue(issues.isEmpty(), "Expected no issues when index property sets are distinct")
    }

    @Test
    fun `fail when two indexes share the same property set`() {
        val node = nodeWithIndexes("idx1" to setOf("email", "name"), "idx2" to setOf("email", "name"))

        val issues = validate(node)

        assertEquals(2, issues.size)
        assertEquals("duplicate_index_property_set", issues.first().code)
        assertEquals("nodes.personNode.indexes.idx1.properties", issues.first().path)
    }

    @Test
    fun `fail when property sets are equal in different order`() {
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

        val issues = validate(node)

        assertEquals(2, issues.size)
    }

    @Test
    fun `fail when two single-property indexes share the same property`() {
        val node = nodeWithIndexes("idx1" to setOf("email"), "idx2" to setOf("email"))

        val issues = validate(node)

        assertEquals(2, issues.size)
        assertEquals("duplicate_index_property_set", issues.first().code)
    }

    @Test
    fun `fail for every index in a group of three duplicates`() {
        val node = nodeWithIndexes(
            "idx1" to setOf("email"),
            "idx2" to setOf("email"),
            "idx3" to setOf("email")
        )

        val issues = validate(node)

        assertEquals(3, issues.size)
    }

    @Test
    fun `pass when index property sets are empty`() {
        val node = nodeWithIndexes("idx1" to emptySet(), "idx2" to emptySet())

        val issues = validate(node)

        assertTrue(issues.isEmpty(), "Empty property sets are never duplicates")
    }

    @Test
    fun `pass when one property set is a strict subset of another`() {
        val node = nodeWithIndexes(
            "idx1" to setOf("email", "name"),
            "idx2" to setOf("email", "name", "age")
        )

        val issues = validate(node)

        assertTrue(issues.isEmpty(), "A subset is not an exact match")
    }

    @Test
    fun `pass when the node has a single index`() {
        val node = nodeWithIndexes("idx1" to setOf("email"))

        val issues = validate(node)

        assertTrue(issues.isEmpty(), "A lone index cannot duplicate anything")
    }

    @Test
    fun `fail when index property set matches a unique constraint's set`() {
        val node = nodeWithIndexes("idx1" to setOf("email", "name"))
        node.constraints["uniq_email_name"] = NodeConstraint(
            type = ConstraintType.UNIQUE,
            label = "Person",
            properties = mutableSetOf("email", "name"),
            name = "uniq_email_name"
        )

        val issues = validate(node)

        assertEquals(1, issues.size)
        assertEquals("duplicate_index_property_set", issues.first().code)
        assertEquals("nodes.personNode.indexes.idx1.properties", issues.first().path)
    }

    @Test
    fun `fail when index property set matches a key constraint's set in any order`() {
        val node = nodeWithIndexes("idx1" to setOf("email", "name"))
        node.constraints["key1"] = NodeConstraint(
            type = ConstraintType.KEY,
            label = "Person",
            properties = mutableSetOf("name", "email"),
            name = "key1"
        )

        val issues = validate(node)

        assertEquals(1, issues.size)
    }

    @Test
    fun `pass when the matching constraint is an existence constraint`() {
        val node = nodeWithIndexes("idx1" to setOf("email"))
        node.constraints["exists_email"] = NodeConstraint(
            type = ConstraintType.EXISTS,
            label = "Person",
            properties = mutableSetOf("email"),
            name = "exists_email"
        )

        val issues = validate(node)

        assertTrue(issues.isEmpty(), "Existence constraints never imply an index")
    }

    @Test
    fun `pass when the matching constraint has no name`() {
        val node = nodeWithIndexes("idx1" to setOf("email"))
        node.constraints["uniq_email"] = NodeConstraint(
            type = ConstraintType.UNIQUE,
            label = "Person",
            properties = mutableSetOf("email"),
            name = null
        )

        val issues = validate(node)

        assertTrue(issues.isEmpty(), "Unnamed constraints are not valid for index view")
    }

    @Test
    fun `pass when the matching constraint has empty properties`() {
        val node = nodeWithIndexes("idx1" to setOf("email"))
        node.constraints["uniq_empty"] = NodeConstraint(
            type = ConstraintType.UNIQUE,
            label = "Person",
            properties = mutableSetOf(),
            name = "uniq_empty"
        )

        val issues = validate(node)

        assertTrue(issues.isEmpty(), "Empty constraints are not valid for index view")
    }

    @Test
    fun `pass when the matching constraint is itself a duplicate composite`() {
        val node = nodeWithIndexes("idx1" to setOf("email", "name"))
        node.constraints["uniq1"] = NodeConstraint(
            type = ConstraintType.UNIQUE,
            label = "Person",
            properties = mutableSetOf("email", "name"),
            name = "uniq1"
        )
        node.constraints["uniq2"] = NodeConstraint(
            type = ConstraintType.UNIQUE,
            label = "Person",
            properties = mutableSetOf("email", "name"),
            name = "uniq2"
        )

        val issues = validate(node)

        assertTrue(issues.isEmpty(), "Duplicate composite constraints are excluded from index view")
    }
}
