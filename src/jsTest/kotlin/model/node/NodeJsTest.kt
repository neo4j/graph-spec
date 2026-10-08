package model.node

import model.node.extension.NodeDisplay
import model.node.extension.NodeExtensions
import model.node.extension.NodeIndex
import model.node.extension.nodeIndexJs
import model.node.extension.toClass
import model.node.extension.toJs
import model.extension.mapping.JsMappingTest
import model.value.StringValue
import model.value.stringValueJs
import model.property.Neo4jType
import model.property.Property
import model.property.propertyJs
import model.type.ConstraintType
import model.type.IndexType
import kotlin.test.assertEquals

class NodeJsTest : JsMappingTest<Node, NodeJs>() {

    override fun createClass() = Node(
        labels = Labels("label"),
        properties = mutableMapOf("prop" to Property(Neo4jType.STRING, name = "propertyName")),
        constraints = mutableMapOf("constraint" to NodeConstraint(ConstraintType.EXISTS, "label", mutableSetOf("prop"))),
        extensions = NodeExtensions(
            custom = mutableMapOf("key1" to StringValue("val1")),
            indexes = mutableMapOf("index" to NodeIndex(IndexType.RANGE, mutableSetOf("label"), mutableSetOf("prop"))),
            display = NodeDisplay(1.0, 2.0)
        ),
        name = "Node Name",
        aliases = mutableSetOf("alias1", "alias2"),
        reference = "node-ref"
    )

    override fun toJs(k: Node): NodeJs = k.toJs("nodeId")

    override fun toClass(js: NodeJs): Node = js.toClass("nodeId")

    override fun verifyJsObject(jsObject: NodeJs) {
        assertEquals("label", jsObject.labels.identifier)
        assertJsEquals(propertyJs("STRING", id = "prop", name = "propertyName"), jsObject.properties["prop"])
        assertJsEquals(nodeConstraintJs("EXISTS", "label", arrayOf("prop")), jsObject.constraints["constraint"])
        assertJsEquals(nodeIndexJs("RANGE", arrayOf("label"), arrayOf("prop")), jsObject.extensions.indexes["index"])
        assertJsEquals(stringValueJs("val1"), jsObject.extensions.custom["key1"])
        assertEquals(1.0, jsObject.extensions.display?.x)
        assertEquals(2.0, jsObject.extensions.display?.y)
        assertEquals("nodeId", jsObject.id)
        assertEquals(listOf("alias1", "alias2"), jsObject.aliases.toList())
        assertEquals("node-ref", jsObject.reference)
    }

}
