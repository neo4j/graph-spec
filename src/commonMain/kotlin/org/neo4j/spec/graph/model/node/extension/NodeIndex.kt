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
package org.neo4j.spec.graph.model.node.extension

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.neo4j.spec.graph.model.extension.Extensions
import org.neo4j.spec.graph.model.extension.custom.CustomMapSerializer
import org.neo4j.spec.graph.model.index.IndexOption
import org.neo4j.spec.graph.model.type.IndexType
import org.neo4j.spec.graph.model.type.Named
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("NodeIndex")
@SerialDescription("An index on a node.")
data class NodeIndex(
    @SerialDescription("Type of the index.")
    var type: IndexType,
    @SerialDescription("Labels the index covers.")
    val labels: MutableSet<String>,
    @SerialDescription("Ids of the properties the index covers.")
    val properties: MutableSet<String>,
    @SerialDescription("Configuration specific to the type of index.")
    val options: IndexOption? = null,
    @SerialDescription("User-defined arbitrary data.")
    @Serializable(with = CustomMapSerializer::class)
    override val custom: MutableMap<String, Any> = mutableMapOf(),
    @SerialDescription("Human-readable name. Used in place of the stable id in the pretty form of the model.")
    override var name: String? = null
) : Extensions,
    Named
