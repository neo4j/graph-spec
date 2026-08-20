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
package model

import model.mapping.NodeMapping
import model.mapping.PropertyMapping
import model.mapping.RelationshipMapping
import model.mapping.TargetMapping
import model.node.Node
import model.node.NodeConstraint
import model.node.NodeIndex
import model.property.Property
import model.relationship.Relationship
import model.relationship.RelationshipConstraint
import model.relationship.RelationshipTarget
import model.type.ConstraintType
import model.type.IndexType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class InternalTest {

    @Test
    fun `test moves node label to identifier`() {
        val model = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(
                "User" to Node(label = "UserLabel")
            ),
            pretty = true
        )

        model.internalise()

        val internalNode = model.nodes["node0"]
        assertNotNull(internalNode, "Node should be renamed to stable id 'node0'")
        assertNull(internalNode.label, "Label should be moved to Labels object and set to null")
        assertEquals("UserLabel", internalNode.labels.identifier, "Identifier should hold the original label")
        assertEquals("User", internalNode.name, "Original map key should be moved to name property")
    }

    @Test
    fun `test increments IDs correctly for multiple nodes and children`() {
        val model = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(
                "User1" to Node(
                    constraints = mutableMapOf(
                        "c1" to NodeConstraint(
                            ConstraintType.UNIQUE,
                            properties = mutableSetOf()
                        )
                    ),
                    indexes = mutableMapOf("i1" to NodeIndex(IndexType.TEXT, mutableSetOf(), mutableSetOf()))
                ),
                "User2" to Node()
            ),
            pretty = true
        )

        model.internalise()

        assertTrue(model.nodes.containsKey("node0"), "First node should be node0")
        assertTrue(model.nodes.containsKey("node1"), "Second node should be node1")

        val node0 = model.nodes["node0"]!!
        assertEquals("User1", node0.name)

        assertTrue(node0.constraints.containsKey("nodeConstraint0"))
        assertEquals("c1", node0.constraints["nodeConstraint0"]?.name)

        assertTrue(node0.indexes.containsKey("nodeIndex0"))
        assertEquals("i1", node0.indexes["nodeIndex0"]?.name)
    }

    @Test
    fun `test translates mappings and deep properties correctly`() {
        val originalModel = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(
                "Person" to Node(
                    label = "Person",
                    properties = mutableMapOf("age" to Property())
                )
            ),
            relationships = mutableMapOf(
                "FRIENDS_WITH" to Relationship(
                    type = "KNOWS",
                    from = RelationshipTarget(),
                    to = RelationshipTarget(),
                    properties = mutableMapOf("since" to Property())
                )
            ),
            mappings = mutableListOf(
                RelationshipMapping(
                    relationship = "FRIENDS_WITH",
                    table = "friends_table",
                    from = TargetMapping(
                        node = "Person",
                        properties = mutableMapOf("age" to PropertyMapping("from_age"))
                    ),
                    to = TargetMapping(
                        node = "Person"
                    ),
                    properties = mutableMapOf(
                        "since" to PropertyMapping("friends_since")
                    )
                )
            ),
            pretty = true
        )

        originalModel.internalise()

        // Assert Nodes and Properties
        val internalNode = originalModel.nodes["node0"]!!
        assertEquals("Person", internalNode.name)
        assertTrue(internalNode.properties.containsKey("nodeProperty0"))

        val internalRel = originalModel.relationships["relationship0"]!!
        assertEquals("FRIENDS_WITH", internalRel.name)
        assertTrue(internalRel.properties.containsKey("relationshipProperty0"))

        // Assert Mappings Deep Translation
        val relMapping = originalModel.mappings.filterIsInstance<RelationshipMapping>().first()
        assertEquals("relationship0", relMapping.relationship)
        assertEquals("node0", relMapping.from.node)
        assertTrue(relMapping.from.properties.containsKey("nodeProperty0"), "From Target property should be renamed")
        assertTrue(
            relMapping.properties.containsKey("relationshipProperty0"),
            "Relationship property should be renamed"
        )
        assertEquals("node0", relMapping.to.node)
    }

    @Test
    fun `test ignores missing mapping references gracefully`() {
        val model = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(),
            mappings = mutableListOf(
                NodeMapping(node = "GhostNode", table = "ghosts", properties = mutableMapOf())
            )
        )

        model.internalise()

        val mapping = model.mappings.first() as NodeMapping
        assertEquals("GhostNode", mapping.node)
    }

    @Test
    fun `test converts key property flag into a node key constraint`() {
        val model = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(
                "User" to Node(
                    label = "User",
                    properties = mutableMapOf("id" to Property(key = true))
                )
            ),
            pretty = true
        )

        model.internalise()

        val node = model.nodes["node0"]!!
        val property = node.properties["nodeProperty0"]!!
        assertNull(property.key, "Key flag should be cleared from the property")

        val constraint = node.constraints["key_constraint_nodeProperty0"]
        assertNotNull(constraint, "A key constraint should be generated for the property")
        assertEquals(ConstraintType.KEY, constraint.type)
        assertEquals("User", constraint.label, "Constraint should reference the node's label")
        assertEquals(mutableSetOf("nodeProperty0"), constraint.properties)
    }

    @Test
    fun `test converts unique property flag into a node unique constraint`() {
        val model = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(
                "User" to Node(
                    properties = mutableMapOf("email" to Property(unique = true))
                )
            ),
            pretty = true
        )

        model.internalise()

        val node = model.nodes["node0"]!!
        val property = node.properties["nodeProperty0"]!!
        assertNull(property.unique, "Unique flag should be cleared from the property")

        val constraint = node.constraints["unique_constraint_nodeProperty0"]
        assertNotNull(constraint, "A unique constraint should be generated for the property")
        assertEquals(ConstraintType.UNIQUE, constraint.type)
        assertEquals(mutableSetOf("nodeProperty0"), constraint.properties)
    }

    @Test
    fun `test converts mustExist property flag into a node exists constraint`() {
        val model = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(
                "User" to Node(
                    properties = mutableMapOf("email" to Property(mustExist = true))
                )
            ),
            pretty = true
        )

        model.internalise()

        val node = model.nodes["node0"]!!
        val property = node.properties["nodeProperty0"]!!
        assertNull(property.mustExist, "MustExist flag should be cleared from the property")

        val constraint = node.constraints["exists_constraint_nodeProperty0"]
        assertNotNull(constraint, "An exists constraint should be generated for the property")
        assertEquals(ConstraintType.EXISTS, constraint.type)
        assertEquals(mutableSetOf("nodeProperty0"), constraint.properties)
    }

    @Test
    fun `test converts key property flag into a relationship key constraint`() {
        val model = GraphModel(
            version = "1.0",
            relationships = mutableMapOf(
                "KNOWS" to Relationship(
                    type = "KNOWS",
                    from = RelationshipTarget(),
                    to = RelationshipTarget(),
                    properties = mutableMapOf("since" to Property(key = true))
                )
            ),
            pretty = true
        )

        model.internalise()

        val relationship = model.relationships["relationship0"]!!
        val property = relationship.properties["relationshipProperty0"]!!
        assertNull(property.key, "Key flag should be cleared from the property")

        val constraint: RelationshipConstraint? = relationship.constraints["key_constraint_relationshipProperty0"]
        assertNotNull(constraint, "A key constraint should be generated for the property")
        assertEquals(ConstraintType.KEY, constraint.type)
        assertEquals(mutableSetOf("relationshipProperty0"), constraint.properties)
    }

    @Test
    fun `test leaves properties without flags unconstrained`() {
        val model = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(
                "User" to Node(
                    properties = mutableMapOf("name" to Property())
                )
            ),
            pretty = true
        )

        model.internalise()

        val node = model.nodes["node0"]!!
        assertTrue(node.constraints.isEmpty(), "No constraint should be generated for an unflagged property")
    }
}
