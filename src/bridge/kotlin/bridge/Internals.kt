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
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.plus
import kotlinx.cinterop.set
import kotlinx.cinterop.toKString
import kotlinx.cinterop.usePinned
import platform.posix.getenv
import platform.posix.memcpy
import kotlin.native.runtime.GC
import kotlin.native.runtime.NativeRuntimeApi

// Status bytes written to output buffer indicating success or failure
const val STATUS_OK: Byte = 0
const val STATUS_ERROR: Byte = 1

// Error response codes returned if output buffer cannot be written to
const val INVALID_INPUTS = -1
const val INTERNAL_ERROR = Int.MIN_VALUE

// Optional env var, set by callers of the library, to cap the Kotlin/Native heap
const val MAX_HEAP_ENV = "GRAPHSPEC_MAX_HEAP_BYTES"

@OptIn(ExperimentalForeignApi::class, NativeRuntimeApi::class, ExperimentalStdlibApi::class)
private val runtimeConfigured: Boolean by lazy {
    getenv(MAX_HEAP_ENV)?.toKString()?.toLongOrNull()?.let { ceiling ->
        GC.maxHeapBytes = ceiling
        // pauseOnTargetHeapOverflow is explicitly left off. A ceiling below what a model genuinely needs then surfaces
        // as an error the caller can report, rather than thread stalling waiting on the collector to free memory.
        GC.pauseOnTargetHeapOverflow = false
    }
    true
}

@OptIn(ExperimentalForeignApi::class)
class BridgeInput internal constructor(private val arguments: Array<out CPointer<ByteVar>?>) {

    val size: Int get() = arguments.size

    operator fun get(index: Int): String = arguments[index]!!.toKString()
}

/*
Handles the Kotlin/Native bridge operations to safely invoke Kotlin methods from C.
Converts the input to a Kotlin string, invokes the passed [action] and writes the
output to the provided output buffer in a memory safe way.
 */
@OptIn(ExperimentalForeignApi::class)
fun invokeBridge(
    vararg input: CPointer<ByteVar>?,
    outputBuffer: CPointer<ByteVar>?,
    bufferSize: Int,
    action: (BridgeInput) -> String
): Int {
    if (input.any { it == null } || outputBuffer == null || bufferSize < 1) return INVALID_INPUTS

    var status = STATUS_OK
    val bytes = try {
        check(runtimeConfigured)
        action(BridgeInput(input)).encodeToByteArray()
    } catch (failure: Throwable) {
        status = STATUS_ERROR
        try {
            (failure.message ?: "Unknown Kotlin error").encodeToByteArray()
        } catch (_: Throwable) {
            return INTERNAL_ERROR
        }
    }

    val required = bytes.size + 1
    // Check that the output buffer is large enough before writing. We return the negative required size
    // if buffer insufficient to allow caller to adjust.
    if (bufferSize < required) return -required

    outputBuffer[0] = status
    if (bytes.isNotEmpty()) {
        bytes.usePinned { memcpy(outputBuffer + 1, it.addressOf(0), bytes.size.toULong()) }
    }
    return required
}
