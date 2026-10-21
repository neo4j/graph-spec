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
package org.neo4j.spec.graph.bridge

import kotlin.test.Test
import kotlin.test.assertTrue

class ValidationTest {

    @Test
    fun testValidate() {
        val input = """{
            "schema": "https://neo4j.com/docs/ontology-graph-spec/1.0.0/schema.json",
            "version": "1",
            "id": "test",
            "nodes": {
                "n": {
                    "constraints": {
                        "c1": {
                            "type": "EXISTS",
                            "properties": [],
                            "name": "constraint1"
                        }
                    }
                }
            }
        }
        """.trimIndent()

        val result = validateContent(input)
        listOf(
            "invalid_node_exist_constraint_property_count",
            "missing_node_identifier_label",
            "missing_node_properties",
            "missing_node_constraint_properties"
        ).forEach { code ->
            assertTrue(result.contains("\"code\":\"$code\""), "Expected issue $code in $result")
        }
    }
}
