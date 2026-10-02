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

import com.fasterxml.jackson.databind.ObjectMapper
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SchemaLocation
import com.networknt.schema.SchemaValidatorsConfig
import com.networknt.schema.SpecVersion
import com.networknt.schema.resource.ClasspathSchemaLoader
import com.networknt.schema.resource.MetaSchemaMapper
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * The spec-validation gate of ADR-0007: `ontology-spec.schema.json` (JSON
 * Schema draft 2020-12) must validate against its meta-schema, and every
 * example resource under `src/jvmTest/resources/ontology/` must validate
 * against the schema.
 */
class OntologySpecExamplesTest {

    private val mapper = ObjectMapper()
    private val config = SchemaValidatorsConfig.builder().formatAssertionsEnabled(true).build()
    private val factory =
        JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012) { builder ->
            // Resolve the draft 2020-12 meta-schema (and its meta/* parts) from
            // the validator's bundled classpath resources, never the network.
            builder
                .schemaMappers { it.add(MetaSchemaMapper()) }
                .schemaLoaders { it.add(ClasspathSchemaLoader()) }
        }

    // The schema lives at the repo root, not on the classpath; the jvmTest
    // working directory is the project directory.
    private val schemaFile = File("ontology-graph-spec.schema.json")

    private fun schema(): JsonSchema = factory.getSchema(mapper.readTree(schemaFile), config)

    @Test
    fun `schema validates against the draft 2020-12 meta-schema`() {
        val metaSchema =
            factory.getSchema(SchemaLocation.of(SpecVersion.VersionFlag.V202012.id), config)
        val errors = metaSchema.validate(mapper.readTree(schemaFile))
        assertTrue(
            errors.isEmpty(),
            "ontology-graph-spec.schema.json must be a valid draft 2020-12 schema:\n" +
                errors.joinToString("\n") { it.message }
        )
    }

    @TestFactory
    fun `ontology examples validate against the schema`(): List<DynamicTest> {
        val examples = ontologyExamples()
        val schema = schema()
        return examples.map { example ->
            dynamicTest(example.name) {
                val errors = schema.validate(mapper.readTree(example))
                assertTrue(
                    errors.isEmpty(),
                    "${example.name} must validate against ontology-graph-spec.schema.json:\n" +
                        errors.joinToString("\n") { it.message }
                )
            }
        }
    }

    @Test
    fun `document missing required fields fails validation`() {
        val invalid = mapper.readTree("""{ "name": "not an ontology" }""")
        val errors = schema().validate(invalid)
        assertFalse(
            errors.isEmpty(),
            "a document without \$schema, id and version must not validate"
        )
        val messages = errors.joinToString("\n") { it.message }
        assertTrue(messages.contains("\$schema"), "missing \$schema must be reported:\n$messages")
        assertTrue(messages.contains("id"), "missing id must be reported:\n$messages")
        assertTrue(messages.contains("version"), "missing version must be reported:\n$messages")
    }
}
