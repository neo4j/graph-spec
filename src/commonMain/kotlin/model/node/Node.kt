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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.extension.ExtensionValue
import model.extension.Extensions
import model.property.Property
import model.spec.SpecDef
import model.spec.SpecDoc
import model.spec.SpecFormat
import model.spec.SpecPropertyNames
import model.tool.Tool
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("Node")
@SpecDoc(
    "Every node carries its label in label (shorthand for an identifier-only labels) or " +
        "labels.identifier (when implied/optional labels exist). Not schema-enforced; a " +
        "node with neither is meaningless."
)
data class Node(
    @SpecDoc("Shorthand for labels.identifier when no implied/optional labels exist.")
    var label: String? = null,
    @SpecDoc("Full labels object: identifier plus implied/optional labels.")
    val labels: Labels? = null,
    @SpecDoc("Properties of this node type; the map key is the property name.")
    @SpecPropertyNames(
        1,
        "Property names are non-empty (ADR-0010): the map key is the property's " +
            "addressable name, the string constraint objects and extensions reference."
    )
    val properties: MutableMap<String, Property> = mutableMapOf(),
    @SpecDoc("Constraint objects on this node type (the nameable alternative to the property flags).")
    val constraints: MutableList<Constraint> = mutableListOf(),
    @SpecDoc("Tools available on this node type.")
    val tools: MutableList<Tool> = mutableListOf(),
    @SpecDoc("Alternative names for this node type.")
    val aliases: MutableList<String> = mutableListOf(),
    @SpecDef("reference")
    @SpecFormat("uri")
    @SpecDoc(
        "A single informational URI pointing at an external definition of this element " +
            "(e.g. the original RDF resource)."
    )
    var reference: String? = null,
    @SpecDoc("Human-readable description of this node type.")
    var description: String? = null,
    @SpecDoc("Named extensions map: first-party neo4j:* keys; custom envelopes under the reserved custom key.")
    override val extensions: MutableMap<String, ExtensionValue> = mutableMapOf()
) : Extensions
