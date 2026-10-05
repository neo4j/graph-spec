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
package model.extension

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Untagged raw-JSON codec for the [ExtensionValue] tree (v1: extension payloads are
 * never validated and are carried as-is). JSON objects map to [MapValue], arrays to
 * [ListValue], and primitives to [StringValue]/[BooleanValue]/[LongValue]/[DoubleValue].
 * JSON null has no [ExtensionValue] kind and is rejected.
 */
object ExtensionValueSerializer : KSerializer<ExtensionValue> {
    @OptIn(InternalSerializationApi::class, ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildSerialDescriptor("ExtensionValue", PolymorphicKind.SEALED)

    override fun serialize(encoder: Encoder, value: ExtensionValue) {
        val jsonEncoder = encoder.requireJsonEncoder("ExtensionValue")
        jsonEncoder.encodeJsonElement(toJson(value))
    }

    override fun deserialize(decoder: Decoder): ExtensionValue {
        val jsonDecoder = decoder.requireJsonDecoder("ExtensionValue")
        return fromJson(jsonDecoder.decodeJsonElement())
    }

    fun toJson(value: ExtensionValue): JsonElement = when (value) {
        is StringValue -> JsonPrimitive(value.value)
        is BooleanValue -> JsonPrimitive(value.value)
        is LongValue -> JsonPrimitive(value.value)
        is DoubleValue -> JsonPrimitive(value.value)
        is ListValue -> JsonArray(value.value.map { toJson(it) })
        is MapValue -> JsonObject(value.value.mapValues { toJson(it.value) })
    }

    fun fromJson(element: JsonElement): ExtensionValue = when (element) {
        is JsonObject -> MapValue(
            element.mapValuesTo(mutableMapOf()) { fromJson(it.value) }
        )
        is JsonArray -> ListValue(
            element.mapTo(mutableListOf()) { fromJson(it) }
        )
        is JsonPrimitive -> fromPrimitive(element)
    }

    private fun fromPrimitive(element: JsonPrimitive): ExtensionValue = when {
        element.isString -> StringValue(element.content)
        element.booleanOrNull != null -> BooleanValue(element.content.toBoolean())
        element.longOrNull != null -> LongValue(element.content.toLong())
        element.doubleOrNull != null -> DoubleValue(element.content.toDouble())
        else -> throw SerializationException(
            "Unsupported extension value (null is not representable in ExtensionValue): $element"
        )
    }
}
