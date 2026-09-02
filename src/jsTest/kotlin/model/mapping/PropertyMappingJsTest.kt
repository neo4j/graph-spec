package model.mapping

import kotlin.test.assertEquals

class PropertyMappingJsTest : JsMappingTest<PropertyMapping, PropertyMappingJs>() {

    override fun createClass() = PropertyMapping(
        column = "field_name",
    )

    override fun toJs(k: PropertyMapping): PropertyMappingJs = k.toJs()

    override fun toClass(js: PropertyMappingJs): PropertyMapping = js.toClass()

    override fun verifyJsObject(jsObject: PropertyMappingJs) {
        assertEquals("field_name", jsObject.column)
    }

}
