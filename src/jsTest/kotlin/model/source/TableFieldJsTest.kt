package model.source

import model.mapping.JsMappingTest
import model.property.Neo4jScalar
import model.property.ScalarType
import model.extension.StringValue
import model.extension.stringValueJs
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class TableFieldJsTest : JsMappingTest<TableField, TableFieldJs>() {

    override fun createClass() = TableField(
        type = "field_type",
        size = 10,
        suggested = ScalarType(Neo4jScalar.STRING),
        supported = setOf(ScalarType(Neo4jScalar.STRING), ScalarType(Neo4jScalar.INTEGER)),
        extensions = mutableMapOf("key1" to StringValue("val1")),
        name = "Field name"
    )

    override fun toJs(k: TableField): TableFieldJs = k.toJs("fieldId")

    override fun toClass(js: TableFieldJs): TableField = js.toClass()

    override fun verifyJsObject(jsObject: TableFieldJs) {
        assertEquals("field_type", jsObject.type)
        assertEquals(10, jsObject.size)
        assertEquals("STRING", jsObject.suggested.type)
        assertContentEquals(arrayOf("STRING", "INTEGER"), jsObject.supported.map { it.type }.toTypedArray())
        assertJsEquals(stringValueJs("val1"), jsObject.extensions["key1"])
        assertEquals("Field name", jsObject.name)
    }

}
