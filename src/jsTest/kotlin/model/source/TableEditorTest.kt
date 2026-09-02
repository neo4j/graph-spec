package model.source

import kotlin.test.*

class TableEditorTest {

    @Test
    fun testSetSource() {
        val table = tableJs(source = "public.users")
        TableEditor.setSource(table, "private.users")
        assertEquals("private.users", table.source)
    }

    @Test
    fun testAddAndRemoveColumn() {
        val table = tableJs(source = "users")

        // Test Add
        val columnId = TableEditor.addColumn(table, "STRING")
        assertTrue(columnId.isNotEmpty(), "Add column should return an ID.")

        // Assert column exists using dynamic access for JS plain object
        val addedColumn = table.columns[columnId]
        assertNotNull(addedColumn, "Column should exist in Record.")
        assertEquals("STRING", addedColumn.type)

        // Test Remove
        TableEditor.removeColumn(table, columnId)
        val removedColumn = table.columns[columnId]
        assertNull(removedColumn, "Column should be removed from Record.")
    }

    @Test
    fun testSetColumnType() {
        val table = tableJs(source = "users")
        val columnId = TableEditor.addColumn(table, "STRING")

        TableEditor.setColumnType(table, columnId, "INTEGER")

        val column = table.columns[columnId]
        assertEquals("INTEGER", column?.type)
    }

    @Test
    fun testSetColumnSize() {
        val table = tableJs(source = "users")
        val columnId = TableEditor.addColumn(table, "STRING")

        TableEditor.setColumnSize(table, columnId, 128)

        val column = table.columns[columnId]
        assertEquals(128, column?.size)
    }

    @Test
    fun testAddPrimaryKey_whenNew_addsKey() {
        val table = tableJs("users")
        TableEditor.addPrimaryKey(table, "id")

        assertEquals(1, table.primaryKeys.size)
        assertTrue(table.primaryKeys.contains("id"))
    }

    @Test
    fun testAddPrimaryKey_whenDuplicate_doesNotAdd() {
        val table = tableJs("users", primaryKeys = arrayOf("id"))
        TableEditor.addPrimaryKey(table, "id")

        assertEquals(1, table.primaryKeys.size)
    }

    @Test
    fun testRemovePrimaryKey_whenExisting_removesKey() {
        val table = tableJs("users", primaryKeys = arrayOf("id", "uuid"))
        TableEditor.removePrimaryKey(table, "id")

        assertEquals(1, table.primaryKeys.size)
        assertFalse(table.primaryKeys.contains("id"))
    }

    @Test
    fun testRemovePrimaryKey_whenNonExisting_doesNothing() {
        val table = tableJs("users", primaryKeys = arrayOf("id"))
        TableEditor.removePrimaryKey(table, "uuid")

        assertEquals(1, table.primaryKeys.size)
    }

    @Test
    fun testAddAndRemoveForeignKey() {
        val table = tableJs("users")
        val ref = foreignKeyReferenceJs("orgs", arrayOf("org_id"))

        // Test Add
        val fkId = TableEditor.addForeignKey(table, arrayOf("org_id"), ref)
        assertTrue(fkId.isNotEmpty())

        val addedFk = table.foreignKeys[fkId]
        assertNotNull(addedFk, "Foreign key should exist in Record.")

        // Test Remove
        TableEditor.removeForeignKey(table, fkId)
        val removedFk = table.foreignKeys[fkId]
        assertNull(removedFk, "Foreign key should be removed from Record.")
    }
}
