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
package migrate

import OntologyGraphSpec
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SpecVersion
import com.networknt.schema.resource.ClasspathSchemaLoader
import com.networknt.schema.resource.MetaSchemaMapper
import model.Type
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** Classloader anchor for resolving the `/migrate/migration/dataModel` resources. */
private object CorpusAnchor

/**
 * The ADR-0008 corpus gate: every 4.0.0 fixture under
 * `src/jvmTest/resources/migrate/migration/dataModel/` (the prod-like YAML files in
 * pretty form, the internal YAML files in internal form) converts through
 * [OntologyGraphSpec] and the output validates against `ontology-graph-spec.schema.json` (the
 * ADR-0007 gate machinery). Each pretty/internal pair of the same model must converge
 * to the same v1 document modulo the generated `id`. The 3.0.0 fixtures (the prod-like
 * JSON files, `graph-data-model-3.0.0.json`) are legacy-chain inputs, not converter
 * inputs, and are rejected as unsupported (ADR-0008 §10).
 */
class GraphSpecV4CorpusTest {

    private val mapper = ObjectMapper()
    private val config = SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build()
    private val factory =
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012) { builder ->
            // Same offline setup as the spec gate (spec/OntologyGraphSpecExamplesTest.kt).
            builder
                .schemaMappers { it.add(MetaSchemaMapper()) }
                .schemaLoaders { it.add(ClasspathSchemaLoader()) }
        }

    // The schema lives at the repo root; the jvmTest working directory is the project dir.
    private val schema: JsonSchema = factory.getSchema(mapper.readTree(File("ontology-graph-spec.schema.json")), config)

    private val corpusDir = File(CorpusAnchor.javaClass.getResource("/migrate/migration/dataModel")!!.path)

    private fun fixtures(subdir: String, extension: String): List<File> =
        File(corpusDir, subdir)
            .listFiles { file -> file.isFile && file.extension == extension }
            ?.sortedBy { it.name }
            .orEmpty()

    private fun convert(yaml: String): String =
        OntologyGraphSpec.Json.encodeToString(OntologyGraphSpec.Yaml.decodeFromString(yaml, Type.GRAPH_SPEC))

    @TestFactory
    fun `every 4-0-0 fixture converts and validates against the v1 schema`(): List<DynamicTest> {
        val fixtures = fixtures("prod-like", "yaml") + fixtures("internal", "yaml")
        assertTrue(fixtures.isNotEmpty(), "no 4.0.0 fixtures found in ${corpusDir.path}")
        return fixtures.map { fixture ->
            dynamicTest("${fixture.parentFile.name}/${fixture.name}") {
                val output = convert(fixture.readText())
                val errors = schema.validate(mapper.readTree(output))
                assertTrue(
                    errors.isEmpty(),
                    "converted ${fixture.name} must validate against ontology-graph-spec.schema.json:\n" +
                        errors.joinToString("\n") { it.message } + "\noutput:\n" + output,
                )
            }
        }
    }

    @TestFactory
    fun `pretty and internal forms of the same model converge modulo the generated id`(): List<DynamicTest> {
        val pretty = fixtures("prod-like", "yaml")
        assertTrue(pretty.isNotEmpty(), "no pretty fixtures found")
        return pretty.map { fixture ->
            dynamicTest(fixture.nameWithoutExtension) {
                val internal = File(corpusDir, "internal/${fixture.name}")
                assertTrue(internal.isFile, "missing internal twin for ${fixture.name}")

                val prettyTree = mapper.readTree(convert(fixture.readText())) as ObjectNode
                val internalTree = mapper.readTree(convert(internal.readText())) as ObjectNode

                val prettyId = prettyTree.remove("id").asText()
                val internalId = internalTree.remove("id").asText()
                assertNotEquals(prettyId, internalId, "each conversion mints a fresh id (ADR-0008 §1)")
                assertEquals(
                    prettyTree,
                    internalTree,
                    "pretty and internal forms of ${fixture.name} must convert to the same v1 document",
                )
            }
        }
    }

    @TestFactory
    fun `pre-4-0-0 fixtures are reported as unsupported`(): List<DynamicTest> {
        val legacy = fixtures("prod-like", "json") + File(corpusDir, "graph-data-model-3.0.0.json")
        assertTrue(legacy.isNotEmpty(), "no legacy fixtures found")
        return legacy.map { fixture ->
            dynamicTest(fixture.name) {
                val exception = assertFailsWith<IllegalStateException> {
                    OntologyGraphSpec.Json.decodeFromString(fixture.readText(), Type.GRAPH_SPEC)
                }
                assertTrue(
                    exception.message!!.contains("Unsupported migration from graph_spec:3.0"),
                    "3.0.0 documents are rejected as unsupported (ADR-0008 §10), got: ${exception.message}",
                )
            }
        }
    }
}
