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

import kotlinx.js.JsPlainObject
import model.jso

/**
 * JS view of [Neo4jType]: a plain object carrying the type name in [type] and, for `VECTOR<*>`
 * types, an optional [dimension]. Mirrors the discriminated-object wire form.
 */
@JsExport
@JsPlainObject
external interface Neo4jTypeJs {
    val type: String
    val dimension: Int?
}

fun neo4jTypeJs(type: String, dimension: Int? = null): Neo4jTypeJs = jso {
    this.type = type
    if (dimension != null) this.dimension = dimension
}

fun Neo4jType.toJs(): Neo4jTypeJs = neo4jTypeJs(typeName, Neo4jType.dimensionOf(this))

fun Neo4jTypeJs.toClass(): Neo4jType =
    Neo4jType.of(type, dimension) ?: error("Invalid neo4j type '$type'")
