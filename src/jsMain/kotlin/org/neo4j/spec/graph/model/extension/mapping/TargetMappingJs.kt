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
package org.neo4j.spec.graph.model.extension.mapping

import kotlinx.js.JsPlainObject
import org.neo4j.spec.graph.model.jso
@JsExport
@JsPlainObject
external interface TargetMappingJs {
    var nodeKeyColumn: String
    var nodeKeyProperty: String
    var relationshipColumn: String
}

fun targetMappingJs(
    nodeKeyColumn: String = "",
    nodeKeyProperty: String = "",
    relationshipColumn: String = ""
): TargetMappingJs = jso {
    this.nodeKeyColumn = nodeKeyColumn
    this.nodeKeyProperty = nodeKeyProperty
    this.relationshipColumn = relationshipColumn
}

fun TargetMapping.toJs() = targetMappingJs(
    nodeKeyColumn = nodeKeyColumn,
    nodeKeyProperty = nodeKeyProperty,
    relationshipColumn = relationshipColumn
)

fun TargetMappingJs.toClass() = TargetMapping(
    nodeKeyColumn = nodeKeyColumn,
    nodeKeyProperty = nodeKeyProperty,
    relationshipColumn = relationshipColumn
)
