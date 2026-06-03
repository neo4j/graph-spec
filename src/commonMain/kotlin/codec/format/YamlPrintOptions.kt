package codec.format

import codec.schema.SchemaElement

class YamlPrintOptions(
    val indent: Int = 2,
    val alwaysQuoteStrings: Boolean = false,
    val inlinePaths: Set<String> = emptySet(),
) {
    fun shouldInline(element: SchemaElement): Boolean {
        return inlinePaths.any { pattern -> matchPath(pattern, element.path) }
    }

    private fun matchPath(pattern: String, path: String): Boolean {
        if (pattern == path) {
            return true
        }
        val regexStr = buildString {
            append("^")
            var i = 0
            while (i < pattern.length) {
                if (pattern.startsWith("**", i)) {
                    // match any characters across multiple hierarchy levels
                    append(".*")
                    i += 2
                } else if (pattern.startsWith("*", i)) {
                    // matches single key/index level
                    append("[^.]+")
                    i += 1
                } else {
                    val char = pattern[i]
                    // Escape standard regex special characters
                    if ("\\^$.|?+()[]{}".contains(char)) {
                        append('\\').append(char)
                    } else {
                        append(char)
                    }
                    i += 1
                }
            }
            append("$")
        }
        return path.matches(Regex(regexStr))
    }
}
