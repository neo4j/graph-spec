package model.source

import model.mapping.JsMappingTest
import model.property.Neo4jType
import model.extension.StringValue
import model.extension.stringValueJs
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNull

class TableFieldJsTest : JsMappingTest<TableField, TableFieldJs>() {

    override fun createClass() = TableField(
        type = "field_type",
        size = 10,
        suggested = Neo4jType.STRING,
        supported = setOf(Neo4jType.STRING, Neo4jType.INTEGER),
        // dimension is meaningless for a string but we preserve rather than strip it
        dimension = 8,
        extensions = mutableMapOf("key1" to StringValue("val1")),
        name = "Field name"
    )

    override fun toJs(k: TableField): TableFieldJs = k.toJs("fieldId")

    override fun toClass(js: TableFieldJs): TableField = js.toClass()

    override fun verifyJsObject(jsObject: TableFieldJs) {
        assertEquals("field_type", jsObject.type)
        assertEquals(10, jsObject.size)
        assertEquals("STRING", jsObject.suggested)
        assertContentEquals(arrayOf("STRING", "INTEGER"), jsObject.supported)
        assertEquals(8, jsObject.dimension)
        assertJsEquals(stringValueJs("val1"), jsObject.extensions["key1"])
        assertEquals("Field name", jsObject.name)
    }

    @Test
    fun testVectorRoundTrip() {
        val field = TableField(
            type = "VECTOR",
            suggested = Neo4jType.VECTOR_FLOAT,
            supported = setOf(Neo4jType.VECTOR_FLOAT, Neo4jType.VECTOR_FLOAT32),
            dimension = 1536,
            name = "embedding"
        )

        val jsObject = field.toJs("embedding")

        assertEquals(1536, jsObject.dimension)
        assertEquals(field, jsObject.toClass())
    }

    @Test
    fun testMissingDimensionRoundTrip() {
        val field = TableField(type = "VECTOR", suggested = Neo4jType.VECTOR_FLOAT, name = "embedding")

        val jsObject = field.toJs("embedding")

        assertNull(jsObject.dimension)
        assertEquals(field, jsObject.toClass())
    }

}
