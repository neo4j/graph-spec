package model.relationship

import model.JsMappingTest
import model.node.Constraint
import model.type.ConstraintType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationshipConstraintJsTest : JsMappingTest<Constraint, RelationshipConstraintJs>() {

    override fun createClass() = Constraint(
        type = ConstraintType.UNIQUE,
        properties = mutableListOf("property_1", "property_2"),
        name = "relationship_constraint"
    )

    override fun toJs(k: Constraint): RelationshipConstraintJs = k.toJs()

    override fun toClass(js: RelationshipConstraintJs): Constraint = js.toClass()

    override fun verifyJsObject(jsObject: RelationshipConstraintJs) {
        assertEquals("unique", jsObject.type)
        assertEquals("relationship_constraint", jsObject.name)
        assertEquals(2, jsObject.properties.size)
        assertTrue(jsObject.properties.contains("property_1"))
        assertTrue(jsObject.properties.contains("property_2"))
    }

}
