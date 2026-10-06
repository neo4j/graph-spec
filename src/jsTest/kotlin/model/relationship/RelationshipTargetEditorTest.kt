package model.relationship

import kotlin.test.*

class RelationshipTargetEditorTest {

    @Test
    fun testRelationshipTargetJsFactoryDefaults() {
        val target = relationshipTargetJs()

        assertEquals("", target.node)
        assertEquals("", target.label)
    }

    @Test
    fun testRelationshipTargetJsFactoryCustomValues() {
        val target = relationshipTargetJs(
            node = "NodeA",
            label = "LabelA",
        )

        assertEquals("NodeA", target.node)
        assertEquals("LabelA", target.label)
    }

    @Test
    fun testSetNodeClearsLabel() {
        // Start with a label set
        val target = relationshipTargetJs(label = "ExistingLabel")
        assertEquals("ExistingLabel", target.label)

        // Setting the node should update node AND clear label
        RelationshipTargetEditor.setNode(target, "NewNode")

        assertEquals("NewNode", target.node)
        assertEquals("", target.label, "Label should be cleared when node is set")
    }

    @Test
    fun testSetLabelClearsNode() {
        // Start with a node set
        val target = relationshipTargetJs(node = "ExistingNode")
        assertEquals("ExistingNode", target.node)

        // Setting the label should update label AND clear node
        RelationshipTargetEditor.setLabel(target, "NewLabel")

        assertEquals("NewLabel", target.label)
        assertEquals("", target.node, "Node should be cleared when label is set")
    }

    @Test
    fun testConsecutiveUpdates() {
        val target = relationshipTargetJs()

        // 1. Set Node
        RelationshipTargetEditor.setNode(target, "A")
        assertEquals("A", target.node)
        assertEquals("", target.label)

        // 2. Switch to Label
        RelationshipTargetEditor.setLabel(target, "B")
        assertEquals("", target.node)
        assertEquals("B", target.label)

        // 3. Switch back to Node
        RelationshipTargetEditor.setNode(target, "C")
        assertEquals("C", target.node)
        assertEquals("", target.label)
    }

    @Test
    fun testCountDefaults() {
        val target = relationshipTargetJs()
        assertEquals(-1, target.count)
        assertEquals(0, target.minCount)
        assertEquals(Int.MAX_VALUE, target.maxCount)
    }

    @Test
    fun testSetCountResetsRange() {
        val target = relationshipTargetJs(minCount = 2, maxCount = 5)

        RelationshipTargetEditor.setCount(target, 3)

        assertEquals(3, target.count)
        assertEquals(0, target.minCount)
        assertEquals(Int.MAX_VALUE, target.maxCount)
    }

    @Test
    fun testSetMinAndMaxClearCount() {
        val target = relationshipTargetJs(count = 3)

        RelationshipTargetEditor.setMinCount(target, 1)
        assertEquals(-1, target.count)
        assertEquals(1, target.minCount)

        RelationshipTargetEditor.setCount(target, 4)
        RelationshipTargetEditor.setMaxCount(target, 9)
        assertEquals(-1, target.count)
        assertEquals(9, target.maxCount)
    }
}
