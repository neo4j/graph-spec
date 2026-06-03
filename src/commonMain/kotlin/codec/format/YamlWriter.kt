package codec.format

import codec.schema.SchemaElement
import codec.schema.SchemaList
import codec.schema.SchemaLiteral
import codec.schema.SchemaMap
import codec.schema.SchemaNull

/**
 * YamlWriter pretty prints yaml according to [YamlPrintOptions]
 * It replaces [net.mamoe.yamlkt.Yaml]'s encodeToString as it doesn't have the flexibility
 * with inlining the lines that we want for a pretty graph spec format.
 */
class YamlWriter(private val options: YamlPrintOptions = YamlPrintOptions()) {

    fun write(element: SchemaElement): String {
        val builder = StringBuilder()
        writeElement(element, indent = 0, forceInline = false, builder = builder)
        return builder.toString().trimEnd() + "\n"
    }

    private fun writeElement(element: SchemaElement, indent: Int, forceInline: Boolean, builder: StringBuilder) {
        val inline = forceInline || options.shouldInline(element)
        when (element) {
            is SchemaNull -> builder.append("null")
            is SchemaLiteral -> if (element.isString) {
                if (options.alwaysQuoteStrings) {
                    builder.append(escapeAndQuoteString(element.string, true))
                } else {
                    builder.append(escapeAndQuoteString(element.string))
                }
            } else {
                builder.append(element.string)
            }
            is SchemaList -> {
                if (element.isEmpty()) {
                    builder.append("[]")
                } else if (inline) {
                    builder.append("[")
                    element.forEachIndexed { i, child ->
                        if (i > 0) builder.append(", ")
                        writeElement(child, indent, forceInline = true, builder)
                    }
                    builder.append("]")
                } else {
                    element.forEachIndexed { i, child ->
                        if (i > 0) {
                            builder.append("\n")
                            builder.append(" ".repeat(indent))
                        }
                        builder.append("- ")
                        when (child) {
                            is SchemaMap if !options.shouldInline(child) ->
                                writeBlockMapInsideList(child, indent + 2, builder)
                            is SchemaList if !options.shouldInline(child) -> {
                                builder.append("\n")
                                builder.append(" ".repeat(indent + 2))
                                writeElement(child, indent + 2, forceInline = false, builder)
                            }
                            else -> writeElement(child, indent + 2, forceInline = false, builder)
                        }
                    }
                }
            }
            is SchemaMap -> {
                if (element.isEmpty()) {
                    builder.append("{}")
                } else if (inline) {
                    builder.append("{ ")
                    var first = true
                    for ((key, value) in element.content) {
                        if (!first) builder.append(", ")
                        first = false
                        builder.append(escapeAndQuoteString(key)).append(": ")
                        writeElement(value, indent, forceInline = true, builder)
                    }
                    builder.append(" }")
                } else {
                    writeBlockMapInsideList(element, indent, builder)
                }
            }
        }
    }

    private fun writeBlockMapInsideList(map: SchemaMap, indent: Int, builder: StringBuilder) {
        var first = true
        for ((key, value) in map.content) {
            if (!first) {
                builder.append("\n")
                builder.append(" ".repeat(indent))
            }
            first = false
            builder.append(escapeAndQuoteString(key)).append(":")
            val valueInline = options.shouldInline(value)
            when (value) {
                is SchemaMap if !valueInline -> {
                    builder.append("\n")
                    builder.append(" ".repeat(indent + options.indent))
                    writeElement(value, indent + options.indent, forceInline = false, builder)
                }
                is SchemaList if !valueInline -> {
                    builder.append("\n")
                    builder.append(" ".repeat(indent + options.indent))
                    writeElement(value, indent + options.indent, forceInline = false, builder)
                }
                else -> {
                    builder.append(" ")
                    writeElement(value, indent, forceInline = false, builder)
                }
            }
        }
    }

    private fun needsQuotes(value: String) = value.isEmpty() ||
            value.trim() != value ||
            value.any { it in ":{}[],&*#?|-<>=!%@`\"'" } ||
            value == "true" || value == "false" || value == "null" ||
            value.contains("\n") || value.contains("\t") ||value.contains("\r") ||
            value.toDoubleOrNull() != null

    private fun escapeAndQuoteString(value: String, needsQuotes: Boolean = needsQuotes(value)): String {
        if (!needsQuotes) {
            return value
        }
        val escaped = value
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
        return "\"$escaped\""
    }
}
