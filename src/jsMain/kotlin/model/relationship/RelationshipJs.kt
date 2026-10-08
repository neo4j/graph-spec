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
import model.jso
import model.property.PropertyJs
import model.property.extension.toClass
import model.property.extension.toJs
import model.property.toClass
import model.property.toJs
import model.relationship.extension.RelationshipExtensionsJs
import model.relationship.extension.relationshipExtensionsJs
import model.relationship.extension.toClass
import model.relationship.extension.toJs
import model.toMap
import model.value.ExtensionValueJs
import model.value.toClass
import model.value.toJs
@JsExport
@JsPlainObject
external interface RelationshipJs {
    var type: String
    val from: RelationshipTargetJs
    val to: RelationshipTargetJs
    val properties: Record<String, PropertyJs>
    val constraints: Record<String, RelationshipConstraintJs>
    val extensions: RelationshipExtensionsJs
    var name: String
    val id: String
    val description: String
    var aliases: Array<String>
    var reference: String
}

fun relationshipJs(
    type: String,
    from: RelationshipTargetJs = relationshipTargetJs(),
    to: RelationshipTargetJs = relationshipTargetJs(),
    properties: Record<String, PropertyJs> = emptyRecord(),
    constraints: Record<String, RelationshipConstraintJs> = emptyRecord(),
    extensions: RelationshipExtensionsJs = relationshipExtensionsJs(),
    name: String,
    id: String,
    description: String = "",
    aliases: Array<String> = emptyArray(),
    reference: String = ""
): RelationshipJs = jso {
    this.type = type
    this.from = from
    this.to = to
    this.properties = properties
    this.constraints = constraints
    this.extensions = extensions
    this.name = name
    this.id = id
    this.description = description
    this.aliases = aliases
    this.reference = reference
}

fun Relationship.toJs(id: String) = relationshipJs(
    type = type,
    from = from.toJs(),
    to = to.toJs(),
    properties = properties.mapValues { (key, property) -> property.toJs(key) }.toRecord(),
    constraints = constraints.mapValues { (_, constraint) -> constraint.toJs() }.toRecord(),
    extensions = extensions.toJs(),
    name = name ?: id,
    id = id,
    description = description,
    aliases = aliases.toTypedArray(),
    reference = reference
)

fun RelationshipJs.toClass(id: String) = Relationship(
    type = type,
    from = from.toClass(),
    to = to.toClass(),
    properties = properties.associateBy { _, property -> property.toClass("relationships.$id", name) },
    constraints = constraints.associateBy { _, constraint -> constraint.toClass() },
    extensions = extensions.toClass(),
    name = name,
    description = description,
    aliases = aliases.toMutableSet(),
    reference = reference
)
