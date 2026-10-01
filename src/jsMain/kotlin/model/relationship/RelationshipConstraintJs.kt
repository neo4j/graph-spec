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

import kotlinx.js.JsPlainObject
import model.jso
import model.node.Constraint

@JsExport
@JsPlainObject
external interface RelationshipConstraintJs {
    var type: String
    var name: String?
    var properties: Array<String>
}

fun relationshipConstraintJs(
    type: String,
    name: String? = null,
    properties: Array<String> = emptyArray()
): RelationshipConstraintJs = jso {
    this.type = type
    this.name = name
    this.properties = properties
}

fun Constraint.toJs() = relationshipConstraintJs(
    type = type,
    name = name,
    properties = properties.toTypedArray()
)

fun RelationshipConstraintJs.toClass() = Constraint(
    type = type,
    properties = properties.toMutableList(),
    name = name
)
