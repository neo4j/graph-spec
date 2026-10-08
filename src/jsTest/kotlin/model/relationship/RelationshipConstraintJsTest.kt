package model.relationship

import model.relationship.extension.RelationshipConstraintExtensions
import model.relationship.extension.toClass
import model.relationship.extension.toJs
import model.extension.mapping.JsMappingTest
import model.value.StringValue
import model.value.stringValueJs
import model.type.ConstraintType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationshipConstraintJsTest : JsMappingTest<RelationshipConstraint, RelationshipConstraintJs>() {

    override fun createClass() = RelationshipConstraint(
        type = ConstraintType.UNIQUE,
        properties = mutableSetOf("property_1", "property_2"),
        extensions = RelationshipConstraintExtensions(
            mutableMapOf("key1" to StringValue("val1"))
        )
    )

    override fun toJs(k: RelationshipConstraint): RelationshipConstraintJs = k.toJs()

    override fun toClass(js: RelationshipConstraintJs): RelationshipConstraint = js.toClass()

    override fun verifyJsObject(jsObject: RelationshipConstraintJs) {
        assertEquals("UNIQUE", jsObject.type)
        assertEquals(2, jsObject.properties.size)
        assertTrue(jsObject.properties.contains("property_1"))
        assertTrue(jsObject.properties.contains("property_2"))
        assertJsEquals(stringValueJs("val1"), jsObject.extensions.custom["key1"])
    }

}
