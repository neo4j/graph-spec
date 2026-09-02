package model.source

import kotlin.test.Test
import kotlin.test.assertEquals

class TableColumnEditorTest {

    @Test
    fun testSetType() {
        val column = tableColumnJs(type = "STRING")
        TableColumnEditor.setType(column, "INTEGER")
        assertEquals("INTEGER", column.type, "Column type should be updated.")
    }

    @Test
    fun testSetSize() {
        val column = tableColumnJs(type = "STRING", size = 255)
        TableColumnEditor.setSize(column, 500)
        assertEquals(500, column.size, "Column size should be updated.")
    }
}
