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
package model.extension.neo4j.index

import kotlinx.js.JsPlainObject
import model.jso
import kotlin.js.JsExport

@JsExport
@JsPlainObject
external interface IndexExtensionJs {
    var name: String?
    var type: String?
    var properties: Array<String>
    var options: IndexOptionsJs?
}

@JsExport
@JsPlainObject
external interface IndexOptionsJs {
    var fulltext: FullTextIndexOptionsJs?
    var point: PointIndexOptionsJs?
    var vector: VectorIndexOptionsJs?
}

@JsExport
@JsPlainObject
external interface FullTextIndexOptionsJs {
    var defaultAnalyzer: String
    var analyzer: String?
    var eventuallyConsistent: Boolean
}

@JsExport
@JsPlainObject
external interface PointIndexOptionsJs {
    var cartesianMin: Array<Double>?
    var cartesianMax: Array<Double>?
    var wgs84Min: Array<Double>?
    var wgs84Max: Array<Double>?
}

@JsExport
@JsPlainObject
external interface VectorIndexOptionsJs {
    var dimensions: Int?
    var similarityFunction: String
}

fun indexExtensionJs(
    name: String? = null,
    type: String? = null,
    properties: Array<String> = emptyArray(),
    options: IndexOptionsJs? = null
): IndexExtensionJs = jso {
    this.name = name
    this.type = type
    this.properties = properties
    this.options = options
}

fun IndexExtension.toJs() = indexExtensionJs(
    name = name,
    type = type,
    properties = properties.toTypedArray(),
    options = options?.toJs()
)

fun IndexOptions.toJs(): IndexOptionsJs = jso {
    this.fulltext = fulltext?.toJs()
    this.point = point?.toJs()
    this.vector = vector?.toJs()
}

fun FullTextIndexOptions.toJs(): FullTextIndexOptionsJs = jso {
    this.defaultAnalyzer = defaultAnalyzer
    this.analyzer = analyzer
    this.eventuallyConsistent = eventuallyConsistent
}

fun PointIndexOptions.toJs(): PointIndexOptionsJs = jso {
    this.cartesianMin = cartesianMin?.toTypedArray()
    this.cartesianMax = cartesianMax?.toTypedArray()
    this.wgs84Min = wgs84Min?.toTypedArray()
    this.wgs84Max = wgs84Max?.toTypedArray()
}

fun VectorIndexOptions.toJs(): VectorIndexOptionsJs = jso {
    this.dimensions = dimensions
    this.similarityFunction = similarityFunction
}

fun IndexExtensionJs.toClass(): IndexExtension = IndexExtension(
    name = name,
    type = type,
    properties = properties.toList(),
    options = options?.toClass()
)

fun IndexOptionsJs.toClass(): IndexOptions = IndexOptions(
    fulltext = fulltext?.toClass(),
    point = point?.toClass(),
    vector = vector?.toClass()
)

fun FullTextIndexOptionsJs.toClass(): FullTextIndexOptions = FullTextIndexOptions(
    defaultAnalyzer = defaultAnalyzer,
    analyzer = analyzer,
    eventuallyConsistent = eventuallyConsistent
)

fun PointIndexOptionsJs.toClass(): PointIndexOptions = PointIndexOptions(
    cartesianMin = cartesianMin?.toList(),
    cartesianMax = cartesianMax?.toList(),
    wgs84Min = wgs84Min?.toList(),
    wgs84Max = wgs84Max?.toList()
)

fun VectorIndexOptionsJs.toClass(): VectorIndexOptions = VectorIndexOptions(
    dimensions = dimensions,
    similarityFunction = similarityFunction
)

@JsExport
class IndexExtensionEditor {
    companion object {
        @JsStatic
        fun setName(index: IndexExtensionJs, name: String?) {
            index.name = name
        }

        @JsStatic
        fun setType(index: IndexExtensionJs, type: String?) {
            index.type = type
        }

        @JsStatic
        fun addProperty(index: IndexExtensionJs, property: String) {
            if (!index.properties.contains(property)) {
                index.properties = index.properties + property
            }
        }

        @JsStatic
        fun removeProperty(index: IndexExtensionJs, property: String) {
            index.properties = index.properties.filter { it != property }.toTypedArray()
        }
    }
}
