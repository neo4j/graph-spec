package org.neo4j.spec.graph.model.extension.source

import org.neo4j.spec.graph.model.extension.mapping.JsMappingTest
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ForeignKeyReferenceJsTest : JsMappingTest<ForeignKeyReference, ForeignKeyReferenceJs>() {

    override fun createClass() = ForeignKeyReference(
        table = "table_name",
        columns = mutableListOf("field1", "field2"),
        custom = mutableMapOf("key1" to "val1"),
    )

    override fun toJs(k: ForeignKeyReference): ForeignKeyReferenceJs = k.toJs()

    override fun toClass(js: ForeignKeyReferenceJs): ForeignKeyReference = js.toClass()

    override fun verifyJsObject(jsObject: ForeignKeyReferenceJs) {
        assertEquals("table_name", jsObject.table)
        assertContentEquals(arrayOf("field1", "field2"), jsObject.columns)
        assertEquals<Any?>("val1", jsObject.custom["key1"])
    }

}
