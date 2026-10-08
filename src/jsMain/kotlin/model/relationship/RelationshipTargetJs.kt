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

import kotlinx.js.JsPlainObject
import model.jso
@JsExport
@JsPlainObject
external interface RelationshipTargetJs {
    var node: String
    var label: String
    var count: Int
    var minCount: Int
    var maxCount: Int
}

fun relationshipTargetJs(
    node: String = "",
    label: String = "",
    count: Int = -1,
    minCount: Int = 0,
    maxCount: Int = Int.MAX_VALUE
): RelationshipTargetJs = jso {
    this.node = node
    this.label = label
    this.count = count
    this.minCount = minCount
    this.maxCount = maxCount
}

fun RelationshipTarget.toJs() = relationshipTargetJs(
    node = node,
    label = label,
    count = count,
    minCount = minCount,
    maxCount = maxCount
)

fun RelationshipTargetJs.toClass() = RelationshipTarget(
    node = node,
    label = label,
    count = count,
    minCount = minCount,
    maxCount = maxCount
)
