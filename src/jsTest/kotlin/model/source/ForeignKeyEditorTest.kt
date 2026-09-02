package model.source

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ForeignKeyEditorTest {

    @Test
    fun testAddColumn_whenNew_addsColumn() {
        val fk = foreignKeyJs(arrayOf("user_id"), foreignKeyReferenceJs("users"))
        ForeignKeyEditor.addColumn(fk, "org_id")

        assertEquals(2, fk.columns.size)
        assertTrue(fk.columns.contains("org_id"))
    }

    @Test
    fun testAddColumn_whenDuplicate_doesNotAdd() {
        val fk = foreignKeyJs(arrayOf("user_id"), foreignKeyReferenceJs("users"))
        ForeignKeyEditor.addColumn(fk, "user_id")

        assertEquals(1, fk.columns.size)
    }

    @Test
    fun testRemoveColumn_whenExisting_removesColumn() {
        val fk = foreignKeyJs(arrayOf("user_id", "org_id"), foreignKeyReferenceJs("users"))
        ForeignKeyEditor.removeColumn(fk, "user_id")

        assertEquals(1, fk.columns.size)
        assertFalse(fk.columns.contains("user_id"))
    }

    @Test
    fun testRemoveColumn_whenNonExisting_doesNothing() {
        val fk = foreignKeyJs(arrayOf("user_id"), foreignKeyReferenceJs("users"))
        ForeignKeyEditor.removeColumn(fk, "non_existent")

        assertEquals(1, fk.columns.size)
    }

    @Test
    fun testSetReferenceTable() {
        val fk = foreignKeyJs(emptyArray(), foreignKeyReferenceJs("users"))
        ForeignKeyEditor.setReferenceTable(fk, "accounts")

        assertEquals("accounts", fk.references.table)
    }

    @Test
    fun testAddReferenceColumn() {
        val fk = foreignKeyJs(emptyArray(), foreignKeyReferenceJs("users", arrayOf("id")))
        ForeignKeyEditor.addReferenceColumn(fk, "uuid")

        assertEquals(2, fk.references.columns.size)
        assertTrue(fk.references.columns.contains("uuid"))
    }

    @Test
    fun testRemoveReferenceColumn() {
        val fk = foreignKeyJs(emptyArray(), foreignKeyReferenceJs("users", arrayOf("id", "uuid")))
        ForeignKeyEditor.removeReferenceColumn(fk, "id")

        assertEquals(1, fk.references.columns.size)
        assertFalse(fk.references.columns.contains("id"))
    }
}
