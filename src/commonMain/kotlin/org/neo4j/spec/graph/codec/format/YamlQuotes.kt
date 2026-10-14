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
 * A quote only starts a scalar at the start of a line or after an indicator (`: `, `- `, `? `, `[`, `{`, `,`),
 * so apostrophes inside plain scalars and comments are left alone.
 */
internal fun markQuotedScalars(yaml: String): String {
    val builder = StringBuilder(yaml.length)
    var previous = '\n' // Last significant character outside of quotes
    var i = 0
    while (i < yaml.length) {
        val char = yaml[i]
        when {
            char == '#' && (i == 0 || yaml[i - 1].isWhitespace()) -> {
                val end = yaml.indexOf('\n', i).let { if (it == -1) yaml.length else it }
                builder.append(yaml, i, end)
                i = end
                continue
            }
            (char == '"' || char == '\'') && previous in SCALAR_START -> {
                builder.append(char).append(QUOTED_MARKER)
                i = copyQuoted(yaml, i + 1, char, builder)
                previous = char
                continue
            }
            char == '\n' -> previous = '\n'
            !char.isWhitespace() -> previous = char
        }
        builder.append(char)
        i++
    }
    return builder.toString()
}

private val SCALAR_START = setOf('\n', ':', '-', '?', '[', '{', ',')

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
