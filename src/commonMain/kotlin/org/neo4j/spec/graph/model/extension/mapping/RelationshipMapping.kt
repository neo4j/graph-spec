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
package org.neo4j.spec.graph.model.extension.mapping

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.neo4j.spec.graph.schema.SerialDescription
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName(MappingType.RELATIONSHIP)
@SerialDescription("Maps rows of a table to relationships.")
data class RelationshipMapping(
    @SerialDescription("Id of the relationship being mapped.")
    var relationship: String,
    @SerialDescription("Id of the source table.")
    var table: String,
    @SerialName("from_node")
    @SerialDescription("How the start node of the relationship is matched.")
    val fromNode: TargetMapping,
    @SerialName("to_node")
    @SerialDescription("How the end node of the relationship is matched.")
    val toNode: TargetMapping,
    @SerialName("match_label")
    @SerialDescription("Label used to match existing relationships.")
    var matchLabel: String? = null,
    @SerialDescription("Mappings from property ids to source columns.")
    val properties: MutableMap<String, PropertyMapping> = mutableMapOf(),
    @SerialDescription("Whether relationships are merged with existing relationships or always created.")
    var mode: MappingMode = MappingMode.MERGE,
    @SerialDescription("Ids of the properties which identify a relationship.")
    val key: MutableSet<String> = mutableSetOf(),
    @SerialName("depends_on")
    @SerialDescription(
        "Id of another mapping in the same list which must be imported before this one. " +
            "Empty when there is no dependency."
    )
    var dependsOn: String = "",
    @SerialDescription(
        "Optional id of this mapping, so other mappings in the same list can reference it in depends_on."
    )
    var id: String = ""
) : Mapping
