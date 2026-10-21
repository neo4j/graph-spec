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
package org.neo4j.spec.graph.bridge

import kotlinx.cinterop.ByteVar
import kotlinx.cinterop.CPointer
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.serialization.json.Json
import org.neo4j.spec.graph.GraphSpec
import org.neo4j.spec.graph.codec.format.JsonFormat
import org.neo4j.spec.graph.codec.schema.SchemaMap
import org.neo4j.spec.graph.migrate.MigrationPath
import org.neo4j.spec.graph.model.Type
import kotlin.experimental.ExperimentalNativeApi

private val jsonFormat = JsonFormat(
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = false
    }
)

private val graphSpec = GraphSpec { format(jsonFormat) }

@OptIn(ExperimentalForeignApi::class, ExperimentalNativeApi::class)
@CName("migrate")
fun migrate(
    inputJson: CPointer<ByteVar>?,
    inputType: CPointer<ByteVar>?,
    targetType: CPointer<ByteVar>?,
    targetVersion: CPointer<ByteVar>?,
    outputBuffer: CPointer<ByteVar>?,
    bufferSize: Int
) = invokeBridge(
    inputJson,
    inputType,
    targetType,
    targetVersion,
    outputBuffer = outputBuffer,
    bufferSize = bufferSize
) { input -> migrateContent(input[0], type = input[1], targetType = input[2], targetVersion = input[3]) }

internal fun migrateContent(content: String, type: String, targetType: String, targetVersion: String): String {
    if (type == Type.GRAPH_SPEC && targetType != Type.GRAPH_SPEC) {
        // Migrations expect longhand, so go via the model to expand any shorthand.
        val model = graphSpec.decodeFromString(content)
        return graphSpec.encodeToString(model, targetType = targetType, targetVersion = targetVersion)
    }
    val path = MigrationPath(GraphSpec.Json.configuration.migrations)
    val schema = jsonFormat.decodeFromString(content)
    var map = schema as? SchemaMap ?: error("Schema format expected")
    map = path.migrate(map, type = type, targetType = targetType, targetVersion = targetVersion)
    return jsonFormat.encodeToString(map)
}
