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
package model.extension.mapping

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName(MappingType.NODE)
@SerialDescription("Maps rows of a table to nodes.")
data class NodeMapping(
    @SerialDescription("Id of the node being mapped.")
    var node: String,
    @SerialDescription("Id of the source table.")
    var table: String,
    @SerialDescription("Mappings from property ids to source columns.")
    val properties: MutableMap<String, PropertyMapping>,
    @SerialDescription("Whether nodes are merged with existing nodes or always created.")
    var mode: MappingMode = MappingMode.MERGE,
    @SerialDescription("Label used to match existing nodes.")
    var matchLabel: String? = null,
    @SerialDescription("Ids of the properties which identify a node.")
    val key: MutableSet<String> = mutableSetOf()
) : Mapping
