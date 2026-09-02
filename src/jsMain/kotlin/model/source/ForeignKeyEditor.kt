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

import model.dropAt
import kotlin.collections.plus

@JsExport
class ForeignKeyEditor {
    companion object {
        @JsStatic
        fun addColumn(foreignKey: ForeignKeyJs, column: String) {
            if (!foreignKey.columns.contains(column)) {
                foreignKey.columns += column
            }
        }

        @JsStatic
        fun removeColumn(foreignKey: ForeignKeyJs, column: String) {
            val index = foreignKey.columns.indexOf(column)
            if (index != -1) {
                foreignKey.columns = foreignKey.columns.dropAt(index)
            }
        }

        @JsStatic
        fun setReferenceTable(foreignKey: ForeignKeyJs, table: String) {
            ForeignKeyReferenceEditor.setTable(foreignKey.references, table)
        }

        @JsStatic
        fun addReferenceColumn(foreignKey: ForeignKeyJs, column: String) {
            ForeignKeyReferenceEditor.addColumn(foreignKey.references, column)
        }

        @JsStatic
        fun removeReferenceColumn(foreignKey: ForeignKeyJs, column: String) {
            ForeignKeyReferenceEditor.removeColumn(foreignKey.references, column)
        }
    }
}
