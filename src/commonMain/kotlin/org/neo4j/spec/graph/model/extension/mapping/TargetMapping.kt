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

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("TargetMapping")
@SerialDescription("Identifies a node at one end of a mapped relationship.")
data class TargetMapping(
    @SerialName("node_key_column")
    @SerialDescription(
        "Name of the column in the node's source table which identifies the node. " +
            "Mutually exclusive with node_key_property."
    )
    var nodeKeyColumn: String = "",
    @SerialName("node_key_property")
    @SerialDescription(
        "Id of the node property which identifies the node. Mutually exclusive with node_key_column."
    )
    var nodeKeyProperty: String = "",
    @SerialName("relationship_column")
    @SerialDescription("Name of the column in the relationship's source table which refers to the node.")
    var relationshipColumn: String = ""
)
