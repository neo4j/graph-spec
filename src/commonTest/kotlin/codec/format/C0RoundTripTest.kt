package codec.format

import codec.schema.SchemaLiteral
import codec.schema.schemaMapOf
import kotlin.test.Test
import kotlin.test.assertEquals

class C0RoundTripTest {

    @Test
    fun `control character in a description round trips through yaml`() {
        // ARRANGE
        val description = "bad" + 0x01.toChar() + "value"
        val writer = YamlWriter()
        val schema = schemaMapOf("description" to SchemaLiteral(description, isString = true))

        // ACT
        val out = writer.write(schema)
        val back = (YamlFormat.default.decodeFromString(out) as Map<*, *>)
        val value = (back["description"] as? SchemaLiteral)?.string ?: back["description"]

        // ASSERT
        assertEquals(description, value)
    }

    @Test
    fun `control character description emits unquoted yaml scalar`() {
        // ARRANGE
        val description = "bad" + 0x01.toChar() + "value"
        val writer = YamlWriter()

        // ACT
        val out = writer.write(SchemaLiteral(description, isString = true))

        // ASSERT - YAML 1.1 forbids C0 controls in plain scalars, but the writer emits one unquoted
        assertEquals("bad" + 0x01.toChar() + "value\n", out)
    }
}
