package org.neo4j.spec.graph.v1.model.relationship.extension

import org.neo4j.spec.graph.v1.model.relationship.toClass
import org.neo4j.spec.graph.v1.model.relationship.toJs
import org.neo4j.spec.graph.v1.model.extension.mapping.JsMappingTest
import org.neo4j.spec.graph.v1.model.value.StringValue
import org.neo4j.spec.graph.v1.model.value.stringValueJs
import org.neo4j.spec.graph.v1.model.index.FullTextIndexOption
import org.neo4j.spec.graph.v1.model.index.FullTextIndexOptionJs
import org.neo4j.spec.graph.v1.model.index.fullTextIndexOptionJs
import org.neo4j.spec.graph.v1.model.index.pointIndexOptionJs
import org.neo4j.spec.graph.v1.model.type.IndexType
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationshipIndexJsTest : JsMappingTest<RelationshipIndex, RelationshipIndexJs>() {

    override fun createClass() = RelationshipIndex(
        type = IndexType.POINT,
        properties = mutableSetOf("property_1", "property_2"),
        options = FullTextIndexOption(
            analyzer = "test"
        ),
        custom = mutableMapOf(
            "key1" to StringValue("val1")
        )
    )

    override fun toJs(k: RelationshipIndex): RelationshipIndexJs = k.toJs()

    override fun toClass(js: RelationshipIndexJs): RelationshipIndex = js.toClass()

    override fun verifyJsObject(jsObject: RelationshipIndexJs) {
        assertEquals("POINT", jsObject.type)
        assertEquals(2, jsObject.properties.size)
        assertTrue(jsObject.properties.contains("property_1"))
        assertTrue(jsObject.properties.contains("property_2"))
        assertJsEquals("test", (jsObject.options as? FullTextIndexOptionJs)?.analyzer)
        assertJsEquals(stringValueJs("val1"), jsObject.custom["key1"])
    }

}
