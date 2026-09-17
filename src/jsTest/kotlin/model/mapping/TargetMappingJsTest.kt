package model.mapping

import kotlin.test.assertEquals

class TargetMappingJsTest : JsMappingTest<TargetMapping, TargetMappingJs>() {

    override fun createClass() = TargetMapping(
        properties = mutableMapOf("prop" to PropertyMapping("field")),
    )

    override fun toJs(k: TargetMapping): TargetMappingJs = k.toJs()

    override fun toClass(js: TargetMappingJs): TargetMapping = js.toClass()

    override fun verifyJsObject(jsObject: TargetMappingJs) {
        assertJsEquals(propertyMappingJs("field"), jsObject.properties["prop"])
    }

}
