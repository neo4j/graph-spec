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
package model.extension.source

import js.objects.Record
import kotlinx.js.JsPlainObject
import model.associateBy
import model.emptyRecord
import model.jso
import model.property.Neo4jType
import model.value.ExtensionValueJs
import model.value.toClass
import model.value.toJs
import kotlin.String

@JsExport
@JsPlainObject
external interface TableColumnJs {
    var type: String
    var size: Int
    val suggested: String
    val supported: Array<String>
    val dimension: Int?
    val custom: Record<String, ExtensionValueJs>
    val name: String
}

fun tableColumnJs(
    type: String,
    size: Int = -1,
    suggested: String = "ANY",
    supported: Array<String> = emptyArray(),
    dimension: Int? = null,
    custom: Record<String, ExtensionValueJs> = emptyRecord(),
    name: String = ""
): TableColumnJs = jso {
    this.type = type
    this.size = size
    this.suggested = suggested
    this.supported = supported
    this.dimension = dimension
    this.custom = custom
    this.name = name
}

fun TableColumn.toJs(key: String) = tableColumnJs(
    type = type,
    size = size,
    suggested = Neo4jType.toString(suggested),
    supported = supported.map { Neo4jType.toString(it) }.toTypedArray(),
    dimension = dimension,
    custom = custom.associateBy { _, value -> value.toJs() },
    name = name ?: key
)

fun TableColumnJs.toClass() = TableColumn(
    type = type,
    size = size,
    suggested = neo4jType(suggested),
    supported = supported.map { neo4jType(it) }.toSet(),
    dimension = dimension,
    custom = custom.associateBy { _, value -> value.toClass() }.toMutableMap(),
    name = name
)

private fun TableColumnJs.neo4jType(type: String) = Neo4jType.fromString(type)
    ?: error("Invalid neo4j type '$type' for column '$name'")
