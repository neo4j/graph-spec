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

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.elementNames
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull

@OptIn(ExperimentalSerializationApi::class)
class Neo4jTypeTest {

    private val allNames = serializer<Neo4jType>().descriptor.getElementDescriptor(1).elementNames.toList()
    private val vectorNames = allNames.filter { it.startsWith("VECTOR") }
    private val nonVectorNames = allNames - vectorNames.toSet()

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `test all serial names are registered in of`() {
        val expectedTypeCount = 38
        assertEquals(expectedTypeCount, allNames.size)
        allNames.forEach { name ->
            assertEquals(name, Neo4jType.of(name)?.typeName, "of('$name') must return the variant named '$name'")
        }
    }

    @Test
    fun `test unsupported names are not resolved`() {
        listOf(
            "", "string", "vector<float>", " STRING", "STRING ", "FLOAT64",
            "LIST<STRING", "LIST<UUID>", "LIST<ANY>", "VECTOR<STRING>", "VECTOR<FLOAT"
        ).forEach { name ->
            assertNull(Neo4jType.of(name), "of('$name') should not resolve")
        }
    }

    @Test
    fun `test dimension is carried by vector types only`() {
        vectorNames.forEach { name ->
            assertNull(Neo4jType.dimensionOf(Neo4jType.of(name)!!), "$name without a dimension")
            assertEquals(123, Neo4jType.dimensionOf(Neo4jType.of(name, dimension = 123)!!), name)
        }
        nonVectorNames.forEach { name ->
            assertNull(Neo4jType.dimensionOf(Neo4jType.of(name, dimension = 123)!!), name)
            assertEquals(Neo4jType.of(name), Neo4jType.of(name, dimension = 123), name)
        }
    }

    @Test
    fun `test dimension is part of vector equality`() {
        assertEquals(VectorFloatType(3), VectorFloatType(3))
        assertNotEquals(VectorFloatType(3), VectorFloatType(4))
        assertNotEquals(VectorFloatType(null), VectorFloatType(0))
        assertNotEquals<Neo4jType>(VectorFloatType(3), VectorFloat32Type(3))
    }

    @Test
    fun `test degenerate dimensions are accepted`() {
        listOf(0, -1).forEach { dimension ->
            assertEquals(dimension, Neo4jType.dimensionOf(Neo4jType.of("VECTOR<FLOAT>", dimension)!!))
        }
    }

    @Test
    fun `test all types round trip through json`() {
        val types = allNames.map { Neo4jType.of(it)!! } + vectorNames.map { Neo4jType.of(it, 123)!! }
        types.forEach { type ->
            val encoded = json.encodeToString(serializer<Neo4jType>(), type)
            assertEquals(type, json.decodeFromString(serializer<Neo4jType>(), encoded), encoded)
        }
    }

    @Test
    fun `test property type serializes to object form`() {
        mapOf(
            StringType to """{"type":{"type":"STRING"}}""",
            ListFloatType to """{"type":{"type":"LIST<FLOAT>"}}""",
            VectorFloatType(123) to """{"type":{"type":"VECTOR<FLOAT>","dimension":123}}""",
            VectorInteger8Type() to """{"type":{"type":"VECTOR<INTEGER8>"}}"""
        ).forEach { (type, expected) ->
            assertEquals(expected, json.encodeToString(Property.serializer(), Property(type = type)))
            assertEquals(type, json.decodeFromString(Property.serializer(), expected).type)
        }
    }

    @Test
    fun `test unknown serialized type is rejected`() {
        assertFailsWith<SerializationException> {
            json.decodeFromString(serializer<Neo4jType>(), """{"type":"VECTOR<STRING>"}""")
        }
    }

    @Test
    fun `test dimension on a non vector type is ignored when decoding`() {
        val property = json.decodeFromString(Property.serializer(), """{"type":{"type":"STRING","dimension":3}}""")
        assertEquals(StringType, property.type)
        assertNull(Neo4jType.dimensionOf(property.type))
    }
}
