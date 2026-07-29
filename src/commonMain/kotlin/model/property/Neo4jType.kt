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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("Neo4jType")
sealed interface Neo4jType {
    /** The graph-spec type name, matching this variant's [SerialName] (e.g. `"VECTOR<FLOAT>"`). */
    val typeName: String

    companion object {
        fun of(name: String, dimension: Int? = null): Neo4jType? = when (name) {
            "ANY" -> AnyType
            "BOOLEAN" -> BooleanType
            "DATE" -> DateType
            "DURATION" -> DurationType
            "FLOAT32" -> Float32Type
            "FLOAT" -> FloatType
            "INTEGER8" -> Integer8Type
            "INTEGER16" -> Integer16Type
            "INTEGER32" -> Integer32Type
            "INTEGER" -> IntegerType
            "LOCAL DATETIME" -> LocalDateTimeType
            "LOCAL TIME" -> LocalTimeType
            "POINT" -> PointType
            "STRING" -> StringType
            "ZONED DATETIME" -> ZonedDateTimeType
            "ZONED TIME" -> ZonedTimeType
            "UUID" -> UuidType
            "LIST<BOOLEAN>" -> ListBooleanType
            "LIST<DATE>" -> ListDateType
            "LIST<DURATION>" -> ListDurationType
            "LIST<FLOAT32>" -> ListFloat32Type
            "LIST<FLOAT>" -> ListFloatType
            "LIST<INTEGER8>" -> ListInteger8Type
            "LIST<INTEGER16>" -> ListInteger16Type
            "LIST<INTEGER32>" -> ListInteger32Type
            "LIST<INTEGER>" -> ListIntegerType
            "LIST<LOCAL DATETIME>" -> ListLocalDateTimeType
            "LIST<LOCAL TIME>" -> ListLocalTimeType
            "LIST<POINT>" -> ListPointType
            "LIST<STRING>" -> ListStringType
            "LIST<ZONED DATETIME>" -> ListZonedDateTimeType
            "LIST<ZONED TIME>" -> ListZonedTimeType
            "VECTOR<FLOAT>" -> VectorFloatType(dimension)
            "VECTOR<FLOAT32>" -> VectorFloat32Type(dimension)
            "VECTOR<INTEGER>" -> VectorIntegerType(dimension)
            "VECTOR<INTEGER32>" -> VectorInteger32Type(dimension)
            "VECTOR<INTEGER16>" -> VectorInteger16Type(dimension)
            "VECTOR<INTEGER8>" -> VectorInteger8Type(dimension)
            else -> null
        }

        fun dimensionOf(type: Neo4jType): Int? = when (type) {
            is VectorFloatType -> type.dimension
            is VectorFloat32Type -> type.dimension
            is VectorIntegerType -> type.dimension
            is VectorInteger32Type -> type.dimension
            is VectorInteger16Type -> type.dimension
            is VectorInteger8Type -> type.dimension
            else -> null
        }
    }
}

// --- Scalars ---

@JsExport @Serializable
@SerialName("ANY")
data object AnyType : Neo4jType {
    override val typeName get() = "ANY"
}

@JsExport @Serializable
@SerialName("BOOLEAN")
data object BooleanType : Neo4jType {
    override val typeName get() = "BOOLEAN"
}

@JsExport @Serializable
@SerialName("DATE")
data object DateType : Neo4jType {
    override val typeName get() = "DATE"
}

@JsExport @Serializable
@SerialName("DURATION")
data object DurationType : Neo4jType {
    override val typeName get() = "DURATION"
}

@JsExport @Serializable
@SerialName("FLOAT32")
data object Float32Type : Neo4jType {
    override val typeName get() = "FLOAT32"
}

@JsExport @Serializable
@SerialName("FLOAT")
data object FloatType : Neo4jType {
    override val typeName get() = "FLOAT"
}

@JsExport @Serializable
@SerialName("INTEGER8")
data object Integer8Type : Neo4jType {
    override val typeName get() = "INTEGER8"
}

@JsExport @Serializable
@SerialName("INTEGER16")
data object Integer16Type : Neo4jType {
    override val typeName get() = "INTEGER16"
}

@JsExport @Serializable
@SerialName("INTEGER32")
data object Integer32Type : Neo4jType {
    override val typeName get() = "INTEGER32"
}

@JsExport @Serializable
@SerialName("INTEGER")
data object IntegerType : Neo4jType {
    override val typeName get() = "INTEGER"
}

@JsExport @Serializable
@SerialName("LOCAL DATETIME")
data object LocalDateTimeType : Neo4jType {
    override val typeName get() = "LOCAL DATETIME"
}

@JsExport @Serializable
@SerialName("LOCAL TIME")
data object LocalTimeType : Neo4jType {
    override val typeName get() = "LOCAL TIME"
}

@JsExport @Serializable
@SerialName("POINT")
data object PointType : Neo4jType {
    override val typeName get() = "POINT"
}

@JsExport @Serializable
@SerialName("STRING")
data object StringType : Neo4jType {
    override val typeName get() = "STRING"
}

@JsExport @Serializable
@SerialName("ZONED DATETIME")
data object ZonedDateTimeType : Neo4jType {
    override val typeName get() = "ZONED DATETIME"
}

@JsExport @Serializable
@SerialName("ZONED TIME")
data object ZonedTimeType : Neo4jType {
    override val typeName get() = "ZONED TIME"
}

@JsExport @Serializable
@SerialName("UUID")
data object UuidType : Neo4jType {
    override val typeName get() = "UUID"
}

// --- Lists ---

@JsExport @Serializable
@SerialName("LIST<BOOLEAN>")
data object ListBooleanType : Neo4jType {
    override val typeName get() = "LIST<BOOLEAN>"
}

@JsExport @Serializable
@SerialName("LIST<DATE>")
data object ListDateType : Neo4jType {
    override val typeName get() = "LIST<DATE>"
}

@JsExport @Serializable
@SerialName("LIST<DURATION>")
data object ListDurationType : Neo4jType {
    override val typeName get() = "LIST<DURATION>"
}

@JsExport @Serializable
@SerialName("LIST<FLOAT32>")
data object ListFloat32Type : Neo4jType {
    override val typeName get() = "LIST<FLOAT32>"
}

@JsExport @Serializable
@SerialName("LIST<FLOAT>")
data object ListFloatType : Neo4jType {
    override val typeName get() = "LIST<FLOAT>"
}

@JsExport @Serializable
@SerialName("LIST<INTEGER8>")
data object ListInteger8Type : Neo4jType {
    override val typeName get() = "LIST<INTEGER8>"
}

@JsExport @Serializable
@SerialName("LIST<INTEGER16>")
data object ListInteger16Type : Neo4jType {
    override val typeName get() = "LIST<INTEGER16>"
}

@JsExport @Serializable
@SerialName("LIST<INTEGER32>")
data object ListInteger32Type : Neo4jType {
    override val typeName get() = "LIST<INTEGER32>"
}

@JsExport @Serializable
@SerialName("LIST<INTEGER>")
data object ListIntegerType : Neo4jType {
    override val typeName get() = "LIST<INTEGER>"
}

@JsExport @Serializable
@SerialName("LIST<LOCAL DATETIME>")
data object ListLocalDateTimeType : Neo4jType {
    override val typeName get() = "LIST<LOCAL DATETIME>"
}

@JsExport @Serializable
@SerialName("LIST<LOCAL TIME>")
data object ListLocalTimeType : Neo4jType {
    override val typeName get() = "LIST<LOCAL TIME>"
}

@JsExport @Serializable
@SerialName("LIST<POINT>")
data object ListPointType : Neo4jType {
    override val typeName get() = "LIST<POINT>"
}

@JsExport @Serializable
@SerialName("LIST<STRING>")
data object ListStringType : Neo4jType {
    override val typeName get() = "LIST<STRING>"
}

@JsExport @Serializable
@SerialName("LIST<ZONED DATETIME>")
data object ListZonedDateTimeType : Neo4jType {
    override val typeName get() = "LIST<ZONED DATETIME>"
}

@JsExport @Serializable
@SerialName("LIST<ZONED TIME>")
data object ListZonedTimeType : Neo4jType {
    override val typeName get() = "LIST<ZONED TIME>"
}

// --- Vectors ---

@JsExport @Serializable
@SerialName("VECTOR<FLOAT>")
data class VectorFloatType(val dimension: Int? = null) : Neo4jType {
    override val typeName get() = "VECTOR<FLOAT>"
}

@JsExport @Serializable
@SerialName("VECTOR<FLOAT32>")
data class VectorFloat32Type(val dimension: Int? = null) : Neo4jType {
    override val typeName get() = "VECTOR<FLOAT32>"
}

@JsExport @Serializable
@SerialName("VECTOR<INTEGER>")
data class VectorIntegerType(val dimension: Int? = null) : Neo4jType {
    override val typeName get() = "VECTOR<INTEGER>"
}

@JsExport @Serializable
@SerialName("VECTOR<INTEGER32>")
data class VectorInteger32Type(val dimension: Int? = null) : Neo4jType {
    override val typeName get() = "VECTOR<INTEGER32>"
}

@JsExport @Serializable
@SerialName("VECTOR<INTEGER16>")
data class VectorInteger16Type(val dimension: Int? = null) : Neo4jType {
    override val typeName get() = "VECTOR<INTEGER16>"
}

@JsExport @Serializable
@SerialName("VECTOR<INTEGER8>")
data class VectorInteger8Type(val dimension: Int? = null) : Neo4jType {
    override val typeName get() = "VECTOR<INTEGER8>"
}
