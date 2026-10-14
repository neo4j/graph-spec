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
}
