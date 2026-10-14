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
package org.neo4j.spec.graph.v1.model.node

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.neo4j.spec.graph.v1.model.node.extension.NodeExtensions
import org.neo4j.spec.graph.v1.model.property.Property
import org.neo4j.spec.graph.v1.model.type.Named
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("Node")
@SerialDescription("A node of the graph.")
data class Node(
    @SerialDescription("Shorthand for the identifying label. Moved to labels when the model is internalised.")
    var label: String? = null,
    @SerialDescription("Labels of the node.")
    val labels: Labels = Labels(),
    @SerialDescription("Properties of the node keyed by id.")
    val properties: MutableMap<String, Property> = mutableMapOf(),
    @SerialDescription("Constraints on the node keyed by id.")
    val constraints: MutableMap<String, NodeConstraint> = mutableMapOf(),
    @SerialDescription("Extensions for the node.")
    val extensions: NodeExtensions = NodeExtensions(),
    @SerialDescription("Human-readable name. Used in place of the stable id in the pretty form of the model.")
    override var name: String? = null,
    @SerialDescription("Description of the node.")
    val description: String = "",
    @SerialDescription("Alternative names for the node.")
    val aliases: MutableSet<String> = mutableSetOf(),
    @SerialDescription("Reference to an external resource describing the node.")
    val reference: String = ""
) : Named
