package model.relationship

import model.JsMappingTest
import model.extension.StringValue
import model.extension.stringValueJs
import model.node.Constraint
import model.property.Property
import model.property.propertyJs
import model.type.ConstraintType
import kotlin.test.assertEquals

class RelationshipJsTest : JsMappingTest<Relationship, RelationshipJs>() {

    override fun createClass() = Relationship(
        type = "RELATIONSHIP_TYPE",
        from = RelationshipTarget("from_node"),
        to = RelationshipTarget("to_node", count = 1),
        properties = mutableMapOf("prop" to Property(type = "STRING")),
        constraints = mutableListOf(Constraint(ConstraintType.KEY, mutableListOf("prop"))),
        extensions = mutableMapOf("key1" to StringValue("val1")),
    )

    override fun toJs(k: Relationship): RelationshipJs = k.toJs("relationshipId")

    override fun toClass(js: RelationshipJs): Relationship = js.toClass()

    override fun verifyJsObject(jsObject: RelationshipJs) {
        assertEquals("RELATIONSHIP_TYPE", jsObject.type)
        assertEquals("from_node", jsObject.from.node)
        assertEquals("to_node", jsObject.to.node)
        assertEquals(1, jsObject.to.count)
        assertJsEquals(propertyJs("STRING", id = "prop"), jsObject.properties["prop"])
        assertJsEquals(relationshipConstraintJs("key", properties = arrayOf("prop")), jsObject.constraints[0])
        assertJsEquals(stringValueJs("val1"), jsObject.extensions["key1"])
        assertEquals("relationshipId", jsObject.id)
    }

}
