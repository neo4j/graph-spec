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
package model

import js.objects.Record
import kotlinx.js.JsPlainObject
import model.extension.GraphModelExtensionsJs
import model.extension.graphModelExtensionsJs
import model.node.NodeJs
import model.relationship.RelationshipJs
@JsExport
@JsPlainObject
external interface GraphModelJs {
    val version: String
    val name: String
    val description: String
    val nodes: Record<String, NodeJs>
    val relationships: Record<String, RelationshipJs>
    val extensions: GraphModelExtensionsJs
}

fun graphModelJs(
    version: String,
    name: String = "",
    description: String = "",
    nodes: Record<String, NodeJs> = emptyRecord(),
    relationships: Record<String, RelationshipJs> = emptyRecord(),
    extensions: GraphModelExtensionsJs = graphModelExtensionsJs()
): GraphModelJs = jso {
    this.version = version
    this.name = name
    this.description = description
    this.nodes = nodes
    this.relationships = relationships
    this.extensions = extensions
}
