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
    eventuallyConsistentUpdateQueueMaxLength = eventuallyConsistentUpdateQueueMaxLength
)

fun FullTextIndexOptionJs.toClass() = FullTextIndexOption(
    defaultAnalyzer = defaultAnalyzer,
    analyzer = analyzer,
    eventuallyConsistent = eventuallyConsistent,
    eventuallyConsistentApplyParallelism = eventuallyConsistentApplyParallelism,
    eventuallyConsistentRefreshInterval = eventuallyConsistentRefreshInterval,
    eventuallyConsistentRefreshParallelism = eventuallyConsistentRefreshParallelism,
    eventuallyConsistentUpdateQueueMaxLength = eventuallyConsistentUpdateQueueMaxLength
)
