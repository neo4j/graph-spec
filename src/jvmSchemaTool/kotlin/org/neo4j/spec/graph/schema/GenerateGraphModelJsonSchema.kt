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
package org.neo4j.spec.graph.schema

import java.io.File
import kotlinx.schema.generator.json.JsonSchemaConfig
import kotlinx.schema.generator.json.serialization.SerializationClassJsonSchemaGenerator
import kotlinx.schema.generator.json.serialization.SerializationClassSchemaIntrospector
import kotlinx.schema.json.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import org.neo4j.spec.graph.model.GraphModel
import org.neo4j.spec.graph.model.Version

/**
 * Output is plain JSON Schema draft 2020-12: nullability is expressed with `"type": [..., "null"]` rather than
 * OpenAPI's `nullable`, and polymorphic subtypes are told apart by their `type` const rather than `discriminator`.
 * Definitions keep their short serial names and are namespaced by the absolute schema url in `$id`.
 */
fun main(args: Array<String>) {
    val out =
        File(args.firstOrNull()?.takeIf { it.isNotEmpty() } ?: "ontology-graph-spec.schema.json").absoluteFile
    out.parentFile?.mkdirs()
    val json = Json { prettyPrint = true }
    val generator =
        SerializationClassJsonSchemaGenerator(
            introspectorConfig =
                SerializationClassSchemaIntrospector.Config(
                    descriptionExtractor = { annotations -> annotations.filterIsInstance<SerialDescription>().firstOrNull()?.value }
                ),
            jsonSchemaConfig = JsonSchemaConfig.Default
        )
    val schema = json.parseToJsonElement(generator.generateSchema(GraphModel.serializer().descriptor).encodeToString(json)).jsonObject
    out.writeText(json.encodeToString(JsonElement.serializer(), withId(schema, Version.schemaUrl(Version.LATEST))))
}

/**
 * Replaces the generated `$id` (the serial name) with the absolute schema url, keeping the name as the `title`.
 */
private fun withId(schema: JsonObject, id: String): JsonObject = buildJsonObject {
    schema.forEach { (key, value) ->
        if (key == "\$id") {
            put(key, JsonPrimitive(id))
            put("title", value)
        } else {
            put(key, value)
        }
    }
}
