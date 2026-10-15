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
package org.neo4j.spec.graph.codec.format

import kotlin.test.Test
import kotlin.test.assertEquals

class YamlQuotesTest {

    private val m = QUOTED_MARKER

    @Test
    fun `marks quoted map values and keys`() {
        assertEquals("\"${m}key\": \"${m}5\"\nother: 5", markQuotedScalars("\"key\": \"5\"\nother: 5"))
    }

    @Test
    fun `marks single quoted scalars with escaped quotes`() {
        assertEquals("a: '${m}it''s'\nb: 1", markQuotedScalars("a: 'it''s'\nb: 1"))
    }

    @Test
    fun `keeps escaped double quotes inside the scalar`() {
        assertEquals("a: \"${m}say \\\"hi\\\": 'x'\"", markQuotedScalars("a: \"say \\\"hi\\\": 'x'\""))
    }

    @Test
    fun `marks flow and block sequence items`() {
        assertEquals("a: [\"${m}x\", 1, '${m}y']\n- \"${m}z\"", markQuotedScalars("a: [\"x\", 1, 'y']\n- \"z\""))
        assertEquals("a: { b: \"${m}c\", d: '${m}e' }", markQuotedScalars("a: { b: \"c\", d: 'e' }"))
    }

    @Test
    fun `ignores apostrophes in plain scalars and comments`() {
        val yaml = "a: it's fine # don't \"touch\"\n# 'comment'\nb: x\"y\""
        assertEquals(yaml, markQuotedScalars(yaml))
    }

    @Test
    fun `marks indented scalars at line start`() {
        assertEquals("list:\n  - \"${m}a\"\n  -\n    \"${m}b\"", markQuotedScalars("list:\n  - \"a\"\n  -\n    \"b\""))
    }

    @Test
    fun `ignores quotes after a comma in plain block scalars`() {
        val yaml = "note: hello, \"world\""
        assertEquals(yaml, markQuotedScalars(yaml))
    }

    @Test
    fun `ignores indicators inside plain scalars`() {
        for (yaml in listOf("a: x - \"y\"", "a: what? \"y\"", "a: p, 'q', \"r\"")) {
            assertEquals(yaml, markQuotedScalars(yaml))
        }
    }

    @Test
    fun `ignores quotes in block scalars`() {
        val yaml = "note: |\n  \"Test\"\n  one\n\n  'two'\nnext: 1"
        assertEquals("note: |\n  \"Test\"\n  one\n\n  'two'\nnext: 1", markQuotedScalars(yaml))
        assertEquals(
            "a:\n  - >-\n    \"x\"\n  - \"${m}y\"\nb: |2 # c\n   'z'\nc: \"${m}d\"",
            markQuotedScalars("a:\n  - >-\n    \"x\"\n  - \"y\"\nb: |2 # c\n   'z'\nc: \"d\"")
        )
    }

    @Test
    fun `ignores quotes on plain scalar continuation lines`() {
        val yaml = "note: hello\n  \"world\"\n  more\nnext: \"x\""
        assertEquals("note: hello\n  \"world\"\n  more\nnext: \"${m}x\"", markQuotedScalars(yaml))
        assertEquals("key:\n  \"${m}v\"", markQuotedScalars("key:\n  \"v\""))
    }

    @Test
    fun `marks quoted values after anchors and tags`() {
        assertEquals("a: &x \"${m}b\"\nc: !!str '${m}d'", markQuotedScalars("a: &x \"b\"\nc: !!str 'd'"))
    }
}
