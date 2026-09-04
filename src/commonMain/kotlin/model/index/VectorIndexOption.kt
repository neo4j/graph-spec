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
package model.index

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.type.IndexType
import kotlin.js.JsExport

/**
 * https://neo4j.com/docs/cypher-manual/current/indexes/semantic-indexes/vector-indexes/#configuration-settings
 */
@JsExport
@Serializable
@SerialName("VECTOR")
data class VectorIndexOption(
    @SerialName("vector.dimensions")
    val dimensions: Int? = null,
    @SerialName("vector.similarity_function")
    val similarityFunction: String = "cosine",
    @SerialName("vector.default_search_expansion_factor")
    val defaultSearchExpansionFactor: Float? = null,
    @SerialName("vector.quantization.enabled")
    val quantizationEnabled: Boolean = true,
    @SerialName("vector.quantization.type")
    val quantizationType: String = "scalar",
    @SerialName("vector.hnsw.m")
    val hnswM: Int = 16,
    @SerialName("vector.hnsw.ef_construction")
    val hnswEfConstruction: Int = 100
) : IndexOption {
    override val type: IndexType get() = IndexType.VECTOR
}
