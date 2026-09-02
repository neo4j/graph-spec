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
package model.source

import model.addUnique
import model.dropAt
import model.getOrThrow
import model.remove

@JsExport
class TableEditor {
    companion object {
        @JsStatic
        fun setSource(table: TableJs, source: String) {
            table.source = source
        }

        @JsStatic
        fun setColumnType(table: TableJs, columnId: String, type: String) {
            val column = table.columns.getOrThrow(columnId, "Column")
            TableColumnEditor.setType(column, type)
        }

        @JsStatic
        fun setColumnSize(table: TableJs, columnId: String, size: Int) {
            val column = table.columns.getOrThrow(columnId, "Column")
            TableColumnEditor.setSize(column, size)
        }

        @JsStatic
        fun addColumn(table: TableJs, type: String): String = table.columns.addUnique("column") {
            tableColumnJs(type)
        }

        @JsStatic
        fun removeColumn(table: TableJs, columnId: String) {
            table.columns.remove(columnId)
        }

        @JsStatic
        fun addPrimaryKey(table: TableJs, key: String) {
            if (!table.primaryKeys.contains(key)) {
                table.primaryKeys += key
            }
        }

        @JsStatic
        fun removePrimaryKey(table: TableJs, key: String) {
            val index = table.primaryKeys.indexOf(key)
            if (index != -1) {
                table.primaryKeys = table.primaryKeys.dropAt(index)
            }
        }

        @JsStatic
        fun addForeignKey(table: TableJs, columns: Array<String>, reference: ForeignKeyReferenceJs): String =
            table.foreignKeys.addUnique("foreignKey") {
                foreignKeyJs(columns, reference)
            }

        @JsStatic
        fun removeForeignKey(table: TableJs, key: String) {
            table.foreignKeys.remove(key)
        }
    }
}
