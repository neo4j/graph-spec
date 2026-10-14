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
package org.neo4j.spec.graph.model.property

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.neo4j.spec.graph.model.property.extension.PropertyExtensions
import org.neo4j.spec.graph.model.type.Named
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("Property")
@SerialDescription("A property of a node or relationship.")
data class Property(
    @SerialDescription("Neo4j type of the property.")
    var type: Neo4jType = Neo4jType.ANY,
    @SerialDescription("Dimension of the property when it holds a vector.")
    var dimension: Int? = null,
    @SerialName("must_exist")
    @SerialDescription(
        "Shorthand for an existence constraint. Expanded into constraints when the model is internalised."
    )
    var mustExist: Boolean? = null,
    @SerialDescription(
        "Shorthand for a uniqueness constraint. Expanded into constraints when the model is internalised."
    )
    var unique: Boolean? = null,
    @SerialDescription("Shorthand for a key constraint. Expanded into constraints when the model is internalised.")
    var key: Boolean? = null,
    @SerialDescription("Extensions for the property.")
    val extensions: PropertyExtensions = PropertyExtensions(),
    @SerialDescription("Human-readable name. Used in place of the stable id in the pretty form of the model.")
    override var name: String? = null,
    @SerialDescription("Description of the property.")
    val description: String = "",
    @SerialDescription("Reference to an external resource describing the property.")
    val reference: String = "",
    @SerialDescription("Pattern values of the property are expected to match.")
    val pattern: String = "",
    @SerialName("one_of")
    @SerialDescription("Values the property is restricted to.")
    val oneOf: MutableList<String> = mutableListOf()
) : Named
