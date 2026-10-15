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

import js.objects.Object
import js.objects.Record
import js.objects.toRecord
import js.string.JsStrings.toKotlinString
import org.neo4j.spec.graph.model.extension.custom.AnyValueSerializer.checkDepth
import org.neo4j.spec.graph.model.extension.custom.AnyValueSerializer.requireDepth
import kotlin.math.abs
import kotlin.math.floor

private const val MAX_SAFE_INTEGER = 9007199254740991L

/**
 * Converts custom extension data into plain JS values.
 * Integers are returned as `number`, or as `bigint` when outside the safe integer range.
 */
fun Map<String, Any>.toCustomJs(): Record<String, Any> {
    values.forEach { it.checkDepth() }
    return mapValues { (_, value) -> value.toJsValue() }.toRecord()
}

/**
 * Converts plain JS custom extension data into Kotlin values.
 * Integral numbers become [Long], other numbers [Double]; `null` and `undefined` map entries are skipped.
 */
fun Record<String, Any>.toCustomClass(): MutableMap<String, Any> = (this as Any).fromJsMap(0)

/** Callers must have checked the depth, see [checkDepth]. */
fun Any.toJsValue(): Any = when (this) {
    is String, is Boolean -> this
    is Byte, is Short, is Int -> (this as Number).toDouble()
    is Long -> if (this in -MAX_SAFE_INTEGER..MAX_SAFE_INTEGER) toDouble() else bigInt(toString())
    is Float, is Double -> (this as Number).toDouble()
    is List<*> -> map { (it ?: error("Custom values cannot be null")).toJsValue() }.toTypedArray()
    is Map<*, *> -> entries.associate { (key, value) ->
        key.toString() to (value ?: error("Custom values cannot be null")).toJsValue()
    }.toRecord()
    else -> error("Unsupported custom value type: ${this::class.simpleName}")
}

fun Any?.fromJsValue(depth: Int = 1): Any = when {
    this == null -> error("Custom values cannot be null or undefined")
    jsTypeOf(this) == "bigint" -> toString().toLong()
    this is Long || this is String || this is Boolean -> this
    jsTypeOf(this) == "number" -> {
        val number = this as Double
        if (number.isFinite() && number == floor(number) && abs(number) <= MAX_SAFE_INTEGER) number.toLong() else number
    }
    this is Array<*> -> asList().fromJsList(depth)
    this is List<*> -> fromJsList(depth)
    this is Map<*, *> -> {
        requireDepth(depth)
        entries.associateTo(mutableMapOf()) { (key, value) -> key.toString() to value.fromJsValue(depth + 1) }
    }
    jsTypeOf(this) == "object" -> {
        requireDepth(depth)
        fromJsMap(depth)
    }
    else -> error("Unsupported custom value type: ${jsTypeOf(this)}")
}

private fun List<*>.fromJsList(depth: Int): MutableList<Any> {
    requireDepth(depth)
    return mapTo(mutableListOf()) { it.fromJsValue(depth + 1) }
}

/** [depth] is the depth of the object itself, so its values are one deeper. */
private fun Any.fromJsMap(depth: Int): MutableMap<String, Any> {
    val record = unsafeCast<Record<String, Any?>>()
    val map = mutableMapOf<String, Any>()
    for (key in Object.keys(record)) {
        val value = record[key] ?: continue
        map[key.toKotlinString()] = value.fromJsValue(depth + 1)
    }
    return map
}

@Suppress("UNUSED_PARAMETER")
private fun bigInt(value: String): Any = js("BigInt(value)")
