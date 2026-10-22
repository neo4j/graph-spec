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
package org.neo4j.spec.graph.model.index

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.neo4j.spec.graph.model.type.IndexType
import org.neo4j.spec.graph.schema.SerialDescription
import kotlin.js.JsExport

/**
 * https://neo4j.com/docs/cypher-manual/current/indexes/semantic-indexes/vector-indexes/#configuration-settings
 */
@JsExport
@Serializable
@SerialName("VECTOR")
@SerialDescription("Configuration of a vector index.")
data class VectorIndexOption(
    @SerialName("vector.dimensions")
    @SerialDescription("Number of dimensions of the indexed vectors.")
    val dimensions: Int? = null,
    @SerialName("vector.similarity_function")
    @SerialDescription("Function used to compare vectors, e.g. cosine or euclidean.")
    val similarityFunction: String = "cosine",
    @SerialName("vector.default_search_expansion_factor")
    @SerialDescription("Default expansion factor applied to searches of the index.")
    val defaultSearchExpansionFactor: Float? = null,
    @SerialName("vector.quantization.enabled")
    @SerialDescription("Whether vector quantization is enabled.")
    val quantizationEnabled: Boolean = true,
    @SerialName("vector.quantization.type")
    @SerialDescription("Type of vector quantization used.")
    val quantizationType: String = "scalar",
    @SerialName("vector.hnsw.m")
    @SerialDescription("Maximum number of connections per node in the HNSW graph.")
    val hnswM: Int = 16,
    @SerialName("vector.hnsw.ef_construction")
    @SerialDescription("Number of nearest neighbours considered when building the HNSW graph.")
    val hnswEfConstruction: Int = 100
) : IndexOption {
    override val type: IndexType get() = IndexType.VECTOR
}
