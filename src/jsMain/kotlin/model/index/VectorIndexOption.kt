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
    hnswEfConstruction = hnswEfConstruction,
)

fun VectorIndexOptionJs.toClass() = VectorIndexOption(
    dimensions = dimensions,
    similarityFunction = similarityFunction,
    defaultSearchExpansionFactor = defaultSearchExpansionFactor,
    quantizationEnabled = quantizationEnabled,
    quantizationType = quantizationType,
    hnswM = hnswM,
    hnswEfConstruction = hnswEfConstruction,
)
