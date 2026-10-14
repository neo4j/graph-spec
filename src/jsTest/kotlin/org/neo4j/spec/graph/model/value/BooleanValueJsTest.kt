package org.neo4j.spec.graph.model.value

import org.neo4j.spec.graph.model.extension.mapping.JsMappingTest
import kotlin.test.assertEquals

class BooleanValueJsTest : JsMappingTest<BooleanValue, BooleanValueJs>() {

    override fun createClass() = BooleanValue(
        value = true
    )

    override fun toJs(k: BooleanValue): BooleanValueJs = k.toJs()

    override fun toClass(js: BooleanValueJs): BooleanValue = js.toClass()

    override fun verifyJsObject(jsObject: BooleanValueJs) {
        assertEquals("BOOLEAN", jsObject.type)
        assertEquals(true, jsObject.value)
    }

}
