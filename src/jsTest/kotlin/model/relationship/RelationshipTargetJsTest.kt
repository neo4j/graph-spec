package model.relationship

import model.JsMappingTest
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RelationshipTargetJsTest : JsMappingTest<RelationshipTarget, RelationshipTargetJs>() {

    override fun createClass() = RelationshipTarget(
        node = "nodeId",
        minCount = 0,
        maxCount = 5,
    )

    override fun toJs(k: RelationshipTarget): RelationshipTargetJs = k.toJs()

    override fun toClass(js: RelationshipTargetJs): RelationshipTarget = js.toClass()

    override fun verifyJsObject(jsObject: RelationshipTargetJs) {
        assertEquals("nodeId", jsObject.node)
        assertNull(jsObject.count)
        assertEquals(0, jsObject.minCount)
        assertEquals(5, jsObject.maxCount)
    }

}
