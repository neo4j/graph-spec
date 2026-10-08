package model.node

import model.node.extension.LabelsExtensions
import model.node.extension.toClass
import model.node.extension.toJs
import model.extension.mapping.JsMappingTest
import model.value.StringValue
import model.value.stringValueJs
import kotlin.test.assertEquals

class LabelsJsTest : JsMappingTest<Labels, LabelsJs>() {

    override fun createClass() = Labels(
        identifier = "test-id",
        implied = mutableSetOf("a", "b"),
        optional = mutableSetOf("c"),
        extensions = LabelsExtensions(mutableMapOf("key1" to StringValue("val1")))
    )

    override fun toJs(k: Labels): LabelsJs = k.toJs()

    override fun toClass(js: LabelsJs): Labels = js.toClass()

    override fun verifyJsObject(jsObject: LabelsJs) {
        assertEquals("test-id", jsObject.identifier)
        assertEquals(2, jsObject.implied.size)
        assertEquals("a", jsObject.implied[0])
        assertJsEquals(stringValueJs("val1"), jsObject.extensions.custom["key1"])
    }

}
