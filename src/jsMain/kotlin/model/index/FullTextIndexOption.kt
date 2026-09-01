package model.index

import kotlinx.js.JsPlainObject
import model.jso

@JsExport
@JsPlainObject
external interface FullTextIndexOptionJs : IndexOptionJs {
    val defaultAnalyzer: String
    val analyzer: String?
    val eventuallyConsistent: Boolean
    val eventuallyConsistentApplyParallelism: Int
    val eventuallyConsistentRefreshInterval: String
    val eventuallyConsistentRefreshParallelism: Int
    val eventuallyConsistentUpdateQueueMaxLength: Int
}

fun fullTextIndexOptionJs(
    defaultAnalyzer: String = "standard-no-stop-words",
    analyzer: String? = null,
    eventuallyConsistent: Boolean = false,
    eventuallyConsistentApplyParallelism: Int = 1,
    eventuallyConsistentRefreshInterval: String = "0s",
    eventuallyConsistentRefreshParallelism: Int = 1,
    eventuallyConsistentUpdateQueueMaxLength: Int = 10_000
): FullTextIndexOptionJs = jso {
    this.type = "FULLTEXT"
    this.defaultAnalyzer = defaultAnalyzer
    this.analyzer = analyzer
    this.eventuallyConsistent = eventuallyConsistent
    this.eventuallyConsistentApplyParallelism = eventuallyConsistentApplyParallelism
    this.eventuallyConsistentRefreshInterval = eventuallyConsistentRefreshInterval
    this.eventuallyConsistentRefreshParallelism = eventuallyConsistentRefreshParallelism
    this.eventuallyConsistentUpdateQueueMaxLength = eventuallyConsistentUpdateQueueMaxLength
}

fun FullTextIndexOption.toJs() = fullTextIndexOptionJs(
    defaultAnalyzer = defaultAnalyzer,
    analyzer = analyzer,
    eventuallyConsistent = eventuallyConsistent,
    eventuallyConsistentApplyParallelism = eventuallyConsistentApplyParallelism,
    eventuallyConsistentRefreshInterval = eventuallyConsistentRefreshInterval,
    eventuallyConsistentRefreshParallelism = eventuallyConsistentRefreshParallelism,
    eventuallyConsistentUpdateQueueMaxLength = eventuallyConsistentUpdateQueueMaxLength,
)

fun FullTextIndexOptionJs.toClass() = FullTextIndexOption(
    defaultAnalyzer = defaultAnalyzer,
    analyzer = analyzer,
    eventuallyConsistent = eventuallyConsistent,
    eventuallyConsistentApplyParallelism = eventuallyConsistentApplyParallelism,
    eventuallyConsistentRefreshInterval = eventuallyConsistentRefreshInterval,
    eventuallyConsistentRefreshParallelism = eventuallyConsistentRefreshParallelism,
    eventuallyConsistentUpdateQueueMaxLength = eventuallyConsistentUpdateQueueMaxLength,
)
