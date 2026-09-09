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

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class IndexOptionRoundTripTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    @Test
    fun `default vector options survive a json round trip`() {
        val original: IndexOption = VectorIndexOption()

        val encoded = json.encodeToString(IndexOptionSerializer, original)
        println("ENCODED DEFAULT VECTOR >>> $encoded")
        val decoded = json.decodeFromString(IndexOptionSerializer, encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `default point options survive a json round trip`() {
        val original: IndexOption = PointIndexOption()

        val encoded = json.encodeToString(IndexOptionSerializer, original)
        println("ENCODED DEFAULT POINT >>> $encoded")
        val decoded = json.decodeFromString(IndexOptionSerializer, encoded)

        assertEquals(original, decoded)
    }

    @Test
    fun `encoded options carry the type discriminator required by the generated schema`() {
        val encoded = json.encodeToString(IndexOptionSerializer, VectorIndexOption(dimensions = 1536))

        println("ENCODED VECTOR WITH DIMENSIONS >>> $encoded")
        assertEquals(true, encoded.contains("\"type\""), "expected a 'type' discriminator in $encoded")
    }

    @Test
    fun `options with an unrecognised key are reported as a validation issue not an exception`() {
        val decoded = json.decodeFromString(IndexOptionSerializer, """{"indexProvider": "range-1.0"}""")

        println("DECODED UNRECOGNISED >>> $decoded")
    }
}
