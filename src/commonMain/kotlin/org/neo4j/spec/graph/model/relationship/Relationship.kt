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
package org.neo4j.spec.graph.model.relationship

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.neo4j.spec.graph.model.property.Property
import org.neo4j.spec.graph.model.relationship.RelationshipConstraint
import org.neo4j.spec.graph.model.relationship.extension.RelationshipExtensions
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("Relationship")
@SerialDescription("A relationship of the graph.")
data class Relationship(
    @SerialDescription("Type of the relationship.")
    var type: String,
    @SerialDescription("The node the relationship starts at.")
    val from: RelationshipTarget,
    @SerialDescription("The node the relationship ends at.")
    val to: RelationshipTarget,
    @SerialDescription("Properties of the relationship keyed by id.")
    val properties: MutableMap<String, Property> = mutableMapOf(),
    @SerialDescription("Constraints on the relationship keyed by id.")
    val constraints: MutableMap<String, RelationshipConstraint> = mutableMapOf(),
    @SerialDescription("Extensions for the relationship.")
    val extensions: RelationshipExtensions = RelationshipExtensions(),
    @SerialDescription("Description of the relationship.")
    val description: String = "",
    @SerialDescription("Alternative names for the relationship.")
    val aliases: MutableSet<String> = mutableSetOf(),
    @SerialDescription("Reference or URI of an external resource describing the relationship.")
    val reference: String = ""
)
