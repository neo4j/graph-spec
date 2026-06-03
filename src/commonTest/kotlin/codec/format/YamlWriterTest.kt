package codec.format

import codec.schema.SchemaList
import codec.schema.SchemaLiteral
import codec.schema.SchemaMap
import codec.schema.SchemaNull
import kotlin.test.Test
import kotlin.test.assertEquals

class YamlWriterTest {

    @Test
    fun `test write primitives`() {
        val writer = YamlWriter()

        // Null value
        assertEquals("null\n", writer.write(SchemaNull()))

        // Non-string literals (booleans, numbers)
        assertEquals("true\n", writer.write(SchemaLiteral("true", isString = false)))
        assertEquals("123.45\n", writer.write(SchemaLiteral("123.45", isString = false)))

        // Simple string literal
        assertEquals("hello\n", writer.write(SchemaLiteral("hello", isString = true)))
    }

    @Test
    fun `test escaping and quoting`() {
        val writer = YamlWriter()

        // String containing colon should be quoted
        assertEquals("\"name: value\"\n", writer.write(SchemaLiteral("name: value", isString = true)))

        // String starting/ending with space should be quoted
        assertEquals("\" padded \"\n", writer.write(SchemaLiteral(" padded ", isString = true)))

        // String with special characters should be quoted and escaped
        assertEquals("\"line1\\nline2\"\n", writer.write(SchemaLiteral("line1\nline2", isString = true)))
        assertEquals("\"with \\\"quotes\\\"\"\n", writer.write(SchemaLiteral("with \"quotes\"", isString = true)))
    }

    @Test
    fun `test always quote`() {
        val options = YamlPrintOptions(alwaysQuoteStrings = true)
        val writer = YamlWriter(options)

        assertEquals("\"hello\"\n", writer.write(SchemaLiteral("hello", isString = true)))
    }

    @Test
    fun `test write standard map`() {
        val writer = YamlWriter()
        val map = SchemaMap(
            mutableMapOf(
                "name" to SchemaLiteral("John Doe", isString = true),
                "age" to SchemaLiteral("30", isString = false),
                "active" to SchemaLiteral("true", isString = false)
            )
        )

        val expected = """
            name: John Doe
            age: 30
            active: true
        """.trimIndent() + "\n"

        assertEquals(expected, writer.write(map))
    }

    @Test
    fun `test write standard list`() {
        val writer = YamlWriter()
        val list = SchemaList(
            mutableListOf(
                SchemaLiteral("apple", isString = true),
                SchemaLiteral("banana", isString = true),
                SchemaLiteral("cherry", isString = true)
            )
        )

        val expected = """
            - apple
            - banana
            - cherry
        """.trimIndent() + "\n"

        assertEquals(expected, writer.write(list))
    }

    @Test
    fun `test write empty collections`() {
        val writer = YamlWriter()

        assertEquals("[]\n", writer.write(SchemaList(mutableListOf())))
        assertEquals("{}\n", writer.write(SchemaMap(mutableMapOf())))
    }

    @Test
    fun `test nested block structures`() {
        val writer = YamlWriter(YamlPrintOptions(indent = 2))
        val root = SchemaMap(
            mutableMapOf(
                "title" to SchemaLiteral("Project", isString = true),
                "metadata" to SchemaMap(
                    mutableMapOf(
                        "tags" to SchemaList(
                            mutableListOf(
                                SchemaLiteral("kotlin", isString = true),
                                SchemaLiteral("yaml", isString = true)
                            )
                        )
                    )
                )
            )
        )

        val expected = """
            title: Project
            metadata:
              tags:
                - kotlin
                - yaml
        """.trimIndent() + "\n"

        assertEquals(expected, writer.write(root))
    }

    @Test
    fun `test explicit inlining`() {
        val options = YamlPrintOptions(
            inlinePaths = setOf("metadata.tags")
        )
        val writer = YamlWriter(options)

        val root = SchemaMap(
            mutableMapOf(
                "title" to SchemaLiteral("Project", isString = true),
                "metadata" to SchemaMap(
                    mutableMapOf(
                        "tags" to SchemaList(
                            mutableListOf(
                                SchemaLiteral("kotlin", isString = true),
                                SchemaLiteral("yaml", isString = true)
                            ),
                            path = "metadata.tags"
                        )
                    ),
                    path = "metadata"
                )
            )
        )

        val expected = """
            title: Project
            metadata:
              tags: [kotlin, yaml]
        """.trimIndent() + "\n"

        assertEquals(expected, writer.write(root))
    }

    @Test
    fun `test wildcard inlining`() {
        val options = YamlPrintOptions(
            inlinePaths = setOf("mappings[*].properties.*")
        )
        val writer = YamlWriter(options)

        val p00 = SchemaMap(
            mutableMapOf(
                "field" to SchemaLiteral("categoryid", isString = true)
            ),
            path = "mappings[0].properties.p:0_0"
        )

        val properties = SchemaMap(
            mutableMapOf(
                "p:0_0" to p00
            ),
            path = "mappings[0].properties"
        )

        val mappingItem = SchemaMap(
            mutableMapOf(
                "node" to SchemaLiteral("n:0", isString = true),
                "properties" to properties
            ),
            path = "mappings[0]"
        )

        val root = SchemaMap(
            mutableMapOf(
                "mappings" to SchemaList(mutableListOf(mappingItem), path = "mappings")
            )
        )

        val expected = """
            mappings:
              - node: "n:0"
                properties:
                  "p:0_0": { field: categoryid }
        """.trimIndent() + "\n"

        assertEquals(expected, writer.write(root))
    }
}
