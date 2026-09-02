package model.source

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ForeignKeyReferenceEditorTest {

    @Test
    fun testSetTable() {
        val reference = foreignKeyReferenceJs(table = "users")
        ForeignKeyReferenceEditor.setTable(reference, "accounts")
        assertEquals("accounts", reference.table, "Table reference should be updated.")
    }

    @Test
    fun testAddField_whenNew_addsField() {
        val reference = foreignKeyReferenceJs(table = "users", columns = arrayOf("id"))
        ForeignKeyReferenceEditor.addColumn(reference, "uuid")

        assertEquals(2, reference.columns.size)
        assertTrue(reference.columns.contains("uuid"), "New field should be added.")
    }

    @Test
    fun testAddField_whenDuplicate_doesNotAdd() {
        val reference = foreignKeyReferenceJs(table = "users", columns = arrayOf("id"))
        ForeignKeyReferenceEditor.addColumn(reference, "id")

        assertEquals(1, reference.columns.size, "Duplicate field should not be added.")
    }

    @Test
    fun testRemoveField_whenExisting_removesField() {
        val reference = foreignKeyReferenceJs(table = "users", columns = arrayOf("id", "uuid"))
        ForeignKeyReferenceEditor.removeColumn(reference, "id")

        assertEquals(1, reference.columns.size)
        assertFalse(reference.columns.contains("id"), "Existing field should be removed.")
        assertTrue(reference.columns.contains("uuid"))
    }

    @Test
    fun testRemoveField_whenNonExisting_doesNothing() {
        val reference = foreignKeyReferenceJs(table = "users", columns = arrayOf("id"))
        ForeignKeyReferenceEditor.removeColumn(reference, "uuid") // Doesn't exist

        assertEquals(1, reference.columns.size, "Array size should remain unchanged.")
        assertTrue(reference.columns.contains("id"))
    }
}
