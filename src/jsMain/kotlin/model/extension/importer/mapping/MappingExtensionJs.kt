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
package model.extension.importer.mapping

import js.objects.Record
import js.objects.toRecord
import kotlinx.js.JsPlainObject
import model.emptyRecord
import model.jso
import model.remove
import kotlin.js.JsExport

@JsExport
@JsPlainObject
external interface MappingExtensionJs {
    var schema: String?
    var kind: String
    var node: String?
    var relationship: String?
    var table: String?
    var query: String?
    var key: Array<String>
    var properties: Record<String, PropertyMappingJs>
    var mode: String?
    var matchLabel: String?
    var startNode: TargetMappingJs?
    var endNode: TargetMappingJs?
}

@JsExport
@JsPlainObject
external interface PropertyMappingJs {
    var column: String
}

@JsExport
@JsPlainObject
external interface TargetMappingJs {
    var node: String
    var properties: Record<String, PropertyMappingJs>
}

fun mappingExtensionJs(
    kind: String,
    node: String? = null,
    relationship: String? = null,
    table: String? = null,
    query: String? = null,
    key: Array<String> = emptyArray(),
    properties: Record<String, PropertyMappingJs> = emptyRecord(),
    mode: String? = null,
    matchLabel: String? = null,
    startNode: TargetMappingJs? = null,
    endNode: TargetMappingJs? = null
): MappingExtensionJs = jso {
    this.kind = kind
    this.node = node
    this.relationship = relationship
    this.table = table
    this.query = query
    this.key = key
    this.properties = properties
    this.mode = mode
    this.matchLabel = matchLabel
    this.startNode = startNode
    this.endNode = endNode
}

fun propertyMappingJs(column: String): PropertyMappingJs = jso {
    this.column = column
}

fun targetMappingJs(node: String, properties: Record<String, PropertyMappingJs> = emptyRecord()): TargetMappingJs =
    jso {
        this.node = node
        this.properties = properties
    }

fun PropertyMapping.toJs(): PropertyMappingJs = propertyMappingJs(column)

fun PropertyMappingJs.toClass(): PropertyMapping = PropertyMapping(column)

fun TargetMapping.toJs(): TargetMappingJs = targetMappingJs(node, properties.mapValues { it.value.toJs() }.toRecord())

@JsExport
class MappingExtensionEditor {
    companion object {
        @JsStatic
        fun setTable(mapping: MappingExtensionJs, table: String?) {
            mapping.table = table
        }

        @JsStatic
        fun setMode(mapping: MappingExtensionJs, mode: String?) {
            mapping.mode = mode
        }

        @JsStatic
        fun addKey(mapping: MappingExtensionJs, property: String) {
            if (!mapping.key.contains(property)) {
                mapping.key = mapping.key + property
            }
        }

        @JsStatic
        fun removeKey(mapping: MappingExtensionJs, property: String) {
            mapping.key = mapping.key.filter { it != property }.toTypedArray()
        }

        @JsStatic
        fun setPropertyMapping(mapping: MappingExtensionJs, property: String, column: String) {
            mapping.properties[property] = propertyMappingJs(column)
        }

        @JsStatic
        fun removePropertyMapping(mapping: MappingExtensionJs, property: String) {
            mapping.properties.remove(property)
        }
    }
}
