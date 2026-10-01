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
package model.tool

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
import model.extension.extractExtras
import model.extension.putExtras
import model.extension.requireJsonDecoder
import model.extension.requireJsonEncoder

/**
 * Untagged catch-all codec for [Tool]: `type`/`name`/`description` are the known fields;
 * every other key is collected into [Tool.extra] on decode and inlined back on encode,
 * so owner-defined tool shapes survive the round trip untouched. The catch-all
 * mechanics ([putExtras], [extractExtras]) are shared; this codec only owns its
 * known-field set and typed field handling.
 */
object ToolSerializer : KSerializer<Tool> {
    private val knownKeys = setOf("type", "name", "description")

    @OptIn(InternalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("Tool") {
        element("type", String.serializer().descriptor)
        element("name", String.serializer().descriptor, isOptional = true)
        element("description", String.serializer().descriptor, isOptional = true)
    }

    override fun serialize(encoder: Encoder, value: Tool) {
        encoder.requireJsonEncoder("Tool").encodeJsonElement(
            buildJsonObject {
                put("type", value.type)
                value.name?.let { put("name", it) }
                value.description?.let { put("description", it) }
                putExtras(value.extra, knownKeys)
            },
        )
    }

    override fun deserialize(decoder: Decoder): Tool {
        val obj = decoder.requireJsonDecoder("Tool").decodeJsonElement().jsonObject
        val type = obj["type"]?.jsonPrimitive?.contentOrNull
            ?: throw SerializationException("Tool requires a 'type' field")
        return Tool(
            type = type,
            name = obj["name"]?.jsonPrimitive?.contentOrNull,
            description = obj["description"]?.jsonPrimitive?.contentOrNull,
            extra = obj.extractExtras(knownKeys),
        )
    }
}
