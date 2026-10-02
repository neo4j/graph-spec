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
package migrate.migration.graphSpec

/**
 * The ADR-0008 §3 token table: every 4.0.0 `Neo4jType` token (38 tokens) mapped to its
 * v1 type token. Lossy rows widen (FLOAT32 -> FLOAT, INTEGER8/16/32 -> INTEGER,
 * UUID -> STRING); the `LOCAL `/`ZONED ` renames drop the space / map onto the v1
 * zoned forms. `LIST<...>` and `VECTOR<...>` wrappers are remapped through their
 * element type. Tokens outside the 4.0.0 enum are not the converter's to judge and
 * pass through unchanged.
 */
internal object Neo4jTypeTokens {

    private val scalars = mapOf(
        "ANY" to "ANY",
        "BOOLEAN" to "BOOLEAN",
        "DATE" to "DATE",
        "DURATION" to "DURATION",
        "FLOAT32" to "FLOAT", // lossy: 32-bit precision class dropped
        "FLOAT" to "FLOAT",
        "INTEGER8" to "INTEGER", // lossy: 8-bit width dropped
        "INTEGER16" to "INTEGER", // lossy: 16-bit width dropped
        "INTEGER32" to "INTEGER", // lossy: 32-bit width dropped
        "INTEGER" to "INTEGER",
        "LOCAL DATETIME" to "LOCALDATETIME",
        "LOCAL TIME" to "LOCALTIME",
        "POINT" to "POINT",
        "STRING" to "STRING",
        "ZONED DATETIME" to "DATETIME", // v1 DATETIME is the zoned form
        "ZONED TIME" to "TIME", // v1 TIME is the zoned form
        "UUID" to "STRING", // lossy: uuid-ness dropped
    )

    fun map(token: String): String = when {
        token.startsWith("LIST<") && token.endsWith(">") ->
            "LIST<${mapScalar(token.removePrefix("LIST<").removeSuffix(">"))}>"

        token.startsWith("VECTOR<") && token.endsWith(">") ->
            "VECTOR<${mapScalar(token.removePrefix("VECTOR<").removeSuffix(">"))}>"

        else -> mapScalar(token)
    }

    private fun mapScalar(token: String): String = scalars[token] ?: token
}
