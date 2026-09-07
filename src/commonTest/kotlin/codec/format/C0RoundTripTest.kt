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
package codec.format

import codec.schema.SchemaLiteral
import codec.schema.schemaMapOf
import kotlin.test.Test
import kotlin.test.assertEquals

class C0RoundTripTest {

    @Test
    fun `control character in a description round trips through yaml`() {
        // ARRANGE
        val description = "bad" + 0x01.toChar() + "value"
        val writer = YamlWriter()
        val schema = schemaMapOf("description" to SchemaLiteral(description, isString = true))

        // ACT
        val out = writer.write(schema)
        val back = (YamlFormat.default.decodeFromString(out) as Map<*, *>)
        val value = (back["description"] as? SchemaLiteral)?.string ?: back["description"]

        // ASSERT
        assertEquals(description, value)
    }

    @Test
    fun `control character description emits unquoted yaml scalar`() {
        // ARRANGE
        val description = "bad" + 0x01.toChar() + "value"
        val writer = YamlWriter()

        // ACT
        val out = writer.write(SchemaLiteral(description, isString = true))

        // ASSERT - YAML 1.1 forbids C0 controls in plain scalars, but the writer emits one unquoted
        assertEquals("bad" + 0x01.toChar() + "value\n", out)
    }
}
