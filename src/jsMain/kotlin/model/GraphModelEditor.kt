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

import js.objects.toRecord
import model.extension.toClass
import model.extension.toJs
import model.node.nodeJs
import model.node.toClass
import model.node.toJs
import model.relationship.relationshipJs
import model.relationship.toClass
import model.relationship.toJs

/**
 * We have duplicate model built on external interfaces with conversion to and from classes in order
 * to support plain JavaScript objects which are used in React's Redux state storage.
 */
@JsExport
class GraphModelEditor {
    companion object {
        @JsStatic
        fun plain(model: GraphModel): GraphModelJs = graphModelJs(
            schema = model.schema,
            id = model.id,
            version = model.version,
            name = model.name,
            description = model.description,
            nodes = model.nodes.mapValues { (key, node) -> node.toJs(key) }.toRecord(),
            relationships = model.relationships.mapValues { (id, relationship) -> relationship.toJs(id) }
                .toRecord(),
            extensions = model.extensions.mapValues { (_, extension) -> extension.toJs() }.toRecord()
        )

        @JsStatic
        fun model(model: GraphModelJs): GraphModel = GraphModel(
            schema = model.schema,
            id = model.id,
            version = model.version,
            name = model.name,
            description = model.description,
            nodes = model.nodes.associateBy { id, js -> js.toClass(id) },
            relationships = model.relationships.associateBy { id, js -> js.toClass(id) },
            extensions = model.extensions.associateBy { _, js -> js.toClass() }.toMutableMap()
        )

        @JsStatic
        fun addNode(model: GraphModelJs, label: String? = null): String =
            model.nodes.addUnique("node") { nodeId ->
                nodeJs(id = nodeId, label = label)
            }

        @JsStatic
        fun removeNode(model: GraphModelJs, nodeId: String) {
            model.nodes.remove(nodeId)
        }

        @JsStatic
        fun addRelationship(model: GraphModelJs, type: String): String =
            model.relationships.addUnique("relationship") { relId ->
                relationshipJs(type = type, id = relId)
            }

        @JsStatic
        fun removeRelationship(model: GraphModelJs, relationshipId: String) {
            model.relationships.remove(relationshipId)
        }
    }
}
