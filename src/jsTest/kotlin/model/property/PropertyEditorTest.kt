package model.property

import model.extension.StringValueJs
import model.extension.stringValueJs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyEditorTest {

    private fun property() = propertyJs(name = "p", id = "p")

    @Test
    fun testOneOfDefaultsEmpty() {
        assertTrue(property().oneOf.isEmpty())
    }

    @Test
    fun testAddOneOf() {
        val property = property()

        PropertyEditor.addOneOf(property, stringValueJs("a"))
        PropertyEditor.addOneOf(property, stringValueJs("b"))

        assertEquals(listOf("a", "b"), property.oneOf.map { (it as StringValueJs).value })
    }

    @Test
    fun testRemoveOneOfByIndex() {
        val property = property()
        PropertyEditor.addOneOf(property, stringValueJs("a"))
        PropertyEditor.addOneOf(property, stringValueJs("b"))
        PropertyEditor.addOneOf(property, stringValueJs("c"))

        PropertyEditor.removeOneOf(property, 1)

        assertEquals(listOf("a", "c"), property.oneOf.map { (it as StringValueJs).value })
    }

    @Test
    fun testRemoveOneOfOutOfRangeIsIgnored() {
        val property = property()
        PropertyEditor.addOneOf(property, stringValueJs("a"))

        PropertyEditor.removeOneOf(property, 5)
        PropertyEditor.removeOneOf(property, -1)

        assertEquals(1, property.oneOf.size)
    }

    @Test
    fun testSetReferenceAndPattern() {
        val property = property()

        PropertyEditor.setReference(property, "ref")
        PropertyEditor.setPattern(property, "^x$")

        assertEquals("ref", property.reference)
        assertEquals("^x$", property.pattern)
    }
}
