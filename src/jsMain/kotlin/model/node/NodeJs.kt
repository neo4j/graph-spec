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

import js.objects.Record
import js.objects.toRecord
import kotlinx.js.JsPlainObject
import model.associateBy
import model.emptyRecord
import model.extension.ExtensionValueJs
import model.extension.toClass
import model.extension.toJs
import model.jso
import model.property.PropertyJs
import model.property.toClass
import model.property.toJs
import kotlin.collections.component1
import kotlin.collections.component2

@JsExport
@JsPlainObject
external interface NodeJs {
    val labels: LabelsJs
    val properties: Record<String, PropertyJs>
    val constraints: Record<String, NodeConstraintJs>
    val indexes: Record<String, NodeIndexJs>
    val extensions: NodeExtensionsJs
    var name: String
    val id: String
    val description: String
    var aliases: Array<String>
    var reference: String
}

fun nodeJs(
    labels: LabelsJs = labelsJs(),
    properties: Record<String, PropertyJs> = emptyRecord(),
    constraints: Record<String, NodeConstraintJs> = emptyRecord(),
    indexes: Record<String, NodeIndexJs> = emptyRecord(),
    extensions: NodeExtensionsJs = nodeExtensionsJs(),
    name: String,
    id: String,
    description: String = "",
    aliases: Array<String> = emptyArray(),
    reference: String = ""
): NodeJs = jso {
    this.labels = labels
    this.properties = properties
    this.constraints = constraints
    this.indexes = indexes
    this.extensions = extensions
    this.name = name
    this.id = id
    this.description = description
    this.aliases = aliases
    this.reference = reference
}

fun Node.toJs(key: String) = nodeJs(
    labels = labels.toJs(),
    properties = properties.mapValues { (key, property) -> property.toJs(key) }.toRecord(),
    constraints = constraints.mapValues { (_, constraint) -> constraint.toJs() }.toRecord(),
    indexes = indexes.mapValues { (_, index) -> index.toJs() }.toRecord(),
    extensions = extensions.toJs(),
    name = name ?: key,
    id = key,
    description = description,
    aliases = aliases.toTypedArray(),
    reference = reference
)

fun NodeJs.toClass(id: String): Node = Node(
    labels = labels.toClass(),
    properties = properties.associateBy { key, value -> value.toClass("nodes.$id", key) },
    constraints = constraints.associateBy { _, value -> value.toClass() },
    indexes = indexes.associateBy { _, value -> value.toClass() },
    extensions = extensions.toClass(),
    name = name,
    description = description,
    aliases = aliases.toMutableSet(),
    reference = reference
)
