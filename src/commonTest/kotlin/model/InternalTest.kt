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

        assertTrue(node0.constraints.containsKey("node0_constraint0"))
        assertEquals("c1", node0.constraints["node0_constraint0"]?.name)

        assertTrue(node0.indexes.containsKey("node0_index0"))
        assertEquals("i1", node0.indexes["node0_index0"]?.name)
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
                    fromNode = TargetMapping(
                        node = "Person",
                        properties = mutableMapOf("age" to PropertyMapping("from_age"))
                    ),
                    toNode = TargetMapping(
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
        assertTrue(internalNode.properties.containsKey("node0_property0"))

        val internalRel = originalModel.relationships["relationship0"]!!
        assertEquals("FRIENDS_WITH", internalRel.name)
        assertTrue(internalRel.properties.containsKey("relationship0_property0"))

        // Assert Mappings Deep Translation
        val relMapping = originalModel.mappings.filterIsInstance<RelationshipMapping>().first()
        assertEquals("relationship0", relMapping.relationship)
        assertEquals("node0", relMapping.fromNode.node)
        assertTrue(
            relMapping.fromNode.properties.containsKey("node0_property0"),
            "From Target property should be renamed"
        )
        assertTrue(
            relMapping.properties.containsKey("relationship0_property0"),
            "Relationship property should be renamed"
        )
        assertEquals("node0", relMapping.toNode.node)
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
        val property = node.properties["node0_property0"]!!
        assertNull(property.key, "Key flag should be cleared from the property")

        val constraint = node.constraints["node0_constraint0"]
        assertNotNull(constraint, "A key constraint should be generated for the property")
        assertEquals(ConstraintType.KEY, constraint.type)
        assertEquals("User", constraint.label, "Constraint should reference the node's label")
        assertEquals(mutableSetOf("node0_property0"), constraint.properties)
        assertEquals("id_User_key", constraint.name)
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
        val property = node.properties["node0_property0"]!!
        assertNull(property.unique, "Unique flag should be cleared from the property")

        val constraint = node.constraints["node0_constraint0"]
        assertNotNull(constraint, "A unique constraint should be generated for the property")
        assertEquals(ConstraintType.UNIQUE, constraint.type)
        assertEquals(mutableSetOf("node0_property0"), constraint.properties)
        assertEquals("email_uniq", constraint.name)
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
        val property = node.properties["node0_property0"]!!
        assertNull(property.mustExist, "MustExist flag should be cleared from the property")

        val constraint = node.constraints["node0_constraint0"]
        assertNotNull(constraint, "An exists constraint should be generated for the property")
        assertEquals(ConstraintType.EXISTS, constraint.type)
        assertEquals(mutableSetOf("node0_property0"), constraint.properties)
        assertEquals("email_exists", constraint.name)
    }

    @Test
    fun `test keeps an explicit node constraint alongside a shorthand one`() {
        val model = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(
                "User" to Node(
                    constraints = mutableMapOf(
                        "c1" to NodeConstraint(ConstraintType.UNIQUE, properties = mutableSetOf())
                    ),
                    properties = mutableMapOf("id" to Property(key = true))
                )
            ),
            pretty = true
        )

        model.internalise()

        val constraints = model.nodes["node0"]!!.constraints
        assertEquals(2, constraints.size, "Both constraints should survive internalise")
        assertEquals("c1", constraints["node0_constraint0"]?.name)
        assertEquals(ConstraintType.UNIQUE, constraints["node0_constraint0"]?.type)
        assertEquals(ConstraintType.KEY, constraints["node0_constraint1"]?.type)
        assertEquals("id_key", constraints["node0_constraint1"]?.name)
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
        val property = relationship.properties["relationship0_property0"]!!
        assertNull(property.key, "Key flag should be cleared from the property")

        val constraint: RelationshipConstraint? = relationship.constraints["relationship0_constraint0"]
        assertNotNull(constraint, "A key constraint should be generated for the property")
        assertEquals(ConstraintType.KEY, constraint.type)
        assertEquals(mutableSetOf("relationship0_property0"), constraint.properties)
        assertEquals("since_KNOWS_key", constraint.name)
    }

    @Test
    fun `test keeps an explicit relationship constraint alongside a shorthand one`() {
        val model = GraphModel(
            version = "1.0",
            relationships = mutableMapOf(
                "KNOWS" to Relationship(
                    type = "KNOWS",
                    from = RelationshipTarget(),
                    to = RelationshipTarget(),
                    constraints = mutableMapOf(
                        "c1" to RelationshipConstraint(ConstraintType.UNIQUE, mutableSetOf())
                    ),
                    properties = mutableMapOf("since" to Property(key = true))
                )
            ),
            pretty = true
        )

        model.internalise()

        val constraints = model.relationships["relationship0"]!!.constraints
        assertEquals(2, constraints.size, "Both constraints should survive internalise")
        assertEquals("c1", constraints["relationship0_constraint0"]?.name)
        assertEquals(ConstraintType.UNIQUE, constraints["relationship0_constraint0"]?.type)
        assertEquals(ConstraintType.KEY, constraints["relationship0_constraint1"]?.type)
        assertEquals("since_KNOWS_key", constraints["relationship0_constraint1"]?.name)
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

    @Test
    fun `test internalise twice leaves ids and names alone`() {
        val model = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(
                "User" to Node(
                    label = "User",
                    properties = mutableMapOf(
                        "id" to Property(key = true),
                        "email" to Property(unique = true)
                    ),
                    constraints = mutableMapOf(
                        "c1" to NodeConstraint(
                            ConstraintType.UNIQUE,
                            label = "User",
                            properties = mutableSetOf("email"),
                            name = "my_custom_name"
                        )
                    ),
                    indexes = mutableMapOf(
                        "i1" to NodeIndex(IndexType.TEXT, mutableSetOf("User"), mutableSetOf("email"))
                    )
                )
            ),
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
        val once = idsAndNames(model)
        model.internalise()

        assertEquals(once, idsAndNames(model), "A second internalise must not renumber or rename anything")
    }

    private fun idsAndNames(model: GraphModel): String = buildString {
        for ((nodeId, node) in model.nodes) {
            appendLine("node $nodeId name=${node.name} label=${node.labels.identifier}")
            node.properties.entries.forEach { (id, property) -> appendLine("  property $id name=${property.name}") }
            node.constraints.entries.forEach { (id, c) -> appendLine("  constraint $id name=${c.name} type=${c.type}") }
            node.indexes.entries.forEach { (id, index) -> appendLine("  index $id name=${index.name}") }
        }
        for ((relationshipId, relationship) in model.relationships) {
            appendLine("relationship $relationshipId name=${relationship.name}")
            relationship.properties.entries.forEach { (id, property) ->
                appendLine("  property $id name=${property.name}")
            }
            relationship.constraints.entries.forEach { (id, c) ->
                appendLine("  constraint $id name=${c.name} type=${c.type}")
            }
            relationship.indexes.entries.forEach { (id, index) -> appendLine("  index $id name=${index.name}") }
        }
    }

    @Test
    fun `test keeps shorthand constraints whose property tokens differ only by a space`() {
        val model = GraphModel(
            version = "1.0",
            nodes = mutableMapOf(
                "User" to Node(
                    label = "User",
                    properties = mutableMapOf(
                        "a b" to Property(key = true),
                        "a_b" to Property(key = true)
                    )
                )
            ),
            pretty = true
        )

        model.internalise()

        val node = model.nodes["node0"]!!
        assertEquals(2, node.constraints.size, "Both constraints must survive internalise")
        assertEquals(
            setOf("node0_constraint0", "node0_constraint1"),
            node.constraints.keys,
            "Both constraints are numbered, since neither is named when ids are assigned"
        )
        assertEquals(
            setOf("a b_User_key", "a__b_User_key"),
            node.constraints.values.mapNotNull { it.name }.toSet(),
            "A space is kept and an underscore is doubled, so the two names stay distinct"
        )
    }
}
