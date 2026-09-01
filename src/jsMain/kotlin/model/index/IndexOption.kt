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
package model.index

import kotlinx.js.JsPlainObject
import model.type.IndexType

@JsExport
@JsPlainObject
external interface IndexOptionJs {
    val type: String
}

fun IndexOption.toJs(): IndexOptionJs = when (this) {
    is FullTextIndexOption -> toJs()
    is PointIndexOption -> toJs()
    is VectorIndexOption -> toJs()
}

fun IndexOptionJs.toClass(): IndexOption = when (this.type) {
    IndexType.FULLTEXT.name -> (this as FullTextIndexOptionJs).toClass()
    IndexType.POINT.name -> (this as PointIndexOptionJs).toClass()
    IndexType.VECTOR.name -> (this as VectorIndexOptionJs).toClass()
    else -> error("Unexpected mapping type: ${this.type}")
}
