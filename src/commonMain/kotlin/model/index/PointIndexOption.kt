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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.type.IndexType
import kotlin.js.JsExport

/**
 * https://neo4j.com/docs/cypher-manual/current/indexes/search-performance-indexes/create-indexes/#point-indexes-examples
 */
@JsExport
@Serializable
@SerialName("PointIndexOption")
data class PointIndexOption(
    @SerialName("spatial.cartesian.min")
    val cartesianMin: DoubleArray = doubleArrayOf(-1000000.0, -1000000.0),
    @SerialName("spatial.cartesian.max")
    val cartesianMax: DoubleArray = doubleArrayOf(1000000.0, 1000000.0),
    @SerialName("spatial.cartesian-3d.min")
    val cartesian3DMin: DoubleArray = doubleArrayOf(-1000000.0, -1000000.0, -1000000.0),
    @SerialName("spatial.cartesian-3d.max")
    val cartesian3DMax: DoubleArray = doubleArrayOf(1000000.0, 1000000.0, 1000000.0),
    @SerialName("spatial.wgs-84.min")
    val wgs84Min: DoubleArray = doubleArrayOf(-180.0, -90.0),
    @SerialName("spatial.wgs-84.max")
    val wgs84Max: DoubleArray = doubleArrayOf(180.0, 90.0),
    @SerialName("spatial.wgs-84-3d.min")
    val wgs843DMin: DoubleArray = doubleArrayOf(-180.0, -90.0, -1000000.0),
    @SerialName("spatial.wgs-84-3d.max")
    val wgs843DMax: DoubleArray = doubleArrayOf(180.0, 90.0, 1000000.0)
) : IndexOption {

    override val type: IndexType = IndexType.POINT

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as PointIndexOption

        if (!cartesianMin.contentEquals(other.cartesianMin)) return false
        if (!cartesianMax.contentEquals(other.cartesianMax)) return false
        if (!cartesian3DMin.contentEquals(other.cartesian3DMin)) return false
        if (!cartesian3DMax.contentEquals(other.cartesian3DMax)) return false
        if (!wgs84Min.contentEquals(other.wgs84Min)) return false
        if (!wgs84Max.contentEquals(other.wgs84Max)) return false
        if (!wgs843DMin.contentEquals(other.wgs843DMin)) return false
        if (!wgs843DMax.contentEquals(other.wgs843DMax)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cartesianMin.contentHashCode()
        result = 31 * result + cartesianMax.contentHashCode()
        result = 31 * result + cartesian3DMin.contentHashCode()
        result = 31 * result + cartesian3DMax.contentHashCode()
        result = 31 * result + wgs84Min.contentHashCode()
        result = 31 * result + wgs84Max.contentHashCode()
        result = 31 * result + wgs843DMin.contentHashCode()
        result = 31 * result + wgs843DMax.contentHashCode()
        return result
    }
}
