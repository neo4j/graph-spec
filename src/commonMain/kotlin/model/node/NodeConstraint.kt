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
package model.node

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.node.extension.NodeConstraintExtensions
import model.type.ConstraintType
import model.type.Named
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("NodeConstraint")
@SerialDescription("A constraint on a node.")
data class NodeConstraint(
    @SerialDescription("Type of the constraint.")
    override var type: ConstraintType,
    @SerialDescription("Label the constraint applies to.")
    var label: String? = null, // TODO: Optional to avoid id issues with frontend redux?
    @SerialDescription("Ids of the properties the constraint covers.")
    override val properties: MutableSet<String>,
    @SerialDescription("Extensions for the constraint.")
    val extensions: NodeConstraintExtensions = NodeConstraintExtensions(),
    @SerialDescription("Human-readable name. Used in place of the stable id in the pretty form of the model.")
    override var name: String? = null
) : Constraint,
    Named
