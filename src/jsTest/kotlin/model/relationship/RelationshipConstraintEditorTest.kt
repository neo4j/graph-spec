package model.relationship

import kotlin.test.*

class RelationshipConstraintEditorTest {

    @Test
    fun testFactoryFunctionInitialization() {
        val type = "unique"
        val properties = arrayOf("prop1", "prop2")

        val constraint = relationshipConstraintJs(
            type = type,
            properties = properties
        )

        assertEquals(type, constraint.type)
        assertEquals(2, constraint.properties.size)
        assertTrue(constraint.properties.contains("prop1"))
        assertNull(constraint.name)
    }

    @Test
    fun testSetType() {
        val constraint = relationshipConstraintJs(type = "unique")
        RelationshipConstraintEditor.setType(constraint, "mustExist")

        assertEquals("mustExist", constraint.type)
    }

    @Test
    fun testSetName() {
        val constraint = relationshipConstraintJs(type = "unique")
        RelationshipConstraintEditor.setName(constraint, "named")
        assertEquals("named", constraint.name)

        RelationshipConstraintEditor.setName(constraint, null)
        assertNull(constraint.name)
    }

    @Test
    fun testAddProperty() {
        val constraint = relationshipConstraintJs(type = "test")

        // Add first property
        RelationshipConstraintEditor.addProperty(constraint, "id")
        assertEquals(1, constraint.properties.size)
        assertEquals("id", constraint.properties[0])

        // Add second property
        RelationshipConstraintEditor.addProperty(constraint, "name")
        assertEquals(2, constraint.properties.size)
        assertTrue(constraint.properties.contains("name"))

        // Test Duplicate Prevention: Adding "id" again should not increase size
        RelationshipConstraintEditor.addProperty(constraint, "id")
        assertEquals(2, constraint.properties.size, "Should not add duplicate properties")
    }

    @Test
    fun testRemoveProperty() {
        val constraint = relationshipConstraintJs(
            type = "test",
            properties = arrayOf("a", "b", "c")
        )

        // Remove middle element
        RelationshipConstraintEditor.removeProperty(constraint, "b")
        assertEquals(2, constraint.properties.size)
        assertFalse(constraint.properties.contains("b"))
        assertEquals("a", constraint.properties[0])
        assertEquals("c", constraint.properties[1])

        // Remove non-existent element
        RelationshipConstraintEditor.removeProperty(constraint, "non-existent")
        assertEquals(2, constraint.properties.size)
    }

    @Test
    fun testEmptyDefaults() {
        val constraint = relationshipConstraintJs(type = "minimal")

        assertNotNull(constraint.properties)
        assertEquals(0, constraint.properties.size)
    }
}
