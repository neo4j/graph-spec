package model.tool

import model.JsMappingTest
import model.extension.StringValueJs
import kotlin.test.Test
import kotlin.test.assertEquals

class CanonicalQueryToolJsTest : JsMappingTest<CanonicalQueryTool, CanonicalQueryToolJs>() {

    override fun createClass() = CanonicalQueryTool(
        name = "findCoActors",
        description = "Actors who shared a film with a given actor",
        cypher = "MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(co:Actor) " +
            "WHERE a.name = \$name RETURN co",
    )

    override fun toJs(k: CanonicalQueryTool): CanonicalQueryToolJs = k.toJs()

    override fun toClass(js: CanonicalQueryToolJs): CanonicalQueryTool = js.toClass()

    override fun verifyJsObject(jsObject: CanonicalQueryToolJs) {
        assertEquals("canonicalQuery", jsObject.type)
        assertEquals("findCoActors", jsObject.name)
        assertEquals("Actors who shared a film with a given actor", jsObject.description)
        assertEquals(createClass().cypher, jsObject.cypher)
    }

    @Test
    fun testFactoryPinsTheTypeLiteral() {
        val tool = canonicalQueryToolJs(cypher = "MATCH (n) RETURN n")

        assertEquals("canonicalQuery", tool.type)
        assertEquals("MATCH (n) RETURN n", tool.cypher)
        assertEquals(null, tool.name)
        assertEquals(null, tool.description)
    }

    @Test
    fun testToToolJsInlinesCypherIntoExtra() {
        val tool = canonicalQueryToolJs(cypher = "MATCH (n) RETURN n", name = "all").toToolJs()

        assertEquals("canonicalQuery", tool.type)
        assertEquals("all", tool.name)
        assertEquals("MATCH (n) RETURN n", (tool.extra["cypher"] as StringValueJs).value)
    }

    @Test
    fun testToolJsDecodesToTheTypedShape() {
        val tool = toolJs(
            type = "canonicalQuery",
            name = "all",
            extra = js.objects.recordOf(
                "cypher" to model.extension.stringValueJs("MATCH (n) RETURN n"),
            ),
        ).toClass()

        val typed = tool.asTyped(CanonicalQueryToolModule)

        assertEquals("all", typed?.name)
        assertEquals("MATCH (n) RETURN n", typed?.cypher)
    }
}
