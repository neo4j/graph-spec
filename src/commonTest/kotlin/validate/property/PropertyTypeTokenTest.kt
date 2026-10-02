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
package validate.property

import model.GraphModel
import model.node.Node
import model.property.Property
import model.relationship.Relationship
import model.relationship.RelationshipTarget
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PropertyTypeTokenTest {

    private val validator = PropertyTypeToken

    private fun modelWithNodeProperty(property: Property) = GraphModel(
        schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
        id = "test",
        version = 1,
        nodes = mutableMapOf("n" to Node(label = "N", properties = mutableMapOf("p" to property))),
    )

    private fun validateNodeProperty(property: Property): List<Issue> {
        val issues = mutableListOf<Issue>()
        validator.validate(modelWithNodeProperty(property), issues)
        return issues
    }

    @Test
    fun `pass for every schema token family`() {
        val validTokens = listOf(
            "ANY",
            "LIST<ANY>",
            "STRING",
            "INTEGER",
            "FLOAT",
            "BOOLEAN",
            "DATE",
            "TIME",
            "LOCALTIME",
            "DATETIME",
            "LOCALDATETIME",
            "DURATION",
            "POINT",
            "BYTES",
            "LIST<STRING>",
            "LIST<FLOAT>",
            "VECTOR<FLOAT>",
            "VECTOR<INTEGER>",
        )
        for (token in validTokens) {
            assertTrue(
                validateNodeProperty(Property(type = token)).isEmpty(),
                "Expected no issues for type token '$token'",
            )
        }
    }

    @Test
    fun `pass when the property has no type`() {
        assertTrue(validateNodeProperty(Property()).isEmpty())
    }

    @Test
    fun `pass for a vector with a dimension companion`() {
        assertTrue(validateNodeProperty(Property(type = "VECTOR<FLOAT>", dimension = 512)).isEmpty())
    }

    @Test
    fun `fail for tokens outside the schema grammar`() {
        val invalidTokens = listOf(
            "string",
            "LIST<STRING",
            "LIST<LIST<STRING>>",
            "LIST<VECTOR<FLOAT>>",
            "VECTOR<LIST<FLOAT>>",
            "VECTOR<ANY>",
            "STRING|INTEGER",
            "MAP",
            "",
        )
        for (token in invalidTokens) {
            val issues = validateNodeProperty(Property(type = token))

            assertEquals(1, issues.size, "Expected exactly one issue for type token '$token'")
            assertEquals("invalid_property_type", issues.first().code)
            assertEquals("nodes.n.properties.p.type", issues.first().path)
        }
    }

    @Test
    fun `fail when dimension rides a non-vector type`() {
        val issues = validateNodeProperty(Property(type = "STRING", dimension = 3))

        assertEquals(1, issues.size)
        assertEquals("dimension_on_non_vector_property", issues.first().code)
        assertEquals("nodes.n.properties.p.dimension", issues.first().path)
    }

    @Test
    fun `fail when dimension is present without a type`() {
        val issues = validateNodeProperty(Property(dimension = 3))

        assertEquals(1, issues.size)
        assertEquals("dimension_on_non_vector_property", issues.first().code)
    }

    @Test
    fun `fail when dimension is below 1`() {
        val issues = validateNodeProperty(Property(type = "VECTOR<FLOAT>", dimension = 0))

        assertEquals(1, issues.size)
        assertEquals("invalid_property_dimension", issues.first().code)
        assertEquals("nodes.n.properties.p.dimension", issues.first().path)
    }

    @Test
    fun `relationship properties are checked too`() {
        val model = GraphModel(
            schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
            id = "test",
            version = 1,
            nodes = mutableMapOf("a" to Node(label = "A"), "b" to Node(label = "B")),
            relationships = mutableMapOf(
                "REL" to Relationship(
                    type = "REL",
                    from = RelationshipTarget(node = "a"),
                    to = RelationshipTarget(node = "b"),
                    properties = mutableMapOf("p" to Property(type = "NOPE")),
                ),
            ),
        )
        val issues = mutableListOf<Issue>()

        validator.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("invalid_property_type", issues.first().code)
        assertEquals("relationships.REL.properties.p.type", issues.first().path)
    }
}
