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
package model.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * The scalar (non-container) Neo4j types. These are the element types that can appear on
 * their own, as the [ListType.items] of a list, or as the [VectorType.items] of a vector.
 */
@JsExport
@Serializable
@SerialName("Neo4jScalar")
enum class Neo4jScalar {
    ANY,
    BOOLEAN,
    STRING,
    INTEGER,
    INTEGER8,
    INTEGER16,
    INTEGER32,
    FLOAT,
    FLOAT32,
    DATE,
    DURATION,
    POINT,
    UUID,

    @SerialName("LOCAL DATETIME")
    LOCAL_DATETIME,

    @SerialName("LOCAL TIME")
    LOCAL_TIME,

    @SerialName("ZONED DATETIME")
    ZONED_DATETIME,

    @SerialName("ZONED TIME")
    ZONED_TIME;

    companion object {
        // Kept as functions rather than enum properties to avoid breaking the JS union conversion.
        fun fromString(string: String): Neo4jScalar? = when (string) {
            "ANY" -> ANY
            "BOOLEAN" -> BOOLEAN
            "STRING" -> STRING
            "INTEGER" -> INTEGER
            "INTEGER8" -> INTEGER8
            "INTEGER16" -> INTEGER16
            "INTEGER32" -> INTEGER32
            "FLOAT" -> FLOAT
            "FLOAT32" -> FLOAT32
            "DATE" -> DATE
            "DURATION" -> DURATION
            "POINT" -> POINT
            "UUID" -> UUID
            "LOCAL DATETIME" -> LOCAL_DATETIME
            "LOCAL TIME" -> LOCAL_TIME
            "ZONED DATETIME" -> ZONED_DATETIME
            "ZONED TIME" -> ZONED_TIME
            else -> null
        }

        fun toString(scalar: Neo4jScalar): String = when (scalar) {
            ANY -> "ANY"
            BOOLEAN -> "BOOLEAN"
            STRING -> "STRING"
            INTEGER -> "INTEGER"
            INTEGER8 -> "INTEGER8"
            INTEGER16 -> "INTEGER16"
            INTEGER32 -> "INTEGER32"
            FLOAT -> "FLOAT"
            FLOAT32 -> "FLOAT32"
            DATE -> "DATE"
            DURATION -> "DURATION"
            POINT -> "POINT"
            UUID -> "UUID"
            LOCAL_DATETIME -> "LOCAL DATETIME"
            LOCAL_TIME -> "LOCAL TIME"
            ZONED_DATETIME -> "ZONED DATETIME"
            ZONED_TIME -> "ZONED TIME"
        }
    }
}
