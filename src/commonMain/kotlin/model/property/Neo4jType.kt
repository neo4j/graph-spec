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
@file:OptIn(ExperimentalSerializationApi::class)

package model.property

import kotlinx.serialization.EncodeDefault
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/** Discriminator values for the container [Neo4jType] variants. Scalars are discriminated by their own name. */
object Neo4jTypeKind {
    const val LIST = "LIST"
    const val VECTOR = "VECTOR"
}

/**
 * A Neo4j property type. Represented as a discriminated object keyed on a `type` field:
 * - [ScalarType] — `{ "type": "STRING" }`
 * - [ListType]   — `{ "type": "LIST", "items": { "type": "STRING" } }`
 * - [VectorType] — `{ "type": "VECTOR", "items": { "type": "FLOAT" }, "dimension": 4 }`
 */
@JsExport
@Serializable(with = Neo4jTypeSerializer::class)
@SerialName("Neo4jType")
sealed class Neo4jType

@JsExport
@Serializable
@SerialName("ScalarType")
data class ScalarType(val type: Neo4jScalar) : Neo4jType()

@JsExport
@Serializable
@SerialName("ListType")
data class ListType(
    val items: ScalarType,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) val type: String = Neo4jTypeKind.LIST
) : Neo4jType()

@JsExport
@Serializable
@SerialName("VectorType")
data class VectorType(
    val items: ScalarType,
    val dimension: Int? = null,
    @EncodeDefault(EncodeDefault.Mode.ALWAYS) val type: String = Neo4jTypeKind.VECTOR
) : Neo4jType()
