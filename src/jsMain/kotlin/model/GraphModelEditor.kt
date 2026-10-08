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
import model.extension.mapping.toClass
import model.extension.mapping.toJs
import model.extension.source.tableJs
import model.extension.source.toClass
import model.extension.source.toJs
import model.extension.toClass
import model.extension.toJs
import model.node.NodeEditor
import model.node.extension.toClass
import model.node.extension.toJs
import model.node.nodeJs
import model.node.toClass
import model.node.toJs
import model.relationship.extension.toClass
import model.relationship.extension.toJs
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
        fun plain(model: GraphModel): GraphModelJs {
            if (model.pretty) {
                error("Pretty models can't be converted to plain models, call model.internalise() first.")
            }
            return graphModelJs(
                schema = model.schema,
                version = model.version,
                name = model.name,
                description = model.description,
                nodes = model.nodes.mapValues { (key, node) -> node.toJs(key) }.toRecord(),
                relationships = model.relationships.mapValues { (id, relationship) -> relationship.toJs(id) }
                    .toRecord(),
                extensions = model.extensions.toJs()
            )
        }

        @JsStatic
        fun model(model: GraphModelJs): GraphModel = GraphModel(
            schema = model.schema,
            version = model.version,
            name = model.name,
            description = model.description,
            nodes = model.nodes.associateBy { id, js -> js.toClass(id) },
            relationships = model.relationships.associateBy { id, js -> js.toClass(id) },
            extensions = model.extensions.toClass()
        )

        @JsStatic
        fun addNode(model: GraphModelJs, name: String? = null, label: String? = null): String =
            model.nodes.addUnique("node") { nodeId ->
                val node = nodeJs(id = nodeId, name = name ?: nodeId)
                if (label != null) {
                    NodeEditor.setIdentifyingLabel(model, nodeId, label)
                }
                node
            }

        @JsStatic
        fun removeNode(model: GraphModelJs, nodeId: String) {
            model.nodes.remove(nodeId)
        }

        @JsStatic
        fun addRelationship(model: GraphModelJs, type: String, name: String?): String =
            model.relationships.addUnique("relationship") { relId ->
                relationshipJs(type = type, id = relId, name = name ?: relId)
            }

        @JsStatic
        fun removeRelationship(model: GraphModelJs, relationshipId: String) {
            model.relationships.remove(relationshipId)
        }

        @JsStatic
        fun addTable(model: GraphModelJs, source: String): String = model.extensions.tables.addUnique("table") {
            tableJs(source)
        }

        @JsStatic
        fun removeTable(model: GraphModelJs, tableId: String) {
            model.extensions.tables.remove(tableId)
        }
    }
}
