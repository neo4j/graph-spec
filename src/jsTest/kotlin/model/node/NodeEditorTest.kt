package model.node

import js.objects.recordOf
import model.GraphModelJs
import model.graphModelJs
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class NodeEditorTest {

    private lateinit var model: GraphModelJs
    private val nodeId = "node-1"

    @BeforeTest
    fun setup() {
        val initialNode = nodeJs(
            label = "Initial Label",
            id = nodeId
        )

        model = graphModelJs(
            id = "model-1",
            version = 1,
            nodes = recordOf(nodeId to initialNode),
        )
    }

    @Test
    fun testSetLabel() {
        NodeEditor.setLabel(model, nodeId, "Updated Label")
        assertEquals("Updated Label", model.nodes[nodeId]?.label)
    }

    @Test
    fun testLabelOperations() {
        // Test Identifying Label
        NodeEditor.setIdentifyingLabel(model, nodeId, "Person")
        assertEquals("Person", model.nodes[nodeId]?.labels?.identifier)

        // Test Implied Labels
        NodeEditor.addImpliedLabel(model, nodeId, "Entity")
        assertEquals(true, model.nodes[nodeId]?.labels?.implied?.contains("Entity"))

        NodeEditor.removeImpliedLabel(model, nodeId, "Entity")
        assertNotEquals(true, model.nodes[nodeId]?.labels?.implied?.contains("Entity"))

        // Test Optional Labels
        NodeEditor.addOptionalLabel(model, nodeId, "Student")
        assertEquals(true, model.nodes[nodeId]?.labels?.optional?.contains("Student"))

        NodeEditor.removeOptionalLabel(model, nodeId, "Student")
        assertNotEquals(true, model.nodes[nodeId]?.labels?.optional?.contains("Student"))
    }

    @Test
    fun testPropertyLifecycle() {
        // Add Property
        val propId = NodeEditor.addProperty(model, nodeId)
        assertNotNull(model.nodes[nodeId]?.properties?.get(propId))

        // Set Property Attributes
        NodeEditor.setPropertyType(model, nodeId, propId, "INTEGER")
        NodeEditor.setPropertyDimension(model, nodeId, propId, 123)
        NodeEditor.setPropertyMustExist(model, nodeId, propId, false)
        NodeEditor.setPropertyUnique(model, nodeId, propId, true)

        val prop = model.nodes[nodeId]?.properties?.get(propId)
        assertEquals("INTEGER", prop?.type)
        assertEquals(123, prop?.dimension)

        NodeEditor.setPropertyDimension(model, nodeId, propId, null)
        assertNull(model.nodes[nodeId]?.properties?.get(propId)?.dimension)

        // Remove Property
        NodeEditor.removeProperty(model, nodeId, propId)
        assertNull(model.nodes[nodeId]?.properties?.get(propId))
    }

    @Test
    fun testConstraintOperations() {
        val constraintIndex = NodeEditor.addConstraint(
            model = model,
            nodeId = nodeId,
            type = "unique",
            name = "User"
        )

        val node = model.nodes[nodeId]!!
        assertNotNull(node.constraints[constraintIndex])
        assertEquals("unique", node.constraints[constraintIndex].type)

        // Update Name
        NodeEditor.setConstraintName(model, nodeId, constraintIndex, "Admin")
        assertEquals("Admin", node.constraints[constraintIndex].name)

        // Update Type
        NodeEditor.setConstraintType(model, nodeId, constraintIndex, "key")
        assertEquals("key", node.constraints[constraintIndex].type)

        // Property Management
        NodeEditor.addConstraintProperty(model, nodeId, constraintIndex, "email")
        assertEquals(true, node.constraints[constraintIndex].properties.contains("email"))

        NodeEditor.removeConstraintProperty(model, nodeId, constraintIndex, "email")
        assertNotEquals(true, node.constraints[constraintIndex].properties.contains("email"))
    }

    @Test
    fun testErrorHandling() {
        // Verify that passing a non-existent nodeId throws an exception (via getOrThrow)
        assertFails {
            NodeEditor.setLabel(model, "non-existent-id", "New Label")
        }

        // Verify that passing a non-existent propertyId throws an exception
        assertFails {
            NodeEditor.setPropertyType(model, nodeId, "fake-prop", "STRING")
        }
    }
}
