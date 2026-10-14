package org.neo4j.spec.graph.v1.model.node

import org.neo4j.spec.graph.v1.model.node.extension.LabelsExtensions
import org.neo4j.spec.graph.v1.model.node.extension.toClass
import org.neo4j.spec.graph.v1.model.node.extension.toJs
import org.neo4j.spec.graph.v1.model.extension.mapping.JsMappingTest
import org.neo4j.spec.graph.v1.model.value.StringValue
import org.neo4j.spec.graph.v1.model.value.stringValueJs
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
