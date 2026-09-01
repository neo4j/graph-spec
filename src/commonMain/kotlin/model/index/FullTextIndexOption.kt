package model.index

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.type.IndexType
import kotlin.js.JsExport

/**
 * https://neo4j.com/docs/cypher-manual/current/indexes/semantic-indexes/full-text-indexes/#configuration-settings
 * https://neo4j.com/docs/operations-manual/2026.07/performance/index-configuration/#index-configuration-fulltext
 * https://neo4j.com/docs/operations-manual/2026.07/configuration/configuration-settings/#index-settings
 */
@JsExport
@Serializable
@SerialName("FullTextIndexOption")
data class FullTextIndexOption(
    @SerialName("fulltext.default_analyzer")
    val defaultAnalyzer: String = "standard-no-stop-words",
    @SerialName("fulltext.analyzer")
    val analyzer: String? = null,
    @SerialName("fulltext.eventually_consistent")
    val eventuallyConsistent: Boolean = false,
    @SerialName("fulltext.eventually_consistent_apply_parallelism")
    val eventuallyConsistentApplyParallelism: Int = 1,
    @SerialName("fulltext.eventually_consistent_refresh_interval")
    val eventuallyConsistentRefreshInterval: String = "0s",
    @SerialName("fulltext.eventually_consistent_refresh_parallelism")
    val eventuallyConsistentRefreshParallelism: Int = 1,
    @SerialName("fulltext.eventually_consistent_index_update_queue_max_length")
    val eventuallyConsistentUpdateQueueMaxLength: Int = 10_000,
) : IndexOption {
    override val type: IndexType = IndexType.FULLTEXT
}
