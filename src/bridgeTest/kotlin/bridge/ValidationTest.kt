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
package bridge

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.plus
import kotlinx.cinterop.toKString
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
class ValidationTest {

    @Test
    fun testValidate() {
        // A valid v1 document: a clean parse through Validations.all yields an empty
        // issue list.
        val input = """{
            "${'$'}schema": "https://neo4j.com/ontology-graph-spec.schema.json",
            "id": "test",
            "version": 1,
            "nodes": {
                "n": {
                    "label": "Movie",
                    "constraints": [
                        {
                            "constraint_type": "unique",
                            "properties": ["title"],
                            "name": "constraint1"
                        }
                    ]
                }
            }
        }
        """.trimIndent()
        val bufferSize = 1024

        memScoped {
            val inputPtr = input.cstr.getPointer(this)
            val outputBuffer = allocArray<ByteVar>(bufferSize)

            val resultSize = validate(inputPtr, outputBuffer = outputBuffer, bufferSize = bufferSize)
            assertTrue(resultSize > 0)
            assertEquals(STATUS_OK, outputBuffer[0])
            val payload = (outputBuffer + 1)!!.toKString()
            assertEquals("[]", payload)
        }
    }

    @Test
    fun testValidateInvalidDocument() {
        // Not a v1 document: required root fields are missing, so decoding fails and the
        // bridge reports STATUS_ERROR with the failure message as payload.
        val input = """{"version": "4.0.0"}"""
        val bufferSize = 1024

        memScoped {
            val inputPtr = input.cstr.getPointer(this)
            val outputBuffer = allocArray<ByteVar>(bufferSize)

            val resultSize = validate(inputPtr, outputBuffer = outputBuffer, bufferSize = bufferSize)
            assertTrue(resultSize > 0)
            assertEquals(STATUS_ERROR, outputBuffer[0])
            val payload = (outputBuffer + 1)!!.toKString()
            assertTrue(payload.isNotEmpty())
        }
    }

    @Test
    fun testValidateDanglingEndpointReference() {
        // A v1 document that decodes but is invalid: the relationship's from endpoint
        // references a node id that is not a key in the nodes map. The bridge decodes
        // fine and surfaces the validator's issue (STATUS_OK, issue in the payload).
        val input = """{
            "${'$'}schema": "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
            "id": "test",
            "version": 1,
            "nodes": {
                "n": { "label": "Movie" }
            },
            "relationships": {
                "ACTED_IN": {
                    "type": "ACTED_IN",
                    "from": { "node": "missing-node" },
                    "to": { "node": "n" }
                }
            }
        }
        """.trimIndent()
        val bufferSize = 1024

        memScoped {
            val inputPtr = input.cstr.getPointer(this)
            val outputBuffer = allocArray<ByteVar>(bufferSize)

            val resultSize = validate(inputPtr, outputBuffer = outputBuffer, bufferSize = bufferSize)
            assertTrue(resultSize > 0)
            assertEquals(STATUS_OK, outputBuffer[0])
            val payload = (outputBuffer + 1)!!.toKString()
            assertTrue(
                payload.contains("missing_relation_from_node"),
                "Expected the dangling endpoint issue in the payload, got: $payload"
            )
            assertTrue(
                payload.contains("relationships.ACTED_IN.from.node"),
                "Expected the issue path in the payload, got: $payload"
            )
        }
    }
}
