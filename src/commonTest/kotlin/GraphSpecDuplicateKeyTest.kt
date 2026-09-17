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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Neither kotlinx.serialization-json nor yamlkt reject duplicate map keys - both silently
 * decode a `Map<String, T>` by overwriting the earlier entry with the later one, with no
 * exception and no merge of fields. This documents that behavior for pretty-format
 * `nodes`/`relationships` maps: the first node is fully discarded, not merged with the second.
 */
class GraphSpecDuplicateKeyTest {

    @Test
    fun `two nodes with the same key in pretty json - the last one silently wins`() {
        val json = """
            {
              "version": "4.0.0",
              "nodes": {
                "album": { "labels": { "identifier": "First" } },
                "album": { "labels": { "identifier": "Second" } }
              }
            }
        """.trimIndent()

        val model = GraphSpec.Json.decodeFromString(json)

        assertEquals(1, model.nodes.size)
        assertEquals("Second", model.nodes.getValue("node0").labels.identifier)
    }

    @Test
    fun `two nodes with the same key in pretty yaml - the last one silently wins`() {
        val yaml = """
            version: "4.0.0"
            nodes:
              album:
                labels:
                  identifier: "First"
              album:
                labels:
                  identifier: "Second"
        """.trimIndent()

        val model = GraphSpec.Yaml.decodeFromString(yaml)

        assertEquals(1, model.nodes.size)
        assertEquals("Second", model.nodes.getValue("node0").labels.identifier)
    }
}
