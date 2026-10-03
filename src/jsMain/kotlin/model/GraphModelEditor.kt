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
import kotlinx.serialization.json.Json
import model.extension.ExtensionValueSerializer
import model.extension.importer.mapping.MappingExtensionJs
import model.extension.importer.mapping.MappingExtensionModule
import model.extension.importer.table.TableExtensionJs
import model.extension.importer.table.TableExtensionModule
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
            nodes = model.nodes.associateBy { _, js -> js.toClass() },
            relationships = model.relationships.associateBy { _, js -> js.toClass() },
            extensions = model.extensions.associateBy { _, js -> js.toClass() }.toMutableMap()
        )

        @JsStatic
        fun addNode(model: GraphModelJs, label: String? = null): String = model.nodes.addUnique("node") { nodeId ->
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

        /*
            Table/mapping are named extensions at the document root (ADR-0005); these
            helpers keep the 4.0.0 addTable/removeTable editor flow working against the
            extensions map.
         */

        @JsStatic
        fun setTableExtension(model: GraphModelJs, table: TableExtensionJs) {
            model.extensions[TableExtensionModule.key] =
                ExtensionValueSerializer.fromJson(Json.parseToJsonElement(JSON.stringify(table)).dropNulls()).toJs()
        }

        @JsStatic
        fun removeTableExtension(model: GraphModelJs) {
            model.extensions.remove(TableExtensionModule.key)
        }

        @JsStatic
        fun setMappingExtension(model: GraphModelJs, mapping: MappingExtensionJs) {
            model.extensions[MappingExtensionModule.key] =
                ExtensionValueSerializer.fromJson(Json.parseToJsonElement(JSON.stringify(mapping)).dropNulls()).toJs()
        }

        @JsStatic
        fun removeMappingExtension(model: GraphModelJs) {
            model.extensions.remove(MappingExtensionModule.key)
        }
    }
}

/*
    JSON.stringify on a *Js twin emits explicit nulls for unset optional fields; the
    ExtensionValue tree has no null kind (absent means none), so strip them before
    the payload enters the extensions map.
 */
private fun kotlinx.serialization.json.JsonElement.dropNulls(): kotlinx.serialization.json.JsonElement = when (this) {
    is kotlinx.serialization.json.JsonObject ->
        kotlinx.serialization.json.JsonObject(
            entries.filter { it.value !is kotlinx.serialization.json.JsonNull }
                .associate { it.key to it.value.dropNulls() }
        )
    is kotlinx.serialization.json.JsonArray -> kotlinx.serialization.json.JsonArray(map { it.dropNulls() })
    else -> this
}
