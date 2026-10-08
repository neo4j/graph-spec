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
package model.property

import js.objects.Record
import js.objects.toRecord
import kotlinx.js.JsPlainObject
import model.associateBy
import model.emptyRecord
import model.jso
import model.property.extension.PropertyExtensionsJs
import model.property.extension.propertyExtensionsJs
import model.property.extension.toClass
import model.property.extension.toJs
import model.value.ExtensionValueJs
import model.value.toClass
import model.value.toJs
@JsExport
@JsPlainObject
external interface PropertyJs {
    var type: String
    var dimension: Int?
    var mustExist: Boolean?
    var unique: Boolean?
    var key: Boolean?
    val extensions: PropertyExtensionsJs
    var name: String
    val id: String
    val description: String
    var reference: String
    var pattern: String
    var oneOf: Array<String>
}

fun propertyJs(
    type: String = "ANY",
    dimension: Int? = null,
    mustExist: Boolean? = null,
    unique: Boolean? = null,
    key: Boolean? = null,
    extensions: PropertyExtensionsJs = propertyExtensionsJs(),
    name: String,
    id: String,
    description: String = "",
    reference: String = "",
    pattern: String = "",
    oneOf: Array<String> = emptyArray()
): PropertyJs = jso {
    this.type = type
    this.dimension = dimension
    this.mustExist = mustExist
    this.unique = unique
    this.key = key
    this.extensions = extensions
    this.name = name
    this.id = id
    this.description = description
    this.reference = reference
    this.pattern = pattern
    this.oneOf = oneOf
}

fun Property.toJs(key: String) = propertyJs(
    type = Neo4jType.toString(type),
    dimension = dimension,
    mustExist = mustExist,
    unique = unique,
    key = this.key,
    extensions = extensions.toJs(),
    name = name ?: key,
    id = key,
    description = description,
    reference = reference,
    pattern = pattern,
    oneOf = oneOf.toTypedArray()
)

fun PropertyJs.toClass(parent: String, property: String): Property {
    val type = type
    val neo4jType = Neo4jType.entries.firstOrNull { Neo4jType.toString(it) == type }
        ?: error("Invalid neo4j type '$type' for $parent.properties.$property")
    return Property(
        type = neo4jType,
        dimension = dimension,
        mustExist = mustExist,
        unique = unique,
        key = key,
        extensions = extensions.toClass(),
        name = name,
        description = description,
        reference = reference,
        pattern = pattern,
        oneOf = oneOf.toMutableList()
    )
}
