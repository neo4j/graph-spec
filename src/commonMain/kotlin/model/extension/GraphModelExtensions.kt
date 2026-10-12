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
package model.extension

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.GraphModel
import model.extension.Extensions
import model.extension.mapping.Mapping
import model.extension.source.Table
import model.value.ExtensionValue
import kotlin.js.JsExport

/**
 * Extensions for a [GraphModel].
 */
@JsExport
@Serializable
@SerialName("GraphModelExtensions")
@SerialDescription("Extensions for a graph model.")
data class GraphModelExtensions(
    @SerialDescription("User-defined arbitrary data.")
    override val custom: MutableMap<String, ExtensionValue> = mutableMapOf(),
    @SerialDescription("Data source table schemas keyed by table id.")
    val tables: MutableMap<String, Table> = mutableMapOf(),
    @SerialDescription("Import mappings from tables to nodes and relationships.")
    val mappings: MutableList<Mapping> = mutableListOf()
) : Extensions
