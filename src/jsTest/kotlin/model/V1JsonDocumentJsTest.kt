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
package model

import codec.format.JsonFormat
import model.extension.BooleanValueJs
import model.extension.ExtensionType
import model.extension.ExtensionValueJs
import model.extension.ListValueJs
import model.extension.MapValueJs
import model.extension.StringValueJs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * A JS-side read of a hand-written v1 document: parsed with the repo's JsonFormat and
 * read through the JS twins, asserting the v1 shapes — id-keyed records, String type
 * tokens, endpoint fields, tools, the custom extension envelope and a named
 * neo4j:display extension.
 */
class V1JsonDocumentJsTest {

    private val document = """
        {
          "${'$'}schema": "https://neo4j.com/ontology-spec/1.0.0/schema.json",
          "id": "js-read-test",
          "version": 2,
          "name": "js-read",
          "nodes": {
            "person": {
              "label": "Person",
              "description": "A person.",
              "properties": {
                "name": { "type": "STRING", "mustExist": true },
                "tags": { "type": "LIST<STRING>" },
                "embedding": { "type": "VECTOR<FLOAT>", "dimension": 1536 }
              },
              "constraints": [
                { "constraint_type": "unique", "name": "person_name_unique", "properties": ["name"] }
              ],
              "tools": [
                {
                  "type": "canonicalQuery",
                  "name": "findCoActors",
                  "description": "Actors who shared a film with a given actor",
                  "cypher": "MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(co:Actor) RETURN co"
                }
              ],
              "extensions": {
                "neo4j:display": { "color": "#e06209", "caption": "name" },
                "custom": [
                  {
                    "type": "acme:lineage",
                    "name": "lineage",
                    "definition": { "owner": "data-platform", "pii": false }
                  }
                ]
              }
            },
            "movie": {
              "label": "Movie",
              "properties": {
                "title": { "type": "STRING" }
              }
            }
          },
          "relationships": {
            "acted_in": {
              "type": "ACTED_IN",
              "from": { "node": "person", "count": 1 },
              "to": { "node": "movie", "min_count": 1, "max_count": 5 },
              "properties": {
                "roles": { "type": "LIST<STRING>" }
              },
              "constraints": [
                { "constraint_type": "unique", "name": "acted_in_roles_unique", "properties": ["roles"] }
              ]
            }
          }
        }
    """.trimIndent()

    private fun parseToJs(): GraphModelJs {
        val model = JsonFormat.default.decodeModelFromString(document)
        return GraphModelEditor.plain(model)
    }

    private fun assertMap(value: ExtensionValueJs?, key: String): MapValueJs {
        assertEquals(ExtensionType.MAP, value?.type, "expected a Map extension value at '$key'")
        return value as MapValueJs
    }

    private fun assertString(value: ExtensionValueJs?, key: String): String {
        assertEquals(ExtensionType.STRING, value?.type, "expected a String extension value at '$key'")
        return (value as StringValueJs).value
    }

    @Test
    fun testIdKeyedRecords() {
        val js = parseToJs()

        assertEquals("https://neo4j.com/ontology-spec/1.0.0/schema.json", js.schema)
        assertEquals("js-read-test", js.id)
        assertEquals(2, js.version)
        assertEquals("js-read", js.name)

        // Nodes and relationships are keyed by local ids; each entry carries its id
        val person = js.nodes["person"]
        val movie = js.nodes["movie"]
        assertNotNull(person)
        assertNotNull(movie)
        assertEquals("person", person.id)
        assertEquals("movie", movie.id)
        assertEquals("Person", person.label)
        assertEquals("Movie", movie.label)

        val actedIn = js.relationships["acted_in"]
        assertNotNull(actedIn)
        assertEquals("acted_in", actedIn.id)
        assertEquals("ACTED_IN", actedIn.type)
    }

    @Test
    fun testPropertyTypeTokens() {
        val person = parseToJs().nodes["person"]!!

        val name = person.properties["name"]
        val tags = person.properties["tags"]
        val embedding = person.properties["embedding"]
        assertNotNull(name)
        assertNotNull(tags)
        assertNotNull(embedding)
        assertEquals("name", name.id)
        assertEquals("STRING", name.type)
        assertEquals(true, name.mustExist)
        assertEquals("LIST<STRING>", tags.type)
        assertEquals("VECTOR<FLOAT>", embedding.type)
        assertEquals(1536, embedding.dimension)
    }

    @Test
    fun testEndpointFields() {
        val actedIn = parseToJs().relationships["acted_in"]!!

        // Endpoint node references point at node ids; count/min_count/max_count
        assertEquals("person", actedIn.from.node)
        assertEquals(1, actedIn.from.count)
        assertEquals("movie", actedIn.to.node)
        assertEquals(1, actedIn.to.minCount)
        assertEquals(5, actedIn.to.maxCount)

        val roles = actedIn.properties["roles"]
        assertNotNull(roles)
        assertEquals("LIST<STRING>", roles.type)

        // constraint_type on the wire reads back as `type` on the ConstraintJs twin
        assertEquals(1, actedIn.constraints.size)
        assertEquals("unique", actedIn.constraints[0].type)
        assertEquals("acted_in_roles_unique", actedIn.constraints[0].name)
        assertTrue(actedIn.constraints[0].properties.contains("roles"))
    }

    @Test
    fun testTools() {
        val person = parseToJs().nodes["person"]!!

        assertEquals(1, person.tools.size)
        val tool = person.tools[0]
        assertEquals("canonicalQuery", tool.type)
        assertEquals("findCoActors", tool.name)
        assertEquals("Actors who shared a film with a given actor", tool.description)
        assertEquals(
            "MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(co:Actor) RETURN co",
            assertString(tool.extra["cypher"], "tools[0].cypher"),
        )

        // The wire `constraint_type` reads back through the node twin as well
        assertEquals(1, person.constraints.size)
        assertEquals("unique", person.constraints[0].type)
        assertEquals("person_name_unique", person.constraints[0].name)
        assertTrue(person.constraints[0].properties.contains("name"))
    }

    @Test
    fun testExtensionsEnvelope() {
        val person = parseToJs().nodes["person"]!!

        // Named first-party extension readable via the extensions record
        val display = assertMap(person.extensions["neo4j:display"], "neo4j:display")
        assertEquals("#e06209", assertString(display.value["color"], "neo4j:display.color"))
        assertEquals("name", assertString(display.value["caption"], "neo4j:display.caption"))

        // The custom envelope: reserved `custom` key holds a list of envelopes, the
        // free-form definition payload is carried untouched
        val custom = person.extensions["custom"]
        assertEquals(ExtensionType.LIST, custom?.type)
        val envelopes = (custom as ListValueJs).value
        assertEquals(1, envelopes.size)
        val envelope = assertMap(envelopes[0], "custom[0]")
        assertEquals("acme:lineage", assertString(envelope.value["type"], "custom[0].type"))
        assertEquals("lineage", assertString(envelope.value["name"], "custom[0].name"))
        val definition = assertMap(envelope.value["definition"], "custom[0].definition")
        assertEquals("data-platform", assertString(definition.value["owner"], "custom[0].definition.owner"))
        val pii = definition.value["pii"]
        assertEquals(ExtensionType.BOOLEAN, pii?.type)
        assertEquals(false, (pii as BooleanValueJs).value)
    }
}
