package model.relationship

import kotlin.test.*
import js.objects.recordOf
import model.GraphModelJs
import model.graphModelJs

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
            id = "model-1",
            version = 1,
            relationships = recordOf(relId to relationship)
        )
    }

    @Test
    fun testBasicMetadataUpdates() {
        val rel = relationshipJs(type = "A", id = "1")

        RelationshipEditor.setType(rel, "B")
        RelationshipEditor.setDescription(rel, "Description B")

        assertEquals("B", rel.type)
        assertEquals("Description B", rel.description)
    }

    @Test
    fun testSourceTargetDelegation() {
        val rel = relationshipJs(type = "T", id = "1")

        // Test Source (from) delegation
        RelationshipEditor.setSourceNode(rel, "Node1")
        assertEquals("Node1", rel.from.node)

        // Test Target (to) delegation
        RelationshipEditor.setTargetNode(rel, "Node2")
        assertEquals("Node2", rel.to.node)
    }

    @Test
    fun testPropertyManagement() {
        val rel = model.relationships[relId]!!

        // Add Property
        val propId = RelationshipEditor.addProperty(model, relId)
        assertNotNull(rel.properties[propId])

        // Update Property
        RelationshipEditor.setPropertyType(model, relId, propId, "VECTOR<FLOAT>")
        RelationshipEditor.setPropertyDimension(model, relId, propId, 123)
        RelationshipEditor.setPropertyMustExist(model, relId, propId, true)

        val prop = rel.properties[propId]!!
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
        val constraintIndex = RelationshipEditor.addConstraint(
            model, relId, type = "unique", properties = arrayOf("p1")
        )
        val constraint = model.relationships[relId]!!.constraints[constraintIndex]
        assertEquals("unique", constraint.type)
        assertTrue(constraint.properties.contains("p1"))

        // Modify Constraint Name
        RelationshipEditor.setConstraintName(model, relId, constraintIndex, "named")
        assertEquals("named", constraint.name)

        // Modify Constraint Properties
        RelationshipEditor.addConstraintProperty(model, relId, constraintIndex, "p2")
        assertTrue(constraint.properties.contains("p2"))
        assertEquals(2, constraint.properties.size)

        RelationshipEditor.removeConstraintProperty(model, relId, constraintIndex, "p1")
        assertFalse(constraint.properties.contains("p1"))
    }

    @Test
    fun testErrorHandling() {
        // Test that operations on non-existent relationships throw as expected
        assertFails {
            RelationshipEditor.addProperty(model, "non-existent-id")
        }
    }
}
