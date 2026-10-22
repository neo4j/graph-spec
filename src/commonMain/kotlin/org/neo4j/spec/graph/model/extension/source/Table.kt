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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.neo4j.spec.graph.model.extension.CustomExtensions
import org.neo4j.spec.graph.model.extension.custom.CustomMapSerializer
import org.neo4j.spec.graph.schema.SerialDescription
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("Table")
@SerialDescription("A data source table schema.")
data class Table(
    @SerialDescription("Where the table's data comes from.")
    var source: String,
    @SerialDescription("Columns of the table in their source order.")
    val columns: MutableList<TableColumn> = mutableListOf(),
    @SerialName("primary_keys")
    @SerialDescription("Names of the columns making up the primary key.")
    val primaryKeys: MutableList<String> = mutableListOf(),
    @SerialName("foreign_keys")
    @SerialDescription("Foreign keys of the table keyed by id.")
    val foreignKeys: MutableMap<String, ForeignKey> = mutableMapOf(),
    @SerialDescription("User-defined arbitrary data.")
    @Serializable(with = CustomMapSerializer::class)
    override val custom: MutableMap<String, Any> = mutableMapOf()
) : CustomExtensions
