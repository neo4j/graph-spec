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
package migrate.migration.dataModel

import GraphSpec
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import model.GraphModel
import model.node.Labels
import model.node.Node
import model.property.Neo4jType
import model.property.Property
import kotlin.test.Test
import kotlin.test.assertEquals

class GraphSpecToDataModelIdUniquenessTest {

    @Test
    fun `encoding to a data model produces unique ids across entities`() {
        val model = GraphModel(
            version = "4.0.0",
            nodes = mutableMapOf(
                "n:0" to Node(
                    labels = Labels(identifier = "Patient"),
                    properties = mutableMapOf("id" to Property(type = Neo4jType.STRING))
                ),
                "n:1" to Node(
                    labels = Labels(identifier = "Doctor"),
                    properties = mutableMapOf("id" to Property(type = Neo4jType.STRING))
                )
            ),
            pretty = true
        )

        val encoded = GraphSpec.Json.encodeToString(model, "data_model_wrapped", "3.0.0")

        val ids = Json.parseToJsonElement(encoded).collectIds()
        assertEquals(ids.size, ids.toSet().size, "Data model ids must be globally unique across entities")
    }
}

private fun JsonElement.collectIds(): List<String> = when (this) {
    is JsonObject -> entries.flatMap { (key, value) ->
        (if (key == "\$id" && value is JsonPrimitive) listOf(value.content) else emptyList()) + value.collectIds()
    }
    is JsonArray -> flatMap { it.collectIds() }
    else -> emptyList()
}
