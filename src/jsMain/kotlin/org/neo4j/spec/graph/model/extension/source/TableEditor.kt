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

import org.neo4j.spec.graph.model.addUnique
import org.neo4j.spec.graph.model.dropAt
import org.neo4j.spec.graph.model.remove
@JsExport
class TableEditor {
    companion object {
        @JsStatic
        fun setSource(table: TableJs, source: String) {
            table.source = source
        }

        @JsStatic
        fun setColumnType(table: TableJs, columnName: String, type: String) {
            TableColumnEditor.setType(table.column(columnName), type)
        }

        @JsStatic
        fun setColumnSize(table: TableJs, columnName: String, size: Int) {
            TableColumnEditor.setSize(table.column(columnName), size)
        }

        @JsStatic
        fun addColumn(table: TableJs, type: String): String {
            val names = table.columns.map { it.name }.toSet()
            val name = generateSequence(0) { it + 1 }.map { "column$it" }.first { it !in names }
            table.columns += tableColumnJs(name, type)
            return name
        }

        @JsStatic
        fun removeColumn(table: TableJs, columnName: String) {
            table.columns = table.columns.filter { it.name != columnName }.toTypedArray()
        }

        private fun TableJs.column(columnName: String): TableColumnJs =
            columns.firstOrNull { it.name == columnName } ?: error("Column with name '$columnName' not found.")

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
