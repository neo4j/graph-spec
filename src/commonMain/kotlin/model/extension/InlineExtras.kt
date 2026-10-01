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

import kotlinx.serialization.SerializationException
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.put

/**
 * Shared mechanics for the untagged catch-all codecs ([model.tool.ToolSerializer],
 * [CustomExtensionSerializer], and the per-owner extension codecs of ADR-0005):
 * keys outside a codec's known-field set are collected into the model's `extra`
 * map on decode ([extractExtras]) and inlined back on encode ([putExtras]), so
 * owner-defined fields survive the round trip untouched. JSON-only: every
 * catch-all codec guards its encoder/decoder through [requireJsonEncoder] /
 * [requireJsonDecoder].
 */

/**
 * JSON-only guard, encode side: [typeName] names the codec's model in the error.
 */
internal fun Encoder.requireJsonEncoder(typeName: String): JsonEncoder =
    this as? JsonEncoder
        ?: throw SerializationException("$typeName can only be serialized as JSON")

/**
 * JSON-only guard, decode side: [typeName] names the codec's model in the error.
 */
internal fun Decoder.requireJsonDecoder(typeName: String): JsonDecoder =
    this as? JsonDecoder
        ?: throw SerializationException("$typeName can only be deserialized from JSON")

/**
 * Inlines [extras] into the object being built, skipping keys in [knownKeys].
 *
 * Known-key collision: an `extra` entry whose key collides with a known field is
 * silently dropped on encode — the known field always wins. This decision is owned
 * here, once, for every catch-all codec. It is unreachable from JSON input (a
 * decoded `extra` never contains known keys) but reachable when a model is built
 * programmatically.
 */
internal fun JsonObjectBuilder.putExtras(extras: Map<String, ExtensionValue>, knownKeys: Set<String>) {
    for ((key, extra) in extras) {
        if (key !in knownKeys) {
            put(key, ExtensionValueSerializer.toJson(extra))
        }
    }
}

/**
 * Collects every key outside [knownKeys] into a fresh `extra` map.
 */
internal fun JsonObject.extractExtras(knownKeys: Set<String>): MutableMap<String, ExtensionValue> =
    filterKeys { it !in knownKeys }
        .mapValuesTo(mutableMapOf()) { ExtensionValueSerializer.fromJson(it.value) }
