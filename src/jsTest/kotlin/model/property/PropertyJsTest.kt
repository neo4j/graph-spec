package model.property

import model.mapping.JsMappingTest
import model.extension.BooleanValue
import model.extension.StringValue
import model.property.PropertyExtension
import model.extension.booleanValueJs
import model.extension.stringValueJs
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyJsTest : JsMappingTest<Property, PropertyJs>() {

    override fun createClass() = Property(
        type = Neo4jType.VECTOR_FLOAT,
        dimension = 8,
        mustExist = true,
        unique = true,
        key = true,
        extensions = PropertyExtension(mutableMapOf("key1" to StringValue("val1"))),
        name = "propertyName",
        reference = "ref",
        pattern = "^a.*",
        oneOf = mutableListOf(StringValue("a"), BooleanValue(true))
    )

    override fun toJs(k: Property): PropertyJs = k.toJs("propertyId")

    override fun toClass(js: PropertyJs): Property = js.toClass("parent", "propertyId")

    override fun verifyJsObject(jsObject: PropertyJs) {
        assertEquals("VECTOR<FLOAT>", jsObject.type)
        assertEquals(8, jsObject.dimension)
        assertTrue(jsObject.mustExist!!)
        assertTrue(jsObject.unique!!)
        assertTrue(jsObject.key!!)
        assertJsEquals(stringValueJs("val1"), jsObject.extensions.custom["key1"])
        assertEquals("propertyId", jsObject.id)
        assertEquals("propertyName", jsObject.name)
        assertEquals("ref", jsObject.reference)
        assertEquals("^a.*", jsObject.pattern)
        assertEquals(2, jsObject.oneOf.size)
        assertJsEquals(stringValueJs("a"), jsObject.oneOf[0])
        assertJsEquals(booleanValueJs(true), jsObject.oneOf[1])
    }

}
