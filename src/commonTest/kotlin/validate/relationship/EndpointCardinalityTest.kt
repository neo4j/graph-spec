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
package validate.relationship

import model.GraphModel
import model.node.Node
import model.relationship.Relationship
import model.relationship.RelationshipTarget
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class EndpointCardinalityTest {

    private val validator = EndpointCardinality

    private fun model(to: RelationshipTarget) = GraphModel(
        schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
        id = "test",
        version = 1,
        nodes = mutableMapOf("a" to Node(label = "A"), "b" to Node(label = "B")),
        relationships = mutableMapOf(
            "REL" to Relationship(type = "REL", from = RelationshipTarget(node = "a"), to = to),
        ),
    )

    private fun validateToEndpoint(to: RelationshipTarget): List<Issue> {
        val issues = mutableListOf<Issue>()
        validator.validate(model(to), issues)
        return issues
    }

    @Test
    fun `pass when the endpoint is unconstrained`() {
        assertTrue(validateToEndpoint(RelationshipTarget(node = "b")).isEmpty())
    }

    @Test
    fun `pass for an exact count including zero`() {
        assertTrue(validateToEndpoint(RelationshipTarget(node = "b", count = 0)).isEmpty())
        assertTrue(validateToEndpoint(RelationshipTarget(node = "b", count = 2)).isEmpty())
    }

    @Test
    fun `pass for a sound range`() {
        assertTrue(validateToEndpoint(RelationshipTarget(node = "b", minCount = 0, maxCount = 5)).isEmpty())
        assertTrue(validateToEndpoint(RelationshipTarget(node = "b", minCount = 5, maxCount = 5)).isEmpty())
        assertTrue(validateToEndpoint(RelationshipTarget(node = "b", minCount = 1)).isEmpty())
        assertTrue(validateToEndpoint(RelationshipTarget(node = "b", maxCount = 1)).isEmpty())
    }

    @Test
    fun `fail for a negative count`() {
        val issues = validateToEndpoint(RelationshipTarget(node = "b", count = -1))

        assertEquals(1, issues.size)
        assertEquals("negative_endpoint_count", issues.first().code)
        assertEquals("relationships.REL.to.count", issues.first().path)
    }

    @Test
    fun `fail for a negative min_count`() {
        val issues = validateToEndpoint(RelationshipTarget(node = "b", minCount = -1))

        assertEquals(1, issues.size)
        assertEquals("negative_endpoint_min_count", issues.first().code)
        assertEquals("relationships.REL.to.min_count", issues.first().path)
    }

    @Test
    fun `fail for a max_count below 1`() {
        val issues = validateToEndpoint(RelationshipTarget(node = "b", maxCount = 0))

        assertEquals(1, issues.size)
        assertEquals("invalid_endpoint_max_count", issues.first().code)
        assertEquals("relationships.REL.to.max_count", issues.first().path)
    }

    @Test
    fun `fail when min_count exceeds max_count`() {
        val issues = validateToEndpoint(RelationshipTarget(node = "b", minCount = 5, maxCount = 1))

        assertEquals(1, issues.size)
        assertEquals("endpoint_min_count_above_max_count", issues.first().code)
    }

    @Test
    fun `fail when count is combined with the ranged form`() {
        val withMax = validateToEndpoint(RelationshipTarget(node = "b", count = 1, maxCount = 2))
        val withMin = validateToEndpoint(RelationshipTarget(node = "b", count = 1, minCount = 0))

        assertEquals(1, withMax.size)
        assertEquals("endpoint_count_combined_with_range", withMax.first().code)
        assertEquals(1, withMin.size)
        assertEquals("endpoint_count_combined_with_range", withMin.first().code)
    }

    @Test
    fun `from endpoints are checked too`() {
        val model = GraphModel(
            schema = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json",
            id = "test",
            version = 1,
            nodes = mutableMapOf("a" to Node(label = "A"), "b" to Node(label = "B")),
            relationships = mutableMapOf(
                "REL" to Relationship(
                    type = "REL",
                    from = RelationshipTarget(node = "a", count = -2),
                    to = RelationshipTarget(node = "b"),
                ),
            ),
        )
        val issues = mutableListOf<Issue>()

        validator.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("negative_endpoint_count", issues.first().code)
        assertEquals("relationships.REL.from.count", issues.first().path)
    }
}
