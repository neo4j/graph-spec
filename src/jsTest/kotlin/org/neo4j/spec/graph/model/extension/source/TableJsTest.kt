package org.neo4j.spec.graph.model.extension.source

import org.neo4j.spec.graph.model.extension.mapping.JsMappingTest
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TableJsTest : JsMappingTest<Table, TableJs>() {

    override fun createClass() = Table(
        source = "sourceId",
        columns = mutableListOf(TableColumn(name = "field", type = "varchar")),
        primaryKeys = mutableSetOf("field"),
        foreignKeys = mutableMapOf("key" to ForeignKey(mutableSetOf("key"), ForeignKeyReference("table"))),
        custom = mutableMapOf("key1" to "val1"),
    )

    override fun toJs(k: Table): TableJs = k.toJs()

    override fun toClass(js: TableJs): Table = js.toClass()

    override fun verifyJsObject(jsObject: TableJs) {
        assertEquals("sourceId", jsObject.source)
        assertJsEquals(tableColumnJs("field", "varchar"), jsObject.columns[0])
        assertContentEquals(arrayOf("field"), jsObject.primaryKeys)
        assertJsEquals(foreignKeyJs(arrayOf("key"), foreignKeyReferenceJs("table")), jsObject.foreignKeys["key"])
        assertEquals<Any?>("val1", jsObject.custom["key1"])
    }

}
