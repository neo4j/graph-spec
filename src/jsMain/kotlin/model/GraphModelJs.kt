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
import model.extension.ExtensionValueJs
import model.node.NodeJs
import model.relationship.RelationshipJs

@JsExport
@JsPlainObject
external interface GraphModelJs {
    val schema: String
    val id: String
    val version: Int
    var name: String?
    var description: String?
    val nodes: Record<String, NodeJs>
    val relationships: Record<String, RelationshipJs>
    val extensions: Record<String, ExtensionValueJs>
}

fun graphModelJs(
    schema: String = "",
    id: String = "",
    version: Int = 1,
    name: String? = null,
    description: String? = null,
    nodes: Record<String, NodeJs> = emptyRecord(),
    relationships: Record<String, RelationshipJs> = emptyRecord(),
    extensions: Record<String, ExtensionValueJs> = emptyRecord(),
): GraphModelJs = jso {
    this.schema = schema
    this.id = id
    this.version = version
    this.name = name
    this.description = description
    this.nodes = nodes
    this.relationships = relationships
    this.extensions = extensions
}
