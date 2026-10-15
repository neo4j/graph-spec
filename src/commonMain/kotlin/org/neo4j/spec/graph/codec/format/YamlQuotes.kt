/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.neo4j.spec.graph.codec.format

/** Private-use character prefixed to quoted scalars, so they can be told apart from unquoted ones after parsing. */
internal const val QUOTED_MARKER = ""

/**
 * Inserts [QUOTED_MARKER] at the start of every single or double-quoted scalar.
 *
 * The YAML library doesn't retain whether a scalar was quoted, which is needed to tell `"5"` from `5`
 * when values are untyped (i.e. custom extensions).
 * A quote only starts a scalar where a node can start: at the start of a line or after an indicator
 * (`: `, `- `, `? `, `[`, `{`, `,` in flow collections).
 * Quotes inside plain scalars (including their continuation lines), comments and block scalars (`|` and `>`)
 * are left alone.
 */
internal fun markQuotedScalars(yaml: String): String {
    val builder = StringBuilder(yaml.length)
    var atStart = true // The next significant character starts a node
    var inPlain = false // Inside a plain (unquoted) scalar
    var pendingPlain = false // A plain scalar ended at a line break, the next line may continue it
    var plainIndent = 0
    var flow = 0 // Depth of nested `[` and `{`
    var lineStart = 0
    var atLineStart = true
    var indent = 0 // Indent of the node the current scalar belongs to
    var scalarColumn = 0
    var i = 0
    while (i < yaml.length) {
        val char = yaml[i]
        if (char == '\n') {
            builder.append(char)
            i++
            lineStart = i
            atLineStart = true
            if (flow == 0) {
                if (inPlain) {
                    pendingPlain = true
                    plainIndent = indent
                    inPlain = false
                } else if (!pendingPlain) {
                    atStart = true
                }
            }
            continue
        }
        if (char == ' ' || char == '\t' || char == '\r') {
            builder.append(char)
            i++
            continue
        }
        if (atLineStart) {
            atLineStart = false
            val column = i - lineStart
            if (pendingPlain && flow == 0 && column > plainIndent && char != '#') {
                inPlain = true
            } else {
                atStart = flow == 0 || atStart
                if (flow == 0) indent = column
            }
            pendingPlain = false
        }
        when {
            char == '#' && (i == 0 || yaml[i - 1].isWhitespace()) -> {
                val end = yaml.indexOf('\n', i).let { if (it == -1) yaml.length else it }
                builder.append(yaml, i, end)
                i = end
                continue
            }
            inPlain && char == ':' && yaml.isBreak(i + 1, flow) -> {
                inPlain = false
                atStart = true
                indent = scalarColumn
            }
            inPlain && flow > 0 && (char == ',' || char == ']' || char == '}') -> {
                inPlain = false
                continue // Handled as a flow indicator
            }
            inPlain -> {}
            atStart && (char == '"' || char == '\'') -> {
                scalarColumn = i - lineStart
                builder.append(char).append(QUOTED_MARKER)
                i = copyQuoted(yaml, i + 1, char, builder)
                atStart = false
                continue
            }
            atStart && flow == 0 && (char == '|' || char == '>') && blockHeaderEnd(yaml, i) != -1 -> {
                i = copyBlockScalar(yaml, i, indent, builder)
                lineStart = i
                atLineStart = true
                continue
            }
            atStart && (char == '-' || char == '?') && yaml.isBreak(i + 1, 0) -> indent = i - lineStart
            atStart && (char == '[' || char == '{') -> flow++
            flow > 0 && (char == ']' || char == '}') -> {
                flow--
                atStart = false
            }
            flow > 0 && char == ',' -> atStart = true
            !inPlain && !atStart && char == ':' && yaml.isBreak(i + 1, flow) -> {
                atStart = true
                indent = scalarColumn
            }
            atStart && (char == '&' || char == '!') -> {
                // Anchors and tags are followed by the node they apply to
                while (i < yaml.length && !yaml[i].isWhitespace()) {
                    builder.append(yaml[i])
                    i++
                }
                continue
            }
            atStart && char == ':' && yaml.isBreak(i + 1, flow) -> {}
            atStart -> {
                inPlain = true
                atStart = false
                scalarColumn = i - lineStart
            }
        }
        builder.append(char)
        i++
    }
    return builder.toString()
}

/** Whether the character at [index] ends an indicator, in flow collections `,`, `]` and `}` do as well. */
private fun String.isBreak(index: Int, flow: Int): Boolean {
    if (index >= length) return true
    val char = this[index]
    return char == ' ' || char == '\t' || char == '\n' || char == '\r' ||
        (flow > 0 && (char == ',' || char == ']' || char == '}'))
}

/** Returns the index after the line break of a block scalar header (`|`, `>-`, `|2 # comment`) or -1 if it isn't. */
private fun blockHeaderEnd(yaml: String, start: Int): Int {
    var i = start + 1
    while (i < yaml.length && (yaml[i].isDigit() || yaml[i] == '+' || yaml[i] == '-')) i++
    while (i < yaml.length && (yaml[i] == ' ' || yaml[i] == '\t')) i++
    if (i < yaml.length && yaml[i] == '#' && yaml[i - 1].isWhitespace()) {
        i = yaml.indexOf('\n', i).let { if (it == -1) yaml.length else it }
    }
    return when {
        i >= yaml.length -> yaml.length
        yaml[i] == '\n' || yaml[i] == '\r' -> yaml.indexOf('\n', i).let { if (it == -1) yaml.length else it + 1 }
        else -> -1
    }
}

/** Copies the block scalar header at [start] and its content lines, more indented than [parentIndent], unchanged. */
private fun copyBlockScalar(yaml: String, start: Int, parentIndent: Int, builder: StringBuilder): Int {
    var i = blockHeaderEnd(yaml, start)
    builder.append(yaml, start, i)
    while (i < yaml.length) {
        val end = yaml.indexOf('\n', i).let { if (it == -1) yaml.length else it + 1 }
        val line = yaml.substring(i, end)
        if (line.isNotBlank() && line.takeWhile { it == ' ' }.length <= parentIndent) break
        builder.append(line)
        i = end
    }
    return i
}

/** Copies a quoted scalar's content and closing [quote], returning the index after it. */
private fun copyQuoted(yaml: String, start: Int, quote: Char, builder: StringBuilder): Int {
    var i = start
    while (i < yaml.length) {
        val char = yaml[i]
        builder.append(char)
        i++
        if (quote == '"' && char == '\\' && i < yaml.length) {
            builder.append(yaml[i])
            i++
        } else if (char == quote) {
            if (quote == '\'' && i < yaml.length && yaml[i] == '\'') {
                builder.append(yaml[i])
                i++
            } else {
                return i
            }
        }
    }
    return i
}
