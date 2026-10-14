package org.neo4j.spec.graph.v1.model.node.extension

import org.neo4j.spec.graph.v1.model.node.toClass
import org.neo4j.spec.graph.v1.model.node.toJs
import org.neo4j.spec.graph.v1.model.extension.mapping.JsMappingTest
import org.neo4j.spec.graph.v1.model.value.StringValue
import org.neo4j.spec.graph.v1.model.value.stringValueJs
import org.neo4j.spec.graph.v1.model.index.VectorIndexOption
import org.neo4j.spec.graph.v1.model.index.VectorIndexOptionJs
import org.neo4j.spec.graph.v1.model.type.IndexType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeIndexJsTest : JsMappingTest<NodeIndex, NodeIndexJs>() {

    override fun createClass() = NodeIndex(
        type = IndexType.TEXT,
        labels = mutableSetOf("node_label"),
        properties = mutableSetOf("property_1", "property_2"),
        options = VectorIndexOption(
            similarityFunction = "test",
        ),
        custom = mutableMapOf(
            "key1" to StringValue("val1")
        )
    )

    override fun toJs(k: NodeIndex): NodeIndexJs = k.toJs()

    override fun toClass(js: NodeIndexJs): NodeIndex = js.toClass()

    override fun verifyJsObject(jsObject: NodeIndexJs) {
        assertEquals("TEXT", jsObject.type)
        val labels = jsObject.labels
        assertEquals(1, labels.size)
        assertTrue(labels.contains("node_label"))
        assertEquals(2, jsObject.properties.size)
        assertTrue(jsObject.properties.contains("property_1"))
        assertTrue(jsObject.properties.contains("property_2"))
        assertJsEquals("test", (jsObject.options as? VectorIndexOptionJs)?.similarityFunction)
        assertJsEquals(stringValueJs("val1"), jsObject.custom["key1"])
    }

}
