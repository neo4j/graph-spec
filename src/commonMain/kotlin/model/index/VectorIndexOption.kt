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
@SerialName("VectorIndexOption")
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
    val hnswEfConstruction: Int = 100,
) : IndexOption {
    override val type: IndexType = IndexType.VECTOR
}
