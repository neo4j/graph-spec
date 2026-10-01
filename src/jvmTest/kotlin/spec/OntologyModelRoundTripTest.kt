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
package spec

import codec.format.JsonFormat
import com.fasterxml.jackson.databind.ObjectMapper
import model.extension.getCustom
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * The model-conformance half of ADR-0003/ADR-0007: [OntologySpecExamplesTest] proves
 * the examples validate against the hand-maintained schema; this test proves the v1
 * Kotlin model ([model.GraphModel] via [JsonFormat]) round-trips those same examples
 * losslessly — deserialize, re-serialize, JSON tree equality — including the open
 * surfaces (tool extras, extension payloads) and the "absent means none" rule.
 */
class OntologyModelRoundTripTest {

    private val mapper = ObjectMapper()
    private val format = JsonFormat.default

    // Same classpath-resource pattern as OntologySpecExamplesTest.
    private val examplesDir = File(javaClass.getResource("/ontology")!!.path)

    private fun examples(): List<File> =
        examplesDir
            .listFiles { file -> file.isFile && file.extension == "json" }
            ?.sortedBy { it.name }
            .orEmpty()

    private fun roundTrip(input: String): String {
        val model = format.decodeModelFromString(input)
        return format.encodeModelToString(model)
    }

    @TestFactory
    fun `ontology examples round-trip through the v1 model`(): List<DynamicTest> {
        val examples = examples()
        assertTrue(examples.isNotEmpty(), "no ontology examples found in ${examplesDir.path}")
        return examples.map { example ->
            dynamicTest(example.name) {
                val input = example.readText()
                val output = roundTrip(input)
                val inputTree = mapper.readTree(input)
                val outputTree = mapper.readTree(output)
                assertEquals(
                    inputTree,
                    outputTree,
                    "${example.name} must survive the model round-trip unchanged " +
                        "(semantic JSON equality; covers 'absent means none' both ways)"
                )
            }
        }
    }

    @Test
    fun `absent means none - omitted optional fields are not invented`() {
        val minimal = """
            {
              "${'$'}schema": "https://neo4j.com/ontology-spec/1.0.0/schema.json",
              "id": "minimal",
              "version": 1,
              "nodes": {
                "Solo": { "label": "Solo" }
              }
            }
        """.trimIndent()

        val outputTree = mapper.readTree(roundTrip(minimal))
        assertEquals(mapper.readTree(minimal), outputTree)

        // The root must not grow empty relationships/extensions or null metadata.
        assertFalse(outputTree.has("name"), "no name invented: $outputTree")
        assertFalse(outputTree.has("description"), "no description invented: $outputTree")
        assertFalse(outputTree.has("relationships"), "no empty relationships map: $outputTree")
        assertFalse(outputTree.has("extensions"), "no empty extensions map: $outputTree")

        // The node must not grow empty properties/constraints/tools/aliases/extensions.
        val solo = outputTree.at("/nodes/Solo")
        assertEquals(setOf("label"), solo.fieldNames().asSequence().toSet())
    }

    @Test
    fun `movies tools keep their owner-defined extras`() {
        val input = File(examplesDir, "movies.ontology.json").readText()
        val model = format.decodeModelFromString(input)

        val actedIn = model.relationships.getValue("ACTED_IN")
        assertEquals(2, actedIn.tools.size)
        val externalRequest = actedIn.tools[0]
        assertEquals("externalRequest", externalRequest.type)
        assertEquals("https://imdb.com/", externalRequest.extra["url"]?.asString)
        val canonicalQuery = actedIn.tools[1]
        assertEquals("canonicalQuery", canonicalQuery.type)
        assertTrue(
            canonicalQuery.extra["cypher"]?.asString?.startsWith("MATCH (a:Actor)") == true,
            "canonicalQuery tool must keep its cypher extra"
        )

        // The extras are inlined on the wire again after re-serialization.
        val outputTree = mapper.readTree(format.encodeModelToString(model))
        assertEquals(
            "https://imdb.com/",
            outputTree.at("/relationships/ACTED_IN/tools/0/url").asText()
        )
        assertTrue(
            outputTree.at("/relationships/ACTED_IN/tools/1/cypher").asText()
                .startsWith("MATCH (a:Actor)")
        )
    }

    @Test
    fun `extension payloads carry unknown fields untouched`() {
        val movies = format.decodeModelFromString(File(examplesDir, "movies.ontology.json").readText())
        val org = format.decodeModelFromString(File(examplesDir, "org.ontology.json").readText())
        val foaf = format.decodeModelFromString(File(examplesDir, "foaf.ontology.json").readText())

        // Named extension payloads are raw JSON trees (movies: neo4j:display, neo4j-importer:*).
        val display = movies.nodes.getValue("Actor").extensions["neo4j:display"]?.asMap
        assertNotNull(display)
        assertEquals("#e06209", display["color"]?.asString)
        val mapping = movies.extensions["neo4j-importer:mapping"]?.asMap
        assertNotNull(mapping)
        assertEquals("node", mapping["kind"]?.asString)
        assertEquals("actor_id", mapping["key"]?.asList?.single()?.asString)

        // Custom extension envelope at the document root (org: governance:retention).
        val retention = org.getCustom().single()
        assertEquals("governance:retention", retention.type)
        assertEquals(
            "https://example.com/ontology-extensions/governance-retention/0.1.0/schema.json",
            retention.schema
        )
        assertEquals(365L, retention.definition?.asMap?.get("review_after_days")?.asLong)

        // Custom extension envelopes on nodes (foaf: rdfs:annotations with free-form definition).
        val annotations = foaf.nodes.getValue("Person").getCustom().single()
        assertEquals("rdfs:annotations", annotations.type)
        val definition = annotations.definition?.asMap
        assertNotNull(definition)
        assertEquals("Person", definition["label"]?.asString)
        assertEquals("stable", definition["term_status"]?.asString)
        assertEquals("http://xmlns.com/foaf/0.1/", definition["isDefinedBy"]?.asString)

        // ... and they survive re-serialization.
        val orgTree = mapper.readTree(format.encodeModelToString(org))
        assertEquals(
            365,
            orgTree.at("/extensions/custom/0/definition/review_after_days").asInt()
        )
        val foafTree = mapper.readTree(format.encodeModelToString(foaf))
        assertEquals(
            "stable",
            foafTree.at("/nodes/Person/extensions/custom/0/definition/term_status").asText()
        )
    }
}
