package org.neo4j.spec.graph.model.relationship

import kotlin.test.*
import js.objects.recordOf
import org.neo4j.spec.graph.model.GraphModelJs
import org.neo4j.spec.graph.model.graphModelJs
class RelationshipEditorTest {

    private lateinit var model: GraphModelJs
    private val relId = "rel-123"

    @BeforeTest
    fun setup() {
        val relationship = relationshipJs(
            type = "WORKS_AT",
            id = relId
        )
        model = graphModelJs(
            version = "1",
            id = "test",
            relationships = recordOf(relId to relationship)
        )
    }

    @Test
    fun testBasicMetadataUpdates() {
        val rel = relationshipJs(type = "A", id = "1")

        RelationshipEditor.setType(rel, "B")

        assertEquals("B", rel.type)
    }

    @Test
    fun testSourceTargetDelegation() {
        val rel = relationshipJs(type = "T", id = "1")

        // Test Source (from) delegation
        RelationshipEditor.setSourceNode(rel, "Node1")
        assertEquals("Node1", rel.from.node)
        assertEquals("", rel.from.label) // RelationshipTargetEditor logic: setting node clears label

        RelationshipEditor.setSourceLabel(rel, "Label1")
        assertEquals("Label1", rel.from.label)
        assertEquals("", rel.from.node) // RelationshipTargetEditor logic: setting label clears node

    }

    @Test
    fun testPropertyManagement() {
        val rel = model.relationships[relId]!!

        // Add Property
        val propId = RelationshipEditor.addProperty(model, relId)
        assertNotNull(rel.properties[propId])
        assertNull(rel.properties[propId]?.name)

        // Update Property
        RelationshipEditor.setPropertyName(model, relId, propId, "NewName")
        RelationshipEditor.setPropertyType(model, relId, propId, "VECTOR<FLOAT>")
        RelationshipEditor.setPropertyDimension(model, relId, propId, 123)
        RelationshipEditor.setPropertyMustExist(model, relId, propId, true)

        val prop = rel.properties[propId]!!
        assertEquals("NewName", prop.name)
        assertEquals("VECTOR<FLOAT>", prop.type)
        assertEquals(123, prop.dimension)
        assertTrue(prop.mustExist!!)

        RelationshipEditor.setPropertyDimension(model, relId, propId, null)
        assertNull(rel.properties[propId]?.dimension)

        // Remove Property
        RelationshipEditor.removeProperty(model, relId, propId)
        assertNull(rel.properties[propId])
    }

    @Test
    fun testConstraintManagement() {
        // Add Constraint
        val constraintId = RelationshipEditor.addConstraint(
            model, relId, type = "UNIQUE", properties = arrayOf("p1")
        )
        val constraint = model.relationships[relId]!!.constraints[constraintId]!!
        assertEquals("UNIQUE", constraint.type)
        assertTrue(constraint.properties.contains("p1"))

        // Modify Constraint Properties
        RelationshipEditor.addConstraintProperty(model, relId, constraintId, "p2")
        assertTrue(constraint.properties.contains("p2"))
        assertEquals(2, constraint.properties.size)

        RelationshipEditor.removeConstraintProperty(model, relId, constraintId, "p1")
        assertFalse(constraint.properties.contains("p1"))
    }

    @Test
    fun testIndexManagement() {
        // Add Index
        val indexId = RelationshipEditor.addIndex(model, relId, "RANGE", arrayOf("a"))
        val index = model.relationships[relId]!!.extensions.indexes[indexId]!!

        // Update Type
        RelationshipEditor.setIndexType(model, relId, indexId, "POINT")
        assertEquals("POINT", index.type)

        // Add Property
        RelationshipEditor.addIndexProperty(model, relId, indexId, "b")
        assertEquals(2, index.properties.size)
    }

    @Test
    fun testErrorHandling() {
        // Test that operations on non-existent relationships throw as expected
        assertFails {
            RelationshipEditor.setType(relationshipJs(type="", id=""), "new")
            // The above won't fail, but the Model ones will:
            RelationshipEditor.addProperty(model, "non-existent-id")
        }
    }
}
