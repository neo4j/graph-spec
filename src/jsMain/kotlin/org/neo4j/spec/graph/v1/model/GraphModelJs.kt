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
package org.neo4j.spec.graph.v1.model

import js.objects.Record
import kotlinx.js.JsPlainObject
import org.neo4j.spec.graph.v1.model.extension.GraphModelExtensionsJs
import org.neo4j.spec.graph.v1.model.extension.graphModelExtensionsJs
import org.neo4j.spec.graph.v1.model.node.NodeJs
import org.neo4j.spec.graph.v1.model.relationship.RelationshipJs
@JsExport
@JsPlainObject
external interface GraphModelJs {
    val schema: String
    val version: String
    val id: String
    val name: String
    val description: String
    val nodes: Record<String, NodeJs>
    val relationships: Record<String, RelationshipJs>
    val extensions: GraphModelExtensionsJs
}

fun graphModelJs(
    schema: String,
    version: String,
    id: String,
    name: String = "",
    description: String = "",
    nodes: Record<String, NodeJs> = emptyRecord(),
    relationships: Record<String, RelationshipJs> = emptyRecord(),
    extensions: GraphModelExtensionsJs = graphModelExtensionsJs()
): GraphModelJs = jso {
    this.schema = schema
    this.version = version
    this.id = id
    this.name = name
    this.description = description
    this.nodes = nodes
    this.relationships = relationships
    this.extensions = extensions
}
