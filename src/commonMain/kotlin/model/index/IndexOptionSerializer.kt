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

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import model.type.IndexType

object IndexOptionSerializer : JsonContentPolymorphicSerializer<IndexOption>(IndexOption::class) {
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("IndexOption", PolymorphicKind.SEALED) {
        element("type", String.serializer().descriptor)

        element(
            "value",
            buildClassSerialDescriptor("IndexOptionTypes") {
                element(IndexType.FULLTEXT.name, FullTextIndexOption.serializer().descriptor)
                element(IndexType.POINT.name, PointIndexOption.serializer().descriptor)
                element(IndexType.VECTOR.name, VectorIndexOption.serializer().descriptor)
            }
        )
    }

    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<IndexOption> {
        val jsonObject = element.jsonObject
        val key = jsonObject.keys.firstOrNull { it.contains(".") }
        val prefix = key?.substringBefore(".")
        return when (prefix) {
            "fulltext" -> FullTextIndexOption.serializer()
            "spatial" -> PointIndexOption.serializer()
            "vector" -> VectorIndexOption.serializer()
            else -> throw SerializationException("Unknown IndexOption type for object: $jsonObject")
        }
    }
}
