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
package schema

import java.io.File
import kotlinx.schema.generator.json.JsonSchemaConfig
import kotlinx.schema.generator.json.serialization.SerializationClassJsonSchemaGenerator
import kotlinx.schema.json.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import model.GraphModel
import model.property.Neo4jType

private const val DEFS = "\$defs"
private const val REF_PREFIX = "#/$DEFS/"

/**
 * Definitions are keyed by `@SerialName`, which for [Neo4jType] is the graph-spec type name, e.g. `"STRING"` or
 * `"LIST<STRING>"`. Those make poor type names in generated code and are not legal identifiers in most languages,
 * so use the Kotlin class name instead (e.g. `StringType`, `ListStringType`). Names of anything else remain the same.
 */
private fun definitionName(name: String): String = Neo4jType.of(name)?.let { it::class.simpleName } ?: name

private fun JsonObject.withRenamedDefinitions(): JsonObject =
    JsonObject(this + (DEFS to JsonObject(getValue(DEFS).jsonObject.mapKeys { definitionName(it.key) })))

private fun JsonElement.withRenamedReferences(): JsonElement =
    when (this) {
        is JsonObject -> JsonObject(mapValues { it.value.withRenamedReferences() })
        is JsonArray -> JsonArray(map { it.withRenamedReferences() })
        is JsonPrimitive ->
            if (content.startsWith(REF_PREFIX)) {
                JsonPrimitive(REF_PREFIX + definitionName(content.removePrefix(REF_PREFIX)))
            } else {
                this
            }
    }

fun main(args: Array<String>) {
    val out =
        File(args.firstOrNull()?.takeIf { it.isNotEmpty() } ?: "go/spec.json").absoluteFile
    out.parentFile?.mkdirs()
    val generator = SerializationClassJsonSchemaGenerator(jsonSchemaConfig = JsonSchemaConfig.OpenAPI)
    val schema = generator.generateSchema(GraphModel.serializer().descriptor)
    val json = Json { prettyPrint = true }
    val renamed = json.parseToJsonElement(schema.encodeToString(json)).jsonObject.withRenamedDefinitions()
    out.writeText(json.encodeToString(renamed.withRenamedReferences()))
}
