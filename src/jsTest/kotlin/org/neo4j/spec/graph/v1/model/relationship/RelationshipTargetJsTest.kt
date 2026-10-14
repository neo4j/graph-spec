package org.neo4j.spec.graph.v1.model.relationship

import org.neo4j.spec.graph.v1.model.relationship.extension.toClass
import org.neo4j.spec.graph.v1.model.relationship.extension.toJs
import org.neo4j.spec.graph.v1.model.extension.mapping.JsMappingTest
import kotlin.test.Test
import kotlin.test.assertEquals

class RelationshipTargetJsTest : JsMappingTest<RelationshipTarget, RelationshipTargetJs>() {

    override fun createClass() = RelationshipTarget(
        node = "nodeId",
        label = "label",
        minCount = 1,
        maxCount = 5
    )

    override fun toJs(k: RelationshipTarget): RelationshipTargetJs = k.toJs()

    override fun toClass(js: RelationshipTargetJs): RelationshipTarget = js.toClass()

    override fun verifyJsObject(jsObject: RelationshipTargetJs) {
        assertEquals("nodeId", jsObject.node)
        assertEquals("label", jsObject.label)
        assertEquals(null, jsObject.count)
        assertEquals(1, jsObject.minCount)
        assertEquals(5, jsObject.maxCount)
    }

    @Test
    fun testExactCountRoundTrip() {
        val js = RelationshipTarget(node = "n", count = 3).toJs()

        assertEquals(3, js.count)
        assertEquals(null, js.minCount)
        assertEquals(null, js.maxCount)
        assertEquals(RelationshipTarget(node = "n", count = 3), js.toClass())
    }

}
