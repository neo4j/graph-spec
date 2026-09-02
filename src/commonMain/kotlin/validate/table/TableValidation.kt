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
package validate.table

import model.GraphModel
import model.source.Table
import model.source.TableColumn
import validate.Issue
import validate.Validation
import kotlin.collections.iterator
import kotlin.js.JsExport

@JsExport
interface TableValidation : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        for ((tableId, table) in model.tables) {
            validateTable(model, tableId, table, issues)
            for ((fieldId, field) in table.columns) {
                validateTableField(model, tableId, table, fieldId, field, issues)
            }
        }
    }

    fun validateTable(model: GraphModel, tableId: String, table: Table, issues: MutableList<Issue>) {
    }

    fun validateTableField(
        model: GraphModel,
        tableId: String,
        table: Table,
        fieldId: String,
        field: TableColumn,
        issues: MutableList<Issue>
    ) {
    }
}
