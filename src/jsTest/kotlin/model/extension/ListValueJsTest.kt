package model.extension

import model.mapping.JsMappingTest
import kotlin.test.assertEquals

class ListValueJsTest : JsMappingTest<ListValue, ListValueJs>() {

    override fun createClass() = ListValue(
        value = mutableListOf(StringValue("val1"), LongValue(4))
    )

    override fun toJs(k: ListValue): ListValueJs = k.toJs()

    override fun toClass(js: ListValueJs): ListValue = js.toClass()

    override fun verifyJsObject(jsObject: ListValueJs) {
        assertEquals("LIST", jsObject.type)
        val (first, second) = jsObject.value
        assertEquals("STRING", first.type)
        assertEquals("val1", (first as StringValueJs).value)
        assertEquals("LONG", second.type)
        assertEquals(4, (second as LongValueJs).value)
    }

}
