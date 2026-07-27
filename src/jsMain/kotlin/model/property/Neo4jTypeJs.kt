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
    val scalar: String
}

@JsExport
@JsPlainObject
external interface ListTypeJs : Neo4jTypeJs {
    override val type: String
    val items: String
}

@JsExport
@JsPlainObject
external interface VectorTypeJs : Neo4jTypeJs {
    override val type: String
    val items: String
    val dimension: Int?
}

fun scalarTypeJs(scalar: String): ScalarTypeJs = jso {
    this.type = Neo4jTypeKind.SCALAR
    this.scalar = scalar
}

fun listTypeJs(items: String): ListTypeJs = jso {
    this.type = Neo4jTypeKind.LIST
    this.items = items
}

fun vectorTypeJs(items: String, dimension: Int?): VectorTypeJs = jso {
    this.type = Neo4jTypeKind.VECTOR
    this.items = items
    this.dimension = dimension
}

fun Neo4jType.toJs(): Neo4jTypeJs = when (this) {
    is ScalarType -> scalarTypeJs(Neo4jScalar.toString(scalar))
    is ListType -> listTypeJs(Neo4jScalar.toString(items))
    is VectorType -> vectorTypeJs(Neo4jScalar.toString(items), dimension)
}

fun Neo4jTypeJs.toClass(): Neo4jType = when (type) {
    Neo4jTypeKind.VECTOR -> (this as VectorTypeJs).let { VectorType(toScalar(it.items), it.dimension) }
    Neo4jTypeKind.LIST -> ListType(toScalar((this as ListTypeJs).items))
    else -> ScalarType(toScalar((this as ScalarTypeJs).scalar))
}

private fun toScalar(name: String): Neo4jScalar =
    Neo4jScalar.fromString(name) ?: error("Invalid Neo4j scalar type '$name'")
