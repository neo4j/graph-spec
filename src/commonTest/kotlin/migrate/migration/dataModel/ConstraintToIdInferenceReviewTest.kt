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

import codec.schema.schemaListOf
import codec.schema.schemaMapOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Review test: a KEY constraint is a data-integrity rule (family 2), not an import ID (family 1).
 * Only mapping `key` may produce nodeKeyProperties/relationshipKeyProperties.
 *
 * Fails on the reviewed head: convertKeyProperties reads long-form KEY constraints
 * and property shorthand into the ID store. Goes green when those loops are removed
 * and only the mapping loop remains.
 */
class ConstraintToIdInferenceReviewTest {

    private val migration = GraphSpecDataModelV3Migration()

    @Test
    fun `KEY constraint does not manufacture a node ID`() {
        // ARRANGE - a node with a KEY constraint and no mapping, no shorthand
        val input = schemaMapOf(
            "nodes" to schemaMapOf(
                "customer" to schemaMapOf(
                    "properties" to schemaMapOf(
                        "customerId" to schemaMapOf("type" to "STRING"),
                        "email" to schemaMapOf("type" to "STRING")
                    ),
                    "constraints" to schemaMapOf(
                        "emailKey" to schemaMapOf(
                            "type" to "KEY",
                            "properties" to schemaListOf("email")
                        )
                    )
                )
            )
        )

        // ACT
        val result = migration.convertExtensions(input)

        // ASSERT - the constraint flows to graphSchema.constraints, never to the ID store
        assertEquals(
            emptyList(),
            result.listOfMapsOrNull("nodeKeyProperties"),
            "A KEY constraint is not an import ID - nodeKeyProperties must only come from mapping key"
        )
    }

    @Test
    fun `key shorthand does not manufacture a node ID`() {
        // ARRANGE - a node with key: true shorthand and no mapping
        val input = schemaMapOf(
            "nodes" to schemaMapOf(
                "customer" to schemaMapOf(
                    "properties" to schemaMapOf(
                        "email" to schemaMapOf("type" to "STRING", "key" to true)
                    )
                )
            )
        )

        // ACT
        val result = migration.convertExtensions(input)

        // ASSERT - shorthand is a constraint spelling, not an ID declaration
        assertEquals(
            emptyList(),
            result.listOfMapsOrNull("nodeKeyProperties"),
            "key: true shorthand is a constraint spelling, not an import ID - nodeKeyProperties must only come from mapping key"
        )
    }

    @Test
    fun `mapping key produces the node ID`() {
        // ARRANGE - control: the one correct source, a mapping with key
        val input = schemaMapOf(
            "mappings" to schemaListOf(
                schemaMapOf(
                    "node" to "customer",
                    "table" to "customers",
                    "key" to schemaListOf("customerId")
                )
            )
        )

        // ACT
        val result = migration.convertExtensions(input)

        // ASSERT - mapping key is the ID, this must keep working
        val nodeKeyProps = result.listOfMaps("nodeKeyProperties")
        assertEquals(1, nodeKeyProps.size)
        assertEquals("#customer", nodeKeyProps[0].map("node").string("\$ref"))
        val keys = nodeKeyProps[0].listOfMaps("keyProperties")
        assertEquals(1, keys.size)
        assertEquals("#customerId", keys[0].string("\$ref"))
    }

    @Test
    fun `constraint and mapping together do not duplicate or overwrite the ID`() {
        // ARRANGE - KEY constraint on email, but the declared ID is customerId via mapping
        val input = schemaMapOf(
            "nodes" to schemaMapOf(
                "customer" to schemaMapOf(
                    "properties" to schemaMapOf(
                        "customerId" to schemaMapOf("type" to "STRING"),
                        "email" to schemaMapOf("type" to "STRING")
                    ),
                    "constraints" to schemaMapOf(
                        "emailKey" to schemaMapOf(
                            "type" to "KEY",
                            "properties" to schemaListOf("email")
                        )
                    )
                )
            ),
            "mappings" to schemaListOf(
                schemaMapOf(
                    "node" to "customer",
                    "table" to "customers",
                    "key" to schemaListOf("customerId")
                )
            )
        )

        // ACT
        val result = migration.convertExtensions(input)

        // ASSERT - exactly one ID, the declared one; the constraint stays a constraint
        val nodeKeyProps = result.listOfMaps("nodeKeyProperties")
        assertEquals(1, nodeKeyProps.size, "one entity has exactly one ID entry")
        val keys = nodeKeyProps[0].listOfMaps("keyProperties")
        assertEquals(1, keys.size)
        assertEquals("#customerId", keys[0].string("\$ref"), "the mapping-declared ID wins, unconstrained by integrity rules")
    }
}
