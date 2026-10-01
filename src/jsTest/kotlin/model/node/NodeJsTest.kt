package model.node

import model.JsMappingTest
import model.extension.StringValue
import model.extension.stringValueJs
import model.property.Property
import model.property.propertyJs
import model.tool.Tool
import model.tool.toolJs
import model.type.ConstraintType
import kotlin.test.assertEquals

class NodeJsTest : JsMappingTest<Node, NodeJs>() {

    override fun createClass() = Node(
        label = "label",
        properties = mutableMapOf("prop" to Property(type = "STRING")),
        constraints = mutableListOf(Constraint(ConstraintType.UNIQUE, mutableListOf("prop"), name = "constraint")),
        tools = mutableListOf(Tool("canonicalQuery", name = "tool")),
        extensions = mutableMapOf("key1" to StringValue("val1")),
    )

    override fun toJs(k: Node): NodeJs = k.toJs("nodeId")

    override fun toClass(js: NodeJs): Node = js.toClass()

    override fun verifyJsObject(jsObject: NodeJs) {
        assertEquals("label", jsObject.label)
        assertJsEquals(propertyJs("STRING", id = "prop"), jsObject.properties["prop"])
        assertJsEquals(nodeConstraintJs("unique", "constraint", arrayOf("prop")), jsObject.constraints[0])
        assertJsEquals(toolJs("canonicalQuery", name = "tool"), jsObject.tools[0])
        assertJsEquals(stringValueJs("val1"), jsObject.extensions["key1"])
        assertEquals("nodeId", jsObject.id)
    }

}
