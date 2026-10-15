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

import kotlin.math.floor

/**
 * Extensions for storing basic arbitrary data.
 * It's encouraged to use name-spaced setters when working with extensions programmatically.
 * Though we cannot prevent users from using top-level setters if they wish to.
 */
interface CustomExtensions {
    val custom: MutableMap<String, Any>

    fun contains(key: String): Boolean = custom.containsKey(key)

    /*
        Name-spaced setters
     */

    fun set(section: String, key: String, value: String) {
        put(section, key, value)
    }

    fun set(section: String, key: String, value: Boolean) {
        put(section, key, value)
    }

    fun set(section: String, key: String, value: Int) {
        set(section, key, value.toLong())
    }

    fun set(section: String, key: String, value: Long) {
        put(section, key, value)
    }

    fun set(section: String, key: String, value: Float) {
        set(section, key, value.toDouble())
    }

    fun set(section: String, key: String, value: Double) {
        put(section, key, value)
    }

    private fun put(section: String, key: String, value: Any) {
        custom[section] = (get(section) ?: emptyMap()).toMutableMap().apply { this[key] = value }
    }

    /*
        Name-spaced getters
     */

    fun getString(section: String, key: String): String? = get(section)?.get(key)?.asString

    fun getBoolean(section: String, key: String): Boolean? = get(section)?.get(key)?.asBoolean

    fun getLong(section: String, key: String): Long? = get(section)?.get(key)?.asLong

    fun getInt(section: String, key: String): Int? = getLong(section, key)?.toInt()

    fun getDouble(section: String, key: String): Double? = get(section)?.get(key)?.asDouble

    fun getFloat(section: String, key: String): Float? = getDouble(section, key)?.toFloat()

    fun getList(section: String, key: String): List<Any>? = get(section)?.get(key)?.asList

    fun getStringList(section: String, key: String): List<String>? = getList(section, key)?.mapNotNull { it.asString }

    fun getBooleanList(section: String, key: String): List<Boolean>? = getList(section, key)?.mapNotNull {
        it.asBoolean
    }

    fun getLongList(section: String, key: String): List<Long>? = getList(section, key)?.mapNotNull { it.asLong }

    fun getIntList(section: String, key: String): List<Int>? = getList(section, key)?.mapNotNull { it.asLong?.toInt() }

    fun getDoubleList(section: String, key: String): List<Double>? = getList(section, key)?.mapNotNull { it.asDouble }

    fun getFloatList(section: String, key: String): List<Float>? = getList(section, key)?.mapNotNull {
        it.asDouble?.toFloat()
    }

    fun getMap(section: String, key: String): Map<String, Any>? = get(section)?.get(key)?.asMap

    fun getStringMap(section: String, key: String): Map<String, String>? = getMap(section, key) { it.asString }

    fun getBooleanMap(section: String, key: String): Map<String, Boolean>? = getMap(section, key) { it.asBoolean }

    fun getLongMap(section: String, key: String): Map<String, Long>? = getMap(section, key) { it.asLong }

    fun getIntMap(section: String, key: String): Map<String, Int>? = getMap(section, key) { it.asLong?.toInt() }

    fun getDoubleMap(section: String, key: String): Map<String, Double>? = getMap(section, key) { it.asDouble }

    fun getFloatMap(section: String, key: String): Map<String, Float>? = getMap(section, key) {
        it.asDouble?.toFloat()
    }

    private fun <T : Any> getMap(section: String, key: String, convert: (Any) -> T?): Map<String, T>? {
        val map = getMap(section, key) ?: return null
        val output = mutableMapOf<String, T>()
        for ((name, value) in map) {
            output[name] = convert(value) ?: continue
        }
        return output
    }

    private fun get(section: String): Map<String, Any>? = custom[section]?.asMap

    /*
        Basic top-level setters
     */

    fun set(key: String, value: String) {
        custom[key] = value
    }

    fun set(key: String, value: Boolean) {
        custom[key] = value
    }

    fun set(key: String, value: Int) {
        set(key, value.toLong())
    }

    fun set(key: String, value: Long) {
        custom[key] = value
    }

    fun set(key: String, value: Float) {
        set(key, value.toDouble())
    }

    fun set(key: String, value: Double) {
        custom[key] = value
    }

    fun setList(key: String, value: List<String>) {
        custom[key] = value.toMutableList()
    }

    fun setStringMap(key: String, value: Map<String, String>) {
        custom[key] = value.toMutableMap()
    }

    fun setIntMap(key: String, value: Map<String, Int>) {
        custom[key] = value.mapValuesTo(mutableMapOf()) { it.value.toLong() }
    }

    /*
        Basic top-level getters
     */

    fun getString(key: String): String? = custom[key]?.asString

    fun getBoolean(key: String): Boolean? = custom[key]?.asBoolean

    fun getLong(key: String): Long? = custom[key]?.asLong

    fun getInt(key: String): Int? = getLong(key)?.toInt()

    fun getDouble(key: String): Double? = custom[key]?.asDouble

    fun getFloat(key: String): Float? = getDouble(key)?.toFloat()
}

private val Any.asString: String? get() = this as? String

private val Any.asBoolean: Boolean? get() = this as? Boolean

private val Any.asLong: Long? get() = when (this) {
    is Long -> this
    is Number -> toDouble().takeIf { it == floor(it) }?.toLong()
    else -> null
}

private val Any.asDouble: Double? get() = (this as? Number)?.toDouble()

@Suppress("UNCHECKED_CAST")
private val Any.asList: List<Any>? get() = this as? List<Any>

@Suppress("UNCHECKED_CAST")
private val Any.asMap: Map<String, Any>? get() = this as? Map<String, Any>
