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
package model.relationship

import js.objects.Record
import js.objects.toRecord
import kotlinx.js.JsPlainObject
import model.associateBy
import model.emptyRecord
import model.extension.ExtensionValueJs
import model.extension.toClass
import model.extension.toJs
import model.jso
import model.node.toClass
import model.node.toJs
import model.property.PropertyJs
import model.property.toClass
import model.property.toJs
import model.tool.ToolJs
import model.tool.toClass
import model.tool.toJs
import kotlin.collections.component1
import kotlin.collections.component2

@JsExport
@JsPlainObject
external interface RelationshipJs {
    var type: String
    val from: RelationshipTargetJs
    val to: RelationshipTargetJs
    val properties: Record<String, PropertyJs>
    var constraints: Array<RelationshipConstraintJs>
    val tools: Array<ToolJs>
    val aliases: Array<String>
    var reference: String?
    var description: String?
    val extensions: Record<String, ExtensionValueJs>
    val id: String
}

fun relationshipJs(
    type: String,
    from: RelationshipTargetJs = relationshipTargetJs(),
    to: RelationshipTargetJs = relationshipTargetJs(),
    properties: Record<String, PropertyJs> = emptyRecord(),
    constraints: Array<RelationshipConstraintJs> = emptyArray(),
    tools: Array<ToolJs> = emptyArray(),
    aliases: Array<String> = emptyArray(),
    reference: String? = null,
    description: String? = null,
    extensions: Record<String, ExtensionValueJs> = emptyRecord(),
    id: String,
): RelationshipJs = jso {
    this.type = type
    this.from = from
    this.to = to
    this.properties = properties
    this.constraints = constraints
    this.tools = tools
    this.aliases = aliases
    this.reference = reference
    this.description = description
    this.extensions = extensions
    this.id = id
}

fun Relationship.toJs(id: String) = relationshipJs(
    type = type,
    from = from.toJs(),
    to = to.toJs(),
    properties = properties.mapValues { (key, property) -> property.toJs(key) }.toRecord(),
    constraints = constraints.map { it.toJs() }.toTypedArray(),
    tools = tools.map { it.toJs() }.toTypedArray(),
    aliases = aliases.toTypedArray(),
    reference = reference,
    description = description,
    extensions = extensions.mapValues { (_, extension) -> extension.toJs() }.toRecord(),
    id = id,
)

fun RelationshipJs.toClass() = Relationship(
    type = type,
    from = from.toClass(),
    to = to.toClass(),
    properties = properties.associateBy { _, property -> property.toClass() },
    constraints = constraints.map { it.toClass() }.toMutableList(),
    tools = tools.map { it.toClass() }.toMutableList(),
    aliases = aliases.toMutableList(),
    reference = reference,
    description = description,
    extensions = extensions.associateBy { _, value -> value.toClass() }.toMutableMap(),
)
