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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.extension.NamedExtension
import validate.Validation
import kotlin.js.JsExport

/**
 * The `importer:table` named extension (ADR-0005): a source table declaration,
 * kept out of the v1 core format. Lives at the document root (v1 placement rule).
 * The minimal v1 form is `{ name?, source?, columns: { <name>: { type } } }` (see the
 * movies/org examples); [primaryKeys]/[foreignKeys] and the column detail fields carry
 * the 4.0.0 source-table surface so 4.0.0-era payloads decode.
 */
@JsExport
@Serializable
data class TableExtension(
    @SerialName("\$schema")
    val schema: String? = null,
    val name: String? = null,
    val source: String? = null,
    val columns: Map<String, TableColumn> = emptyMap(),
    val primaryKeys: Set<String> = emptySet(),
    val foreignKeys: Map<String, ForeignKey> = emptyMap()
)

@JsExport
@Serializable
data class TableColumn(
    /** The source column type as declared by the source (e.g. `varchar`); blank = unknown. */
    val type: String = "",
    val size: Int? = null,
    /** Suggested Neo4j property type token (v1 `$defs/propertyType`); absent = no suggestion. */
    val suggested: String? = null,
    /** Neo4j property type tokens the column's values support; empty = unconstrained. */
    val supported: Set<String> = emptySet(),
    val dimension: Int? = null
)

@JsExport
@Serializable
data class ForeignKey(val columns: Set<String>, val references: ForeignKeyReference)

@JsExport
@Serializable
data class ForeignKeyReference(val table: String, val columns: Set<String> = emptySet())

/** The `importer:table` module: wire key, payload codec, and the table validators. */
object TableExtensionModule : NamedExtension<TableExtension> {
    override val key: String = "importer:table"
    override val serializer = TableExtension.serializer()
    override val validations: List<Validation> = listOf(TableColumnName, TableColumnType)
}
