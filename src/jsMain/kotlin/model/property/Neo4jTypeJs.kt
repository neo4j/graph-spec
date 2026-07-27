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

@JsExport
@JsPlainObject
external interface Neo4jTypeJs {
    val type: String
}

@JsExport
@JsPlainObject
external interface ScalarTypeJs : Neo4jTypeJs {
    override val type: String
}

@JsExport
@JsPlainObject
external interface ListTypeJs : Neo4jTypeJs {
    override val type: String
    val items: ScalarTypeJs
}

@JsExport
@JsPlainObject
external interface VectorTypeJs : Neo4jTypeJs {
    override val type: String
    val items: ScalarTypeJs
    val dimension: Int?
}

fun scalarTypeJs(type: String): ScalarTypeJs = jso {
    this.type = type
}

fun listTypeJs(items: ScalarTypeJs): ListTypeJs = jso {
    this.type = Neo4jTypeKind.LIST
    this.items = items
}

fun vectorTypeJs(items: ScalarTypeJs, dimension: Int?): VectorTypeJs = jso {
    this.type = Neo4jTypeKind.VECTOR
    this.items = items
    this.dimension = dimension
}

fun Neo4jType.toJs(): Neo4jTypeJs = when (this) {
    is ScalarType -> scalarTypeJs(Neo4jScalar.toString(type))
    is ListType -> listTypeJs(scalarTypeJs(Neo4jScalar.toString(items.type)))
    is VectorType -> vectorTypeJs(scalarTypeJs(Neo4jScalar.toString(items.type)), dimension)
}

fun Neo4jTypeJs.toClass(): Neo4jType = when (type) {
    Neo4jTypeKind.VECTOR -> (this as VectorTypeJs).let {
        VectorType(items = ScalarType(scalar(it.items.type)), dimension = it.dimension)
    }
    Neo4jTypeKind.LIST -> (this as ListTypeJs).let {
        ListType(items = ScalarType(scalar(it.items.type)))
    }
    else -> ScalarType(scalar(type))
}

private fun scalar(name: String): Neo4jScalar =
    Neo4jScalar.fromString(name) ?: error("Invalid Neo4j scalar type '$name'")
