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

/** Discriminator (`type`) values for the [Neo4jType] variants. */
object Neo4jTypeKind {
    const val SCALAR = "ScalarType"
    const val LIST = "ListType"
    const val VECTOR = "VectorType"
}

/**
 * A Neo4j property type, serialized as a discriminated union keyed on `type`:
 * - [ScalarType] — `{ "type": "ScalarType", "scalar": "STRING" }`
 * - [ListType]   — `{ "type": "ListType", "items": "STRING" }`
 * - [VectorType] — `{ "type": "VectorType", "items": "FLOAT", "dimension": 4 }`
 *
 * A list/vector element is always a scalar ([Neo4jScalar]); a `dimension` exists only on a vector.
 */
@JsExport
@Serializable
@SerialName("Neo4jType")
sealed class Neo4jType

@JsExport
@Serializable
@SerialName(Neo4jTypeKind.SCALAR)
data class ScalarType(val scalar: Neo4jScalar) : Neo4jType()

@JsExport
@Serializable
@SerialName(Neo4jTypeKind.LIST)
data class ListType(val items: Neo4jScalar) : Neo4jType()

@JsExport
@Serializable
@SerialName(Neo4jTypeKind.VECTOR)
data class VectorType(val items: Neo4jScalar, val dimension: Int? = null) : Neo4jType()
