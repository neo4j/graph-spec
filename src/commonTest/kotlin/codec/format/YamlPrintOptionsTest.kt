package codec.format

import codec.schema.SchemaElement
import codec.schema.SchemaLiteral
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlin.test.assertFalse

class YamlPrintOptionsTest {

    @Test
    fun `test exact path match`() {
        val options = YamlPrintOptions(
            inlinePaths = setOf("metadata.tags", "settings.connections")
        )

        // Exact matches
        assertTrue(options.shouldInline(elementWithPath("metadata.tags")))
        assertTrue(options.shouldInline(elementWithPath("settings.connections")))

        // Partial or incorrect matches
        assertFalse(options.shouldInline(elementWithPath("metadata")))
        assertFalse(options.shouldInline(elementWithPath("metadata.tags.subTag")))
        assertFalse(options.shouldInline(elementWithPath("settings.other")))
    }

    @Test
    fun `test single wildcard match`() {
        val options = YamlPrintOptions(
            inlinePaths = setOf("nodes.*.properties")
        )

        // Single wildcards matching dynamic keys
        assertTrue(options.shouldInline(elementWithPath("nodes.nodeA.properties")))
        assertTrue(options.shouldInline(elementWithPath("nodes.node_B.properties")))
        assertTrue(options.shouldInline(elementWithPath("nodes.123.properties")))

        // Invalid matches (more/fewer segments, or different hierarchy)
        assertFalse(options.shouldInline(elementWithPath("nodes.properties")))
        assertFalse(options.shouldInline(elementWithPath("nodes.nodeA.nested.properties")))
        assertFalse(options.shouldInline(elementWithPath("other.nodeA.properties")))
    }

    @Test
    fun `test recursive wildcard matching`() {
        val options = YamlPrintOptions(
            inlinePaths = setOf("configs.**")
        )

        // Recursive wildcards should match anything nested under the prefix
        assertTrue(options.shouldInline(elementWithPath("configs.active")))
        assertTrue(options.shouldInline(elementWithPath("configs.users.roles")))
        assertTrue(options.shouldInline(elementWithPath("configs.users.roles.permissions.read")))

        // Should not match sibling or parent paths
        assertFalse(options.shouldInline(elementWithPath("configs")))
        assertFalse(options.shouldInline(elementWithPath("other.configs.active")))
    }

    @Test
    fun `test explicit list index match`() {
        val options = YamlPrintOptions(
            inlinePaths = setOf("mappings[*].properties.*")
        )

        // Matches valid indexed list paths
        assertTrue(options.shouldInline(elementWithPath("mappings[0].properties.field")))
        assertTrue(options.shouldInline(elementWithPath("mappings[125].properties.anotherField")))

        // Fails when brackets are missing or mismatched
        assertFalse(options.shouldInline(elementWithPath("mappings.0.properties.field")))
        assertFalse(options.shouldInline(elementWithPath("mappings[0].other.field")))
    }

    @Test
    fun `test empty options do not match`() {
        val options = YamlPrintOptions()

        assertFalse(options.shouldInline(elementWithPath("metadata.tags")))
        assertFalse(options.shouldInline(elementWithPath("any.path")))
    }

    private fun elementWithPath(path: String): SchemaElement {
        return SchemaLiteral(string = "", path = path, isString = false)
    }
}
