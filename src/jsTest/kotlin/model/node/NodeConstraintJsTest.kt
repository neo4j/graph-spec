package model.node

import model.JsMappingTest
import model.type.ConstraintType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeConstraintJsTest : JsMappingTest<Constraint, NodeConstraintJs>() {

    override fun createClass() = Constraint(
        type = ConstraintType.KEY,
        properties = mutableListOf("property_1", "property_2"),
        name = "node_constraint"
    )

    override fun toJs(k: Constraint): NodeConstraintJs = k.toJs()

    override fun toClass(js: NodeConstraintJs): Constraint = js.toClass()

    override fun verifyJsObject(jsObject: NodeConstraintJs) {
        assertEquals("key", jsObject.type)
        assertEquals("node_constraint", jsObject.name)
        assertEquals(2, jsObject.properties.size)
        assertTrue(jsObject.properties.contains("property_1"))
        assertTrue(jsObject.properties.contains("property_2"))
    }

}
