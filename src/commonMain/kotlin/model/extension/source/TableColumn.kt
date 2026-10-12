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

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.extension.Extensions
import model.property.Neo4jType
import model.type.Named
import model.value.ExtensionValue
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("TableColumn")
@SerialDescription("A column of a data source table.")
data class TableColumn(
    @SerialDescription("Data type of the column in the source.")
    var type: String = "",
    @SerialDescription("Size of the column. -1 when unspecified.")
    var size: Int = -1,
    @SerialDescription("Neo4j type suggested for values of this column.")
    val suggested: Neo4jType = Neo4jType.ANY,
    @SerialDescription("Neo4j types values of this column can be converted to.")
    val supported: Set<Neo4jType> = emptySet(),
    @SerialDescription("Dimension of the column when it holds vectors.")
    val dimension: Int? = null,
    @SerialDescription("User-defined arbitrary data.")
    override val custom: MutableMap<String, ExtensionValue> = mutableMapOf(),
    @SerialDescription("Human-readable name. Used in place of the stable id in the pretty form of the model.")
    override var name: String? = null
) : Extensions,
    Named
