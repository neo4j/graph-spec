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
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import validate.Validations
import kotlin.test.assertTrue

/**
 * The validator-conformance half of ADR-0003/ADR-0007: [OntologyGraphSpecExamplesTest] proves
 * the examples validate against the hand-maintained schema; [OntologyGraphModelRoundTripTest]
 * proves they round-trip the v1 model; this test proves they pass the v1 validator
 * suite ([Validations.all] — the rules the schema cannot express) with zero issues.
 * A schema-valid example that trips the model-level validators means schema/validator
 * disagreement, and per ADR-0003 the schema wins.
 */
class OntologyGraphSpecValidationTest {

    private val format = JsonFormat.default

    @TestFactory
    fun `ontology examples validate with zero issues through Validations all`(): List<DynamicTest> {
        return ontologyExamples().map { example ->
            dynamicTest(example.name) {
                val model = format.decodeModelFromString(example.readText())
                val issues = model.validate(Validations.all)
                assertTrue(
                    issues.isEmpty(),
                    "${example.name} must validate with zero issues through Validations.all, got: $issues"
                )
            }
        }
    }
}
