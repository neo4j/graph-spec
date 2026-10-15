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
package org.neo4j.spec.graph.model.extension.source

import js.objects.Record
import kotlinx.js.JsPlainObject
import org.neo4j.spec.graph.model.associateBy
import org.neo4j.spec.graph.model.emptyRecord
import org.neo4j.spec.graph.model.extension.custom.toCustomClass
import org.neo4j.spec.graph.model.extension.custom.toCustomJs
import org.neo4j.spec.graph.model.jso
import kotlin.String

@JsExport
@JsPlainObject
external interface TableJs {
    var source: String
    var columns: Array<TableColumnJs>
    var primaryKeys: Array<String>
    val foreignKeys: Record<String, ForeignKeyJs>
    val custom: Record<String, Any>
}

fun tableJs(
    source: String,
    columns: Array<TableColumnJs> = emptyArray(),
    primaryKeys: Array<String> = emptyArray(),
    foreignKeys: Record<String, ForeignKeyJs> = emptyRecord(),
    custom: Record<String, Any> = emptyRecord()
): TableJs = jso {
    this.source = source
    this.columns = columns
    this.primaryKeys = primaryKeys
    this.foreignKeys = foreignKeys
    this.custom = custom
}

fun Table.toJs() = tableJs(
    source = source,
    columns = columns.map { it.toJs() }.toTypedArray(),
    primaryKeys = primaryKeys.toTypedArray(),
    foreignKeys = foreignKeys.associateBy { _, key -> key.toJs() },
    custom = custom.toCustomJs()
)

fun TableJs.toClass() = Table(
    source = source,
    columns = columns.map { it.toClass() }.toMutableList(),
    primaryKeys = primaryKeys.toMutableSet(),
    foreignKeys = foreignKeys.associateBy { _, fk -> fk.toClass() },
    custom = custom.toCustomClass()
)
