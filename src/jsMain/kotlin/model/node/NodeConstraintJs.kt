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

import kotlinx.js.JsPlainObject
import model.jso
import model.type.ConstraintType

@JsExport
@JsPlainObject
external interface NodeConstraintJs {
    var type: String
    var label: String?
    var properties: Array<String>
    val extensions: NodeConstraintExtensionsJs
}

fun nodeConstraintJs(
    type: String,
    label: String? = null,
    properties: Array<String> = emptyArray(),
    extensions: NodeConstraintExtensionsJs = nodeConstraintExtensionsJs()
): NodeConstraintJs = jso {
    this.type = type
    this.label = label
    this.properties = properties
    this.extensions = extensions
}

fun NodeConstraint.toJs() = nodeConstraintJs(
    type = type.name,
    label = label,
    properties = properties.toTypedArray(),
    extensions = extensions.toJs()
)

fun NodeConstraintJs.toClass() = NodeConstraint(
    type = ConstraintType.valueOf(type),
    label = label,
    properties = properties.toMutableSet(),
    extensions = extensions.toClass()
)
