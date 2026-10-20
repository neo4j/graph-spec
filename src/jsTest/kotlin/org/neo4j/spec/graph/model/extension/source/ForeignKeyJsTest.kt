package org.neo4j.spec.graph.model.extension.source

import org.neo4j.spec.graph.model.extension.mapping.JsMappingTest
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ForeignKeyJsTest : JsMappingTest<ForeignKey, ForeignKeyJs>() {

    override fun createClass() = ForeignKey(
        columns = mutableListOf("field1", "field2"),
        references = ForeignKeyReference("table"),
        custom = mutableMapOf("key1" to "val1"),
    )

    override fun toJs(k: ForeignKey): ForeignKeyJs = k.toJs()

    override fun toClass(js: ForeignKeyJs): ForeignKey = js.toClass()

    override fun verifyJsObject(jsObject: ForeignKeyJs) {
        assertContentEquals(arrayOf("field1", "field2"), jsObject.columns)
        assertEquals("table", jsObject.references.table)
        assertEquals<Any?>("val1", jsObject.custom["key1"])
    }

}
