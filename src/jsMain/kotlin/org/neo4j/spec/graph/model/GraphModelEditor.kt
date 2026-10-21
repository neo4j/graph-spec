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
package org.neo4j.spec.graph.model

import js.objects.toRecord
import org.neo4j.spec.graph.model.extension.source.tableJs
import org.neo4j.spec.graph.model.extension.toClass
import org.neo4j.spec.graph.model.extension.toJs
import org.neo4j.spec.graph.model.node.NodeEditor
import org.neo4j.spec.graph.model.node.nodeJs
import org.neo4j.spec.graph.model.node.toClass
import org.neo4j.spec.graph.model.node.toJs
import org.neo4j.spec.graph.model.relationship.relationshipJs
import org.neo4j.spec.graph.model.relationship.toClass
import org.neo4j.spec.graph.model.relationship.toJs

/**
 * We have duplicate model built on external interfaces with conversion to and from classes in order
 * to support plain JavaScript objects which are used in React's Redux state storage.
 *
 * **Experimental:** these JavaScript helpers (the `*Editor` classes and the plain `*Js` objects) are experimental
 * and may change or be removed in any release without notice.
 */
@JsExport
class GraphModelEditor {
    companion object {
        @JsStatic
        fun plain(model: GraphModel): GraphModelJs = graphModelJs(
            schema = model.schema,
            version = model.version,
            id = model.id,
            name = model.name,
            description = model.description,
            nodes = model.nodes.mapValues { (key, node) -> node.toJs(key) }.toRecord(),
            relationships = model.relationships.mapValues { (id, relationship) -> relationship.toJs(id) }
                .toRecord(),
            extensions = model.extensions.toJs()
        )

        @JsStatic
        fun model(model: GraphModelJs): GraphModel = GraphModel(
            schema = model.schema,
            version = model.version,
            id = model.id,
            name = model.name,
            description = model.description,
            nodes = model.nodes.associateBy { id, js -> js.toClass(id) },
            relationships = model.relationships.associateBy { id, js -> js.toClass(id) },
            extensions = model.extensions.toClass()
        )

        @JsStatic
        fun addNode(model: GraphModelJs, label: String? = null): String = model.nodes.addUnique("node") { nodeId ->
            val node = nodeJs(id = nodeId)
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
        fun addRelationship(model: GraphModelJs, type: String): String =
            model.relationships.addUnique("relationship") { relId ->
                relationshipJs(type = type, id = relId)
            }

        @JsStatic
        fun removeRelationship(model: GraphModelJs, relationshipId: String) {
            model.relationships.remove(relationshipId)
        }

        @JsStatic
        fun addTable(model: GraphModelJs, source: String): String =
            model.extensions.dataSourceTables.addUnique("table") {
                tableJs(source)
            }

        @JsStatic
        fun removeTable(model: GraphModelJs, tableId: String) {
            model.extensions.dataSourceTables.remove(tableId)
        }
    }
}
