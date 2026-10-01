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
package model.relationship

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

/**
 * v1 relationship endpoint (ontology-spec.schema.json `$defs.endpoint`): [node] is a node id
 * from the document's nodes map. Cardinality: [count] is the exact form, [minCount]/[maxCount]
 * the ranged form; all absent = unconstrained (0..*).
 */
@JsExport
@Serializable
@SerialName("RelationshipTarget")
data class RelationshipTarget(
    var node: String,
    var count: Int? = null,
    @SerialName("min_count")
    var minCount: Int? = null,
    @SerialName("max_count")
    var maxCount: Int? = null
)
