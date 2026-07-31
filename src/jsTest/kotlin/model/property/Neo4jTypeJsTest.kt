package model.property

import model.mapping.JsMappingTest
import kotlin.test.Test
import kotlin.test.assertEquals

class Neo4jTypeJsTest : JsMappingTest<Neo4jType, Neo4jTypeJs>() {

    override fun createClass(): Neo4jType = VectorFloatType(1536)

    override fun toJs(k: Neo4jType): Neo4jTypeJs = k.toJs()

    override fun toClass(js: Neo4jTypeJs): Neo4jType = js.toClass()

    override fun verifyJsObject(jsObject: Neo4jTypeJs) {
        assertEquals("VECTOR<FLOAT>", jsObject.type)
        assertEquals(1536, jsObject.dimension)
    }

    @Test
    fun `test dimensionless types omit dimension`() {
        assertEquals("""{"type":"STRING"}""", JSON.stringify(StringType.toJs()))
        assertEquals("""{"type":"VECTOR<FLOAT>"}""", JSON.stringify(VectorFloatType().toJs()))
    }
}
