package model.extension.mapping

import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class RelationshipMappingJsTest : JsMappingTest<RelationshipMapping, RelationshipMappingJs>() {

    override fun createClass() = RelationshipMapping(
        relationship = "relationshipId",
        table = "table_name",
        fromNode = TargetMapping("from_node"),
        toNode = TargetMapping(label = "to_label"),
        properties = mutableMapOf("prop" to PropertyMapping("field")),
        mode = MappingMode.MERGE,
        matchLabel = "matchLabel",
        key = mutableSetOf("key"),
    )

    override fun toJs(k: RelationshipMapping): RelationshipMappingJs = k.toJs()

    override fun toClass(js: RelationshipMappingJs): RelationshipMapping = js.toClass()

    override fun verifyJsObject(jsObject: RelationshipMappingJs) {
        assertEquals("RELATIONSHIP", jsObject.type)
        assertEquals("table_name", jsObject.table)
        assertEquals("from_node", jsObject.fromNode.node)
        assertEquals("to_label", jsObject.toNode.label)
        assertJsEquals(propertyMappingJs("field"), jsObject.properties["prop"])
        assertEquals("MERGE", jsObject.mode)
        assertEquals("matchLabel", jsObject.matchLabel)
        assertContentEquals(arrayOf("key"), jsObject.key)
    }

}
