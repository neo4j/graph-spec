package model.property

import model.mapping.JsMappingTest
import model.extension.StringValue
import model.extension.stringValueJs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class PropertyJsTest : JsMappingTest<Property, PropertyJs>() {

    override fun createClass() = Property(
        type = Neo4jType.BOOLEAN,
        // dimension is meaningless for a boolean but we preserve rather than strip it
        dimension = 8,
        mustExist = true,
        unique = true,
        key = true,
        extensions = mutableMapOf("key1" to StringValue("val1")),
        name = "propertyName"
    )

    override fun toJs(k: Property): PropertyJs = k.toJs("propertyId")

    override fun toClass(js: PropertyJs): Property = js.toClass("parent", "propertyId")

    override fun verifyJsObject(jsObject: PropertyJs) {
        assertEquals("BOOLEAN", jsObject.type)
        assertEquals(8, jsObject.dimension)
        assertTrue(jsObject.mustExist!!)
        assertTrue(jsObject.unique!!)
        assertTrue(jsObject.key!!)
        assertJsEquals(stringValueJs("val1"), jsObject.extensions["key1"])
        assertEquals("propertyId", jsObject.id)
        assertEquals("propertyName", jsObject.name)
    }

    @Test
    fun testVectorRoundTrip() {
        val property = Property(type = Neo4jType.VECTOR_FLOAT, dimension = 1536, name = "embedding")

        val jsObject = property.toJs("embedding")

        assertEquals("VECTOR<FLOAT>", jsObject.type)
        assertEquals(1536, jsObject.dimension)
        assertEquals(property, jsObject.toClass("parent", "embedding"))
    }

    @Test
    fun testMissingDimensionRoundTrip() {
        val property = Property(type = Neo4jType.VECTOR_FLOAT, name = "embedding")

        val jsObject = property.toJs("embedding")

        assertNull(jsObject.dimension)
        assertEquals(property, jsObject.toClass("parent", "embedding"))
    }

}
