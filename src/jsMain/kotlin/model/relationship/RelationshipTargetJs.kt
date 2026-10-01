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
    var count: Int?
    var minCount: Int?
    var maxCount: Int?
}

fun relationshipTargetJs(
    node: String = "",
    count: Int? = null,
    minCount: Int? = null,
    maxCount: Int? = null
): RelationshipTargetJs = jso {
    this.node = node
    this.count = count
    this.minCount = minCount
    this.maxCount = maxCount
}

fun RelationshipTarget.toJs() = relationshipTargetJs(
    node = node,
    count = count,
    minCount = minCount,
    maxCount = maxCount
)

fun RelationshipTargetJs.toClass() = RelationshipTarget(
    node = node,
    count = count,
    minCount = minCount,
    maxCount = maxCount
)
