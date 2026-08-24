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

import codec.format.JsonFormat
import codec.schema.SchemaMap
import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CArrayPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.cstr
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalForeignApi::class)
class BridgeTest {

    private fun response(buffer: CArrayPointer<ByteVar>, size: Int): Pair<Byte, String> =
        buffer[0] to ByteArray(size - 1) { buffer[it + 1] }.decodeToString()

    @Test
    fun testSuccessfulBridgeCallWithSingleInput() {
        val input = "{\"version\":\"1\", \"data\":\"test\"}"
        val bufferSize = 1024

        memScoped {
            val inputPtr = input.cstr.getPointer(this)
            val outputBuffer = allocArray<ByteVar>(bufferSize)
            val format = JsonFormat.default

            val resultSize = invokeBridge(inputPtr, outputBuffer = outputBuffer, bufferSize = bufferSize) {
                val schema = format.decodeFromString(it[0]) as SchemaMap
                schema["version"] = "2"
                format.encodeToString(schema)
            }

            assertTrue(resultSize > 0)
            val (status, payload) = response(outputBuffer, resultSize)
            assertEquals(STATUS_OK, status)
            val output = format.decodeFromString(payload) as SchemaMap
            assertEquals(output["data"].toString(), "test")
            assertEquals(output["version"].toString(), "2")
        }
    }

    @Test
    fun testSuccessfulBridgeCallWithMultipleInputs() {
        val inputJson = "{\"version\":\"1\", \"data\":\"test\"}"
        val inputVersion = "3"
        val bufferSize = 1024

        memScoped {
            val inputJsonPtr = inputJson.cstr.getPointer(this)
            val inputVersionPtr = inputVersion.cstr.getPointer(this)
            val outputBuffer = allocArray<ByteVar>(bufferSize)
            val format = JsonFormat.default

            val resultSize =
                invokeBridge(inputJsonPtr, inputVersionPtr, outputBuffer = outputBuffer, bufferSize = bufferSize) {
                    val schema = format.decodeFromString(it[0]) as SchemaMap
                    schema["version"] = it[1]
                    format.encodeToString(schema)
                }

            assertTrue(resultSize > 0)
            val (status, payload) = response(outputBuffer, resultSize)
            assertEquals(STATUS_OK, status)
            val output = format.decodeFromString(payload) as SchemaMap
            assertEquals(output["data"].toString(), "test")
            assertEquals(output["version"].toString(), inputVersion)
        }
    }

    @Test
    fun testNullInputs() {
        val input = "{\"version\":\"1\", \"data\":\"test\"}"
        val bufferSize = 1024

        memScoped {
            val inputPtr = input.cstr.getPointer(this)
            val outputBuffer = allocArray<ByteVar>(bufferSize)

            var resultSize = invokeBridge(null, outputBuffer = outputBuffer, bufferSize = bufferSize) { it[0] }
            assertEquals(resultSize, -1)

            resultSize = invokeBridge(inputPtr, null, outputBuffer = outputBuffer, bufferSize = bufferSize) { it[0] }
            assertEquals(resultSize, -1)

            resultSize = invokeBridge(inputPtr, outputBuffer = null, bufferSize = bufferSize) { it[0] }
            assertEquals(resultSize, -1)

            resultSize = invokeBridge(inputPtr, outputBuffer = outputBuffer, bufferSize = -1) { it[0] }
            assertEquals(resultSize, -1)
        }
    }

    @Test
    fun testBufferTooSmall() {
        val input = "{\"version\":\"1\", \"data\":\"test\"}"
        val bufferSize = 5 // Purposely too small for result to be written to

        memScoped {
            val inputPtr = input.cstr.getPointer(this)
            val outputBuffer = allocArray<ByteVar>(bufferSize)

            val resultSize = invokeBridge(inputPtr, outputBuffer = outputBuffer, bufferSize = bufferSize) { it[0] }

            // Should return a negative number indicating the required size
            assertEquals(-(input.encodeToByteArray().size + 1), resultSize)
        }
    }

    @Test
    fun testMultiByteUtf8Payload() {
        val responsePayload = "aé€😀" // multi-byte characters (1+2+3+4=10bytes)
        val expected = responsePayload.encodeToByteArray()
        val bufferSize = 1024

        memScoped {
            val inputPtr = "{}".cstr.getPointer(this)
            val outputBuffer = allocArray<ByteVar>(bufferSize)

            val resultSize = invokeBridge(inputPtr, outputBuffer = outputBuffer, bufferSize = bufferSize) { responsePayload }

            // The size from the response should be bytes, not characters
            assertEquals(expected.size + 1, resultSize)
            assertTrue(expected.size > responsePayload.length)
            val (status, text) = response(outputBuffer, resultSize)
            assertEquals(STATUS_OK, status)
            assertEquals(expected.decodeToString(), text)
        }
    }

    @Test
    fun testKotlinExceptionThrownInAction() {
        val input = "{\"version\":\"1\", \"data\":\"test\"}"
        val bufferSize = 1024

        memScoped {
            val inputPtr = input.cstr.getPointer(this)
            val outputBuffer = allocArray<ByteVar>(bufferSize)

            val resultSize = invokeBridge(inputPtr, outputBuffer = outputBuffer, bufferSize = bufferSize) {
                throw RuntimeException("Something went wrong")
            }

            assertTrue(resultSize > 0)
            val (status, payload) = response(outputBuffer, resultSize)
            assertEquals(STATUS_ERROR, status)
            assertEquals("Something went wrong", payload)
        }
    }
}
