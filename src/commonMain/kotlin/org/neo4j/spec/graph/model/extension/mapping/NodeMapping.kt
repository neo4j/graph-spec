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
    @SerialName("match_label")
    @SerialDescription("Label used to match existing nodes.")
    var matchLabel: String? = null,
    @SerialName("key_columns")
    @SerialDescription(
        "Names of the source table columns which identify a node. Mutually exclusive with key_properties."
    )
    val keyColumns: MutableSet<String> = mutableSetOf(),
    @SerialName("key_properties")
    @SerialDescription(
        "Ids of the properties which identify a node. Mutually exclusive with key_columns."
    )
    val keyProperties: MutableSet<String> = mutableSetOf(),
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
) : Mapping {
    /**
     * Identifies by key properties instead of key columns, replacing [keyProperties] with the properties mapped to
     * the current [keyColumns] and clearing [keyColumns]. Does nothing if there are no key columns.
     *
     * @throws IllegalArgumentException if a key column is not mapped to a property, leaving the mapping unchanged.
     */
    fun useKeyProperties() {
        if (keyColumns.isEmpty()) {
            return
        }
        val converted = columnsToProperties(keyColumns, properties)
        keyProperties.clear()
        keyProperties.addAll(converted)
        keyColumns.clear()
    }

    /**
     * Identifies by key columns instead of key properties, replacing [keyColumns] with the columns of the current
     * [keyProperties] and clearing [keyProperties]. Does nothing if there are no key properties.
     *
     * @throws IllegalArgumentException if a key property is not mapped to a column, leaving the mapping unchanged.
     */
    fun useKeyColumns() {
        if (keyProperties.isEmpty()) {
            return
        }
        val converted = propertiesToColumns(keyProperties, properties)
        keyColumns.clear()
        keyColumns.addAll(converted)
        keyProperties.clear()
    }
}
