package model.relationship

import model.relationship.extension.RelationshipIndex
import model.relationship.extension.relationshipIndexJs
import model.relationship.extension.toClass
import model.relationship.extension.toJs
import model.extension.mapping.JsMappingTest
import model.value.StringValue
import model.relationship.extension.RelationshipExtensions
import model.value.stringValueJs
import model.property.Neo4jType
import model.property.Property
import model.property.propertyJs
import model.type.ConstraintType
import model.type.IndexType
import kotlin.test.assertEquals

class RelationshipJsTest : JsMappingTest<Relationship, RelationshipJs>() {

    override fun createClass() = Relationship(
        type = "RELATIONSHIP_TYPE",
        from = RelationshipTarget("from_node"),
        to = RelationshipTarget("to_node"),
        properties = mutableMapOf("prop" to Property(Neo4jType.STRING, name = "property_name")),
        constraints = mutableMapOf("constraint" to RelationshipConstraint(ConstraintType.KEY, mutableSetOf("prop"))),
        extensions = RelationshipExtensions(
            custom = mutableMapOf("key1" to StringValue("val1")),
            indexes = mutableMapOf("index" to RelationshipIndex(IndexType.POINT, mutableSetOf("prop")))
        ),
        name = "relationshipName",
        aliases = mutableSetOf("alias1", "alias2"),
        reference = "rel-ref"
    )

    override fun toJs(k: Relationship): RelationshipJs = k.toJs("relationshipId")

    override fun toClass(js: RelationshipJs): Relationship = js.toClass("relationshipId")

    override fun verifyJsObject(jsObject: RelationshipJs) {
        assertEquals("RELATIONSHIP_TYPE", jsObject.type)
        assertEquals("from_node", jsObject.from.node)
        assertEquals("to_node", jsObject.to.node)
        assertJsEquals(propertyJs("STRING", id = "prop", name = "property_name"), jsObject.properties["prop"])
        assertJsEquals(relationshipConstraintJs("KEY", arrayOf("prop")), jsObject.constraints["constraint"])
        assertJsEquals(relationshipIndexJs("POINT", arrayOf("prop")), jsObject.extensions.indexes["index"])
        assertJsEquals(stringValueJs("val1"), jsObject.extensions.custom["key1"])
        assertEquals("relationshipId", jsObject.id)
        assertEquals("relationshipName", jsObject.name)
        assertEquals(listOf("alias1", "alias2"), jsObject.aliases.toList())
        assertEquals("rel-ref", jsObject.reference)
    }

}
