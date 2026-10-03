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
package model.extension.importer.table

import js.objects.Record
import kotlinx.js.JsPlainObject
import model.emptyRecord
import model.jso
import model.remove
import kotlin.js.JsExport

@JsExport
@JsPlainObject
external interface TableExtensionJs {
    var schema: String?
    var name: String?
    var source: String?
    var columns: Record<String, TableColumnJs>
    var primaryKeys: Array<String>
    var foreignKeys: Record<String, ForeignKeyJs>
}

@JsExport
@JsPlainObject
external interface TableColumnJs {
    var type: String
    var size: Int?
    var suggested: String?
    var supported: Array<String>
    var dimension: Int?
}

@JsExport
@JsPlainObject
external interface ForeignKeyJs {
    var columns: Array<String>
    var references: ForeignKeyReferenceJs
}

@JsExport
@JsPlainObject
external interface ForeignKeyReferenceJs {
    var table: String
    var columns: Array<String>
}

fun tableExtensionJs(
    schema: String? = null,
    name: String? = null,
    source: String? = null,
    columns: Record<String, TableColumnJs> = emptyRecord(),
    primaryKeys: Array<String> = emptyArray(),
    foreignKeys: Record<String, ForeignKeyJs> = emptyRecord()
): TableExtensionJs = jso {
    this.schema = schema
    this.name = name
    this.source = source
    this.columns = columns
    this.primaryKeys = primaryKeys
    this.foreignKeys = foreignKeys
}

fun tableColumnJs(
    type: String = "",
    size: Int? = null,
    suggested: String? = null,
    supported: Array<String> = emptyArray(),
    dimension: Int? = null
): TableColumnJs = jso {
    this.type = type
    this.size = size
    this.suggested = suggested
    this.supported = supported
    this.dimension = dimension
}

fun foreignKeyJs(columns: Array<String>, references: ForeignKeyReferenceJs): ForeignKeyJs = jso {
    this.columns = columns
    this.references = references
}

fun foreignKeyReferenceJs(table: String, columns: Array<String> = emptyArray()): ForeignKeyReferenceJs = jso {
    this.table = table
    this.columns = columns
}

fun TableColumn.toJs() = tableColumnJs(
    type = type,
    size = size,
    suggested = suggested,
    supported = supported.toTypedArray(),
    dimension = dimension
)

fun TableColumnJs.toClass(): TableColumn = TableColumn(
    type = type,
    size = size,
    suggested = suggested,
    supported = supported.toSet(),
    dimension = dimension
)

fun ForeignKey.toJs(): ForeignKeyJs = foreignKeyJs(columns.toTypedArray(), references.toJs())

fun ForeignKeyReference.toJs(): ForeignKeyReferenceJs = foreignKeyReferenceJs(table, columns.toTypedArray())

fun ForeignKeyJs.toClass(): ForeignKey = ForeignKey(columns.toSet(), references.toClass())

fun ForeignKeyReferenceJs.toClass(): ForeignKeyReference = ForeignKeyReference(table, columns.toSet())

@JsExport
class TableExtensionEditor {
    companion object {
        @JsStatic
        fun setName(table: TableExtensionJs, name: String?) {
            table.name = name
        }

        @JsStatic
        fun setSource(table: TableExtensionJs, source: String?) {
            table.source = source
        }

        @JsStatic
        fun setColumn(table: TableExtensionJs, name: String, column: TableColumnJs) {
            table.columns[name] = column
        }

        @JsStatic
        fun removeColumn(table: TableExtensionJs, name: String) {
            table.columns.remove(name)
        }

        @JsStatic
        fun addPrimaryKey(table: TableExtensionJs, column: String) {
            if (!table.primaryKeys.contains(column)) {
                table.primaryKeys = table.primaryKeys + column
            }
        }

        @JsStatic
        fun removePrimaryKey(table: TableExtensionJs, column: String) {
            table.primaryKeys = table.primaryKeys.filter { it != column }.toTypedArray()
        }

        @JsStatic
        fun setForeignKey(table: TableExtensionJs, name: String, foreignKey: ForeignKeyJs) {
            table.foreignKeys[name] = foreignKey
        }

        @JsStatic
        fun removeForeignKey(table: TableExtensionJs, name: String) {
            table.foreignKeys.remove(name)
        }
    }
}

@JsExport
class TableColumnEditor {
    companion object {
        @JsStatic
        fun setType(column: TableColumnJs, type: String) {
            column.type = type
        }

        @JsStatic
        fun setSuggested(column: TableColumnJs, suggested: String?) {
            column.suggested = suggested
        }

        @JsStatic
        fun addSupported(column: TableColumnJs, type: String) {
            if (!column.supported.contains(type)) {
                column.supported = column.supported + type
            }
        }

        @JsStatic
        fun removeSupported(column: TableColumnJs, type: String) {
            column.supported = column.supported.filter { it != type }.toTypedArray()
        }

        @JsStatic
        fun setDimension(column: TableColumnJs, dimension: Int?) {
            column.dimension = dimension
        }
    }
}
