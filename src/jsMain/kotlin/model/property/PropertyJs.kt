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
import model.extension.ExtensionValueJs
import model.extension.toClass
import model.extension.toJs
import model.jso

@JsExport
@JsPlainObject
external interface PropertyJs {
    var type: String?
    var dimension: Int?
    var mustExist: Boolean?
    var unique: Boolean?
    var key: Boolean?
    val oneOf: Array<ExtensionValueJs>
    var pattern: String?
    val aliases: Array<String>
    var reference: String?
    var description: String?
    val extensions: Record<String, ExtensionValueJs>
    val id: String
}

fun propertyJs(
    type: String? = null,
    dimension: Int? = null,
    mustExist: Boolean? = null,
    unique: Boolean? = null,
    key: Boolean? = null,
    oneOf: Array<ExtensionValueJs> = emptyArray(),
    pattern: String? = null,
    aliases: Array<String> = emptyArray(),
    reference: String? = null,
    description: String? = null,
    extensions: Record<String, ExtensionValueJs> = emptyRecord(),
    id: String,
): PropertyJs = jso {
    this.type = type
    this.dimension = dimension
    this.mustExist = mustExist
    this.unique = unique
    this.key = key
    this.oneOf = oneOf
    this.pattern = pattern
    this.aliases = aliases
    this.reference = reference
    this.description = description
    this.extensions = extensions
    this.id = id
}

fun Property.toJs(key: String) = propertyJs(
    type = type,
    dimension = dimension,
    mustExist = mustExist,
    unique = unique,
    key = this.key,
    oneOf = oneOf.map { it.toJs() }.toTypedArray(),
    pattern = pattern,
    aliases = aliases.toTypedArray(),
    reference = reference,
    description = description,
    extensions = extensions.mapValues { (_, extension) -> extension.toJs() }.toRecord(),
    id = key,
)

fun PropertyJs.toClass(): Property = Property(
    type = type,
    dimension = dimension,
    mustExist = mustExist,
    unique = unique,
    key = key,
    oneOf = oneOf.map { it.toClass() }.toMutableList(),
    pattern = pattern,
    aliases = aliases.toMutableList(),
    reference = reference,
    description = description,
    extensions = extensions.associateBy { _, value -> value.toClass() }.toMutableMap(),
)
