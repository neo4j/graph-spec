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

import model.GraphModel
import model.extension.getNamedList
import validate.Issue
import validate.Validation
import validate.extensionPath

/**
 * `importer:table` rule (ports 4.0.0's TableColumnEmptyName): a column map key is
 * the column's addressable name, so a blank key is meaningless. Duplicate keys are
 * impossible in a JSON object, so 4.0.0's TableColumnDuplicateName has no v1 form.
 */
object TableColumnName : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachTable { path, table ->
            table.columns.keys.forEach { column ->
                if (column.isBlank()) {
                    issues.add(
                        Issue(
                            code = "empty_table_column_name",
                            message = "Table '${table.name ?: path}' has a column with a blank name",
                            path = "$path.columns"
                        )
                    )
                }
            }
        }
    }
}

/**
 * `importer:table` rule (ports 4.0.0's TableColumnType): a column on a non-local
 * source must declare its source type; the local-source exemption is kept
 * (4.0.0 used `source != "local"` as the cloud discriminator).
 */
object TableColumnType : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachTable { path, table ->
            if (table.source == "local") return@forEachTable
            table.columns.forEach { (columnId, column) ->
                if (column.type.isBlank() && column.suggested == null) {
                    issues.add(
                        Issue(
                            code = "missing_table_column_type",
                            message = "Missing type for table column '$columnId'",
                            path = "$path.columns.$columnId.type"
                        )
                    )
                }
            }
        }
    }
}

/**
 * Root-level walk of `importer:table` payloads (top-level placement per ADR-0005).
 * Object-or-list aware: the 4.0.0 converter emits list payloads, so every element
 * is visited; per-element issue paths stay at the extension-key base (the same
 * choice the index validators make for list payloads).
 */
internal fun GraphModel.forEachTable(action: (path: String, table: TableExtension) -> Unit) {
    val path = extensionPath("", TableExtensionModule.key)
    getNamedList(TableExtensionModule).forEach { action(path, it) }
}
