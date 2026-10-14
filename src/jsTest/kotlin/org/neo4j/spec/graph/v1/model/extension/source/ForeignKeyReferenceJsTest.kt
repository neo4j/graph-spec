package org.neo4j.spec.graph.v1.model.extension.source

import org.neo4j.spec.graph.v1.model.extension.mapping.JsMappingTest
import org.neo4j.spec.graph.v1.model.value.StringValue
import org.neo4j.spec.graph.v1.model.value.stringValueJs
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ForeignKeyReferenceJsTest : JsMappingTest<ForeignKeyReference, ForeignKeyReferenceJs>() {

    override fun createClass() = ForeignKeyReference(
        table = "table_name",
        columns = mutableSetOf("field1", "field2"),
        custom = mutableMapOf("key1" to StringValue("val1")),
    )

    override fun toJs(k: ForeignKeyReference): ForeignKeyReferenceJs = k.toJs()

    override fun toClass(js: ForeignKeyReferenceJs): ForeignKeyReference = js.toClass()

    override fun verifyJsObject(jsObject: ForeignKeyReferenceJs) {
        assertEquals("table_name", jsObject.table)
        assertContentEquals(arrayOf("field1", "field2"), jsObject.columns)
        assertJsEquals(stringValueJs("val1"), jsObject.custom["key1"])
    }

}
