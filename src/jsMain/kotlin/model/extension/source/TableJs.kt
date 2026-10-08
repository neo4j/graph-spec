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
import model.value.ExtensionValueJs
import model.value.toClass
import model.value.toJs
import kotlin.String

@JsExport
@JsPlainObject
external interface TableJs {
    var source: String
    val columns: Record<String, TableColumnJs>
    var primaryKeys: Array<String>
    val foreignKeys: Record<String, ForeignKeyJs>
    val custom: Record<String, ExtensionValueJs>
}

fun tableJs(
    source: String,
    columns: Record<String, TableColumnJs> = emptyRecord(),
    primaryKeys: Array<String> = emptyArray(),
    foreignKeys: Record<String, ForeignKeyJs> = emptyRecord(),
    custom: Record<String, ExtensionValueJs> = emptyRecord()
): TableJs = jso {
    this.source = source
    this.columns = columns
    this.primaryKeys = primaryKeys
    this.foreignKeys = foreignKeys
    this.custom = custom
}

fun Table.toJs() = tableJs(
    source = source,
    columns = columns.associateBy { key, column -> column.toJs(key) },
    primaryKeys = primaryKeys.toTypedArray(),
    foreignKeys = foreignKeys.associateBy { _, key -> key.toJs() },
    custom = custom.associateBy { _, value -> value.toJs() }
)

fun TableJs.toClass() = Table(
    source = source,
    columns = columns.associateBy { _, column -> column.toClass() },
    primaryKeys = primaryKeys.toMutableSet(),
    foreignKeys = foreignKeys.associateBy { _, fk -> fk.toClass() },
    custom = custom.associateBy { _, value -> value.toClass() }.toMutableMap()
)
