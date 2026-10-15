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
package org.neo4j.spec.graph.model.extension.custom

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import org.neo4j.spec.graph.model.extension.custom.AnyValueSerializer.toAnyValue

private val json = Json {
    encodeDefaults = true
    explicitNulls = false
}

/**
 * Converts [value] into the plain types custom values are stored as: [String], [Boolean], [Long], [Double],
 * [List] and [Map].
 */
internal fun <T> toCustomValue(serializer: SerializationStrategy<T>, value: T): Any =
    json.encodeToJsonElement(serializer, value).toAnyValue()

/** Converts a plain custom value back into [T], the inverse of [toCustomValue]. */
internal fun <T> fromCustomValue(deserializer: DeserializationStrategy<T>, value: Any): T =
    json.decodeFromJsonElement(deserializer, json.encodeToJsonElement(AnyValueSerializer, value))
