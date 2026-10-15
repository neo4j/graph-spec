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

import js.objects.Record
import org.neo4j.spec.graph.model.extension.ExtensionsEditor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class AnyValueJsTest {

    @Test
    fun testToCustomJsProducesPlainValues() {
        val js = mutableMapOf<String, Any>(
            "string" to "text",
            "boolean" to true,
            "long" to 5L,
            "double" to 1.5,
            "list" to mutableListOf(1L, "x"),
            "map" to mutableMapOf("nested" to 2L)
        ).toCustomJs()

        assertEquals(
            """{"string":"text","boolean":true,"long":5,"double":1.5,"list":[1,"x"],"map":{"nested":2}}""",
            JSON.stringify(js)
        )
        assertEquals("number", jsTypeOf(js["long"]))
    }

    @Test
    fun testLongOutsideSafeRangeIsBigInt() {
        val js = mutableMapOf<String, Any>("big" to Long.MAX_VALUE).toCustomJs()
        assertEquals("bigint", jsTypeOf(js["big"]))
        assertEquals(Long.MAX_VALUE.toString(), js["big"].toString())
    }

    @Test
    fun testToCustomClassReadsPlainObjects() {
        val js: Record<String, Any> = js(
            """({ string: "text", boolean: false, integer: 5, decimal: 1.5, big: BigInt("9007199254740993"),
                list: [1, "x"], map: { nested: { deep: 2 } }, skipped: undefined })"""
        )
        val custom = js.toCustomClass()
        assertEquals(
            mapOf(
                "string" to "text",
                "boolean" to false,
                "integer" to 5L,
                "decimal" to 1.5,
                "big" to 9007199254740993L,
                "list" to listOf(1L, "x"),
                "map" to mapOf("nested" to mapOf("deep" to 2L))
            ),
            custom
        )
        assertEquals(5L, custom["integer"] as Long)
        assertEquals(1.5, custom["decimal"] as Double)
    }

    @Test
    fun testRoundTrip() {
        val custom = mutableMapOf<String, Any>(
            "a" to "b",
            "c" to listOf(1L, 2.5, mapOf("d" to true))
        )
        assertEquals<Map<String, Any>>(custom, custom.toCustomJs().toCustomClass())
    }

    @Test
    fun testUnsupportedValuesFail() {
        assertFails { mutableMapOf<String, Any>("bad" to Pair(1, 2)).toCustomJs() }
        val js: Record<String, Any> = js("({ list: [null] })")
        assertFails { js.toCustomClass() }
    }

    @Test
    fun testEditorSetsPlainValue() {
        val custom: Record<String, Any> = js("({})")
        ExtensionsEditor.set(custom, "key", 5)
        assertEquals(mapOf<String, Any>("key" to 5L), custom.toCustomClass())
    }

    @Test
    fun testDeeplyNestedValuesFail() {
        val js: Record<String, Any> = js(
            "(() => { let v = 'leaf'; for (let i = 0; i < 1000; i++) v = [v]; return { deep: v } })()"
        )
        assertFails { js.toCustomClass() }
        var nested: Any = "leaf"
        repeat(1000) { nested = listOf(nested) }
        assertFails { mutableMapOf("deep" to nested).toCustomJs() }
    }
}
