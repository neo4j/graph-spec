package model.extension.source

import model.extension.mapping.JsMappingTest
import model.value.StringValue
import model.value.stringValueJs
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class ForeignKeyJsTest : JsMappingTest<ForeignKey, ForeignKeyJs>() {

    override fun createClass() = ForeignKey(
        columns = mutableSetOf("field1", "field2"),
        references = ForeignKeyReference("table"),
        custom = mutableMapOf("key1" to StringValue("val1")),
    )

    override fun toJs(k: ForeignKey): ForeignKeyJs = k.toJs()

    override fun toClass(js: ForeignKeyJs): ForeignKey = js.toClass()

    override fun verifyJsObject(jsObject: ForeignKeyJs) {
        assertContentEquals(arrayOf("field1", "field2"), jsObject.columns)
        assertEquals("table", jsObject.references.table)
        assertJsEquals(stringValueJs("val1"), jsObject.custom["key1"])
    }

}
