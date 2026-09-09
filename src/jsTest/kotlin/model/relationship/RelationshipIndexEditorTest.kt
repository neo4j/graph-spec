package model.relationship
import js.objects.Object.Companion.keys
import kotlin.test.*
import js.objects.get
import model.extension.StringValue
import model.extension.toJs

class RelationshipIndexEditorTest {

    @Test
    fun testRelationshipIndexJsFactory() {
        val type = "BTREE"
        val properties = arrayOf("prop1")

        val index = relationshipIndexJs(
            type = type,
            properties = properties
        )

        assertEquals(type, index.type)
        assertEquals(1, index.properties.size)
        assertEquals("prop1", index.properties[0])
        assertNull(index.options)
        assertNotNull(index.extensions)
    }

    @Test
    fun testSetType() {
        val index = relationshipIndexJs("OLD_TYPE", arrayOf())
        RelationshipIndexEditor.setType(index, "NEW_TYPE")

        assertEquals("NEW_TYPE", index.type)
    }

    @Test
    fun testAddProperty() {
        val index = relationshipIndexJs("test", arrayOf("existing"))

        // Add new property
        RelationshipIndexEditor.addProperty(index, "new_prop")
        assertEquals(2, index.properties.size)
        assertTrue(index.properties.contains("new_prop"))

        // Attempt to add duplicate property (should be ignored by the logic)
        RelationshipIndexEditor.addProperty(index, "existing")
        assertEquals(2, index.properties.size, "Should not add duplicate properties")
    }

    @Test
    fun testRemoveProperty() {
        val index = relationshipIndexJs("test", arrayOf("p1", "p2", "p3"))

        // Remove existing property
        RelationshipIndexEditor.removeProperty(index, "p2")
        assertEquals(2, index.properties.size)
        assertFalse(index.properties.contains("p2"))
        assertEquals("p1", index.properties[0])
        assertEquals("p3", index.properties[1])

        // Remove non-existent property
        RelationshipIndexEditor.removeProperty(index, "not_here")
        assertEquals(2, index.properties.size)
    }

    @Test
    fun testPropertiesArrayImmutability() {
        // This tests that the editor correctly re-assigns the array reference
        // (Since Kotlin/JS += on Arrays creates a new array)
        val index = relationshipIndexJs("test", arrayOf("a"))
        val originalRef = index.properties

        RelationshipIndexEditor.addProperty(index, "b")

        assertNotSame(originalRef, index.properties, "The properties array reference should be updated")
        assertEquals(2, index.properties.size)
    }
}
