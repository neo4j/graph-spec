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
import model.jso

@JsExport
@JsPlainObject
external interface VectorIndexOptionJs : IndexOptionJs {
    var dimensions: Int?
    var similarityFunction: String
    var defaultSearchExpansionFactor: Float?
    var quantizationEnabled: Boolean
    var quantizationType: String
    var hnswM: Int
    var hnswEfConstruction: Int
}

fun vectorIndexOptionJs(
    dimensions: Int? = null,
    similarityFunction: String = "cosine",
    defaultSearchExpansionFactor: Float? = null,
    quantizationEnabled: Boolean = true,
    quantizationType: String = "scalar",
    hnswM: Int = 16,
    hnswEfConstruction: Int = 100
): VectorIndexOptionJs = jso {
    this.type = "VECTOR"
    this.dimensions = dimensions
    this.similarityFunction = similarityFunction
    this.defaultSearchExpansionFactor = defaultSearchExpansionFactor
    this.quantizationEnabled = quantizationEnabled
    this.quantizationType = quantizationType
    this.hnswM = hnswM
    this.hnswEfConstruction = hnswEfConstruction
}

fun VectorIndexOption.toJs() = vectorIndexOptionJs(
    dimensions = dimensions,
    similarityFunction = similarityFunction,
    defaultSearchExpansionFactor = defaultSearchExpansionFactor,
    quantizationEnabled = quantizationEnabled,
    quantizationType = quantizationType,
    hnswM = hnswM,
    hnswEfConstruction = hnswEfConstruction
)

fun VectorIndexOptionJs.toClass() = VectorIndexOption(
    dimensions = dimensions,
    similarityFunction = similarityFunction,
    defaultSearchExpansionFactor = defaultSearchExpansionFactor,
    quantizationEnabled = quantizationEnabled,
    quantizationType = quantizationType,
    hnswM = hnswM,
    hnswEfConstruction = hnswEfConstruction
)
