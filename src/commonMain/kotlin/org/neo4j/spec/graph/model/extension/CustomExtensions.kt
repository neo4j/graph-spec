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
package org.neo4j.spec.graph.model.extension

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.serializer
import org.neo4j.spec.graph.model.extension.custom.fromCustomValue
import org.neo4j.spec.graph.model.extension.custom.toCustomValue

/**
 * Extensions for storing arbitrary data in [custom].
 * Plain values (String, Boolean, Long, Double, List and Map) can be put in [custom] directly,
 * helpers used to store and read `@Serializable` objects.
 */
interface CustomExtensions {
    val custom: MutableMap<String, Any>

    /**
     * Stores a serializable object under [section] as plain maps, lists and primitives.
     */
    fun <T> setSerializable(section: String, key: String, value: T, serializer: SerializationStrategy<T>) {
        val existing = custom[section] as? Map<*, *>
        val updated = mutableMapOf<String, Any>()
        existing?.forEach { (name, entry) -> if (name is String && entry != null) updated[name] = entry }
        updated[key] = toCustomValue(serializer, value)
        custom[section] = updated
    }

    /**
     * Stores a serializable object as plain maps, lists and primitives.
     * See also the reified [setSerializable] extension.
     */
    fun <T> setSerializable(key: String, value: T, serializer: SerializationStrategy<T>) {
        custom[key] = toCustomValue(serializer, value)
    }

    /**
     * Reads the object stored under [section] and [key] back into [T],
     * or `null` if there isn't one. See also the reified [getSerializable] extension.
     * @throws kotlinx.serialization.SerializationException if the stored value doesn't match [T].
     */
    fun <T> getSerializable(section: String, key: String, deserializer: DeserializationStrategy<T>): T? {
        val value = (custom[section] as? Map<*, *>)?.get(key) ?: return null
        return fromCustomValue(deserializer, value)
    }

    /**
     * Reads the object stored under [key] back into [T], or `null` if there isn't one.
     * See also the reified [getSerializable] extension.
     * @throws kotlinx.serialization.SerializationException if the stored value doesn't match [T].
     */
    fun <T> getSerializable(key: String, deserializer: DeserializationStrategy<T>): T? {
        val value = custom[key] ?: return null
        return fromCustomValue(deserializer, value)
    }
}

/**
 * Stores a `@Serializable` object (or collection of them) as plain maps, lists and primitives.
 * Unlike the plain setters this works with generic classes as the type arguments are known.
 */
inline fun <reified T> CustomExtensions.setSerializable(key: String, value: T) =
    setSerializable(key, value, serializer<T>())

/** Name-spaced version of [setSerializable]. */
inline fun <reified T> CustomExtensions.setSerializable(section: String, key: String, value: T) =
    setSerializable(section, key, value, serializer<T>())

/** Reads back a `@Serializable` object stored with [setSerializable], or `null` if there isn't one. */
inline fun <reified T> CustomExtensions.getSerializable(key: String): T? = getSerializable(key, serializer<T>())

/** Name-spaced version of [getSerializable]. */
inline fun <reified T> CustomExtensions.getSerializable(section: String, key: String): T? =
    getSerializable(section, key, serializer<T>())
