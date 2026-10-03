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

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put

/**
 * Untagged catch-all codec for the [CustomExtension] envelope: `type`/`$schema`/`name`/
 * `definition` are the known fields; every other key is collected into
 * [CustomExtension.extra] on decode and inlined back on encode, carried untouched.
 * The catch-all mechanics ([putExtras], [extractExtras]) are shared; this codec
 * only owns its known-field set and typed field handling.
 */
object CustomExtensionSerializer : KSerializer<CustomExtension> {
    private val knownKeys = setOf("type", "\$schema", "name", "definition")

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("CustomExtension") {
        element("type", String.serializer().descriptor)
        element("\$schema", String.serializer().descriptor, isOptional = true)
        element("name", String.serializer().descriptor, isOptional = true)
        element("definition", ExtensionValueSerializer.descriptor, isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: CustomExtension) {
        encoder.requireJsonEncoder("CustomExtension").encodeJsonElement(
            buildJsonObject {
                put("type", value.type)
                value.schema?.let { put("\$schema", it) }
                value.name?.let { put("name", it) }
                value.definition?.let { put("definition", ExtensionValueSerializer.toJson(it)) }
                putExtras(value.extra, knownKeys)
            }
        )
    }

    override fun deserialize(decoder: Decoder): CustomExtension {
        val obj = decoder.requireJsonDecoder("CustomExtension").decodeJsonElement().jsonObject
        val type = obj["type"]?.jsonPrimitive?.contentOrNull
            ?: throw SerializationException("CustomExtension requires a 'type' field")
        return CustomExtension(
            type = type,
            schema = obj["\$schema"]?.jsonPrimitive?.contentOrNull,
            name = obj["name"]?.jsonPrimitive?.contentOrNull,
            definition = obj["definition"]?.let { ExtensionValueSerializer.fromJson(it) },
            extra = obj.extractExtras(knownKeys)
        )
    }
}
