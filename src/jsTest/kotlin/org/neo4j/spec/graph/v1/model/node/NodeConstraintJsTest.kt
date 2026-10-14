package org.neo4j.spec.graph.v1.model.node

import org.neo4j.spec.graph.v1.model.node.extension.NodeConstraintExtensions
import org.neo4j.spec.graph.v1.model.node.extension.toClass
import org.neo4j.spec.graph.v1.model.node.extension.toJs
import org.neo4j.spec.graph.v1.model.extension.mapping.JsMappingTest
import org.neo4j.spec.graph.v1.model.value.StringValue
import org.neo4j.spec.graph.v1.model.value.stringValueJs
import org.neo4j.spec.graph.v1.model.type.ConstraintType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeConstraintJsTest : JsMappingTest<NodeConstraint, NodeConstraintJs>() {

    override fun createClass() = NodeConstraint(
        type = ConstraintType.KEY,
        label = "node_label",
        properties = mutableSetOf("property_1", "property_2"),
        extensions = NodeConstraintExtensions(
            mutableMapOf("key1" to StringValue("val1"))
        )
    )

    override fun toJs(k: NodeConstraint): NodeConstraintJs = k.toJs()

    override fun toClass(js: NodeConstraintJs): NodeConstraint = js.toClass()

    override fun verifyJsObject(jsObject: NodeConstraintJs) {
        assertEquals("KEY", jsObject.type)
        assertEquals("node_label", jsObject.label)
        assertEquals(2, jsObject.properties.size)
        assertTrue(jsObject.properties.contains("property_1"))
        assertTrue(jsObject.properties.contains("property_2"))
        assertJsEquals(stringValueJs("val1"), jsObject.extensions.custom["key1"])
    }

}
