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
import codec.format.YamlFormat
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals

/**
 * The YAML half of ADR-0007(d): with the v1 model landed, the YAML example forms return
 * via the Kotlin YAML codec — proven as a round-trip assertion, not as committed .yaml
 * files. Every JSON example goes JSON -> model ([JsonFormat]) -> YAML string
 * ([YamlFormat]) -> model ([YamlFormat]) -> JSON string ([JsonFormat]); the YAML-decoded
 * model must equal the JSON-loaded one (data-class equality) and the final JSON must be
 * Jackson tree-equal to the original. If [YamlFormat] drops or invents a field, the tree
 * equality fails.
 */
class OntologyGraphYamlRoundTripTest {

    private val mapper = ObjectMapper()
    private val json = JsonFormat.default
    private val yaml = YamlFormat.default

    @TestFactory
    fun `ontology examples round-trip through the YAML codec`(): List<DynamicTest> {
        return ontologyExamples().map { example ->
            dynamicTest(example.name) {
                val input = example.readText()

                val jsonModel = json.decodeModelFromString(input)
                val yamlString = yaml.encodeModelToString(jsonModel)
                val yamlModel = yaml.decodeModelFromString(yamlString)

                assertEquals(
                    jsonModel,
                    yamlModel,
                    "${example.name}: the YAML-decoded model must equal the JSON-loaded model\n$yamlString"
                )

                val output = json.encodeModelToString(yamlModel)
                assertEquals(
                    mapper.readTree(input),
                    mapper.readTree(output),
                    "${example.name} must survive the YAML codec round-trip unchanged " +
                        "(semantic JSON equality; covers 'absent means none' both ways)\n$yamlString"
                )
            }
        }
    }
}
