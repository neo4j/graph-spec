package model.relationship

import kotlin.test.*

class RelationshipTargetEditorTest {

    @Test
    fun testRelationshipTargetJsFactoryDefaults() {
        val target = relationshipTargetJs()

        assertEquals("", target.node)
        assertNull(target.count)
        assertNull(target.minCount)
        assertNull(target.maxCount)
    }

    @Test
    fun testRelationshipTargetJsFactoryCustomValues() {
        val target = relationshipTargetJs(
            node = "NodeA",
            count = 2,
        )

        assertEquals("NodeA", target.node)
        assertEquals(2, target.count)
    }

    @Test
    fun testSetNode() {
        val target = relationshipTargetJs()

        RelationshipTargetEditor.setNode(target, "NewNode")

        assertEquals("NewNode", target.node)
    }

    @Test
    fun testCardinalityUpdates() {
        val target = relationshipTargetJs(node = "A")

        RelationshipTargetEditor.setCount(target, 3)
        assertEquals(3, target.count)

        RelationshipTargetEditor.setMinCount(target, 1)
        RelationshipTargetEditor.setMaxCount(target, 10)
        assertEquals(1, target.minCount)
        assertEquals(10, target.maxCount)

        RelationshipTargetEditor.setCount(target, null)
        assertNull(target.count)
    }
}
