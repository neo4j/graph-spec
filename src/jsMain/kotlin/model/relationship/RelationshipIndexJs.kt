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
import model.index.IndexOptionJs
import model.index.toClass
import model.index.toJs
import model.jso
import model.type.IndexType

@JsExport
@JsPlainObject
external interface RelationshipIndexJs {
    var type: String
    var properties: Array<String>
    var options: IndexOptionJs?
}

fun relationshipIndexJs(type: String, properties: Array<String>, options: IndexOptionJs? = null): RelationshipIndexJs =
    jso {
        this.type = type
        this.properties = properties
        this.options = options
    }

fun RelationshipIndex.toJs() = relationshipIndexJs(
    type = type.name,
    properties = properties.toTypedArray(),
    options = options?.toJs()
)

fun RelationshipIndexJs.toClass() = RelationshipIndex(
    type = IndexType.valueOf(type),
    properties = properties.toMutableSet(),
    options = options?.toClass()
)
