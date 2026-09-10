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
import model.property.Neo4jType
import model.property.Property
import model.relationship.Relationship
import model.relationship.RelationshipTarget
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationshipPropertyVectorDimensionBoundsTest {

    private val validator = RelationshipPropertyVectorDimensionBounds
    private val model = GraphModel("4.0.0")

    @Test
    fun `fail when relationship vector property dimension is below min`() {
        val relationship = Relationship(
            type = "ACTED_IN",
            from = RelationshipTarget(),
            to = RelationshipTarget(),
            properties = mutableMapOf(
                "embedding" to Property(type = Neo4jType.VECTOR_FLOAT, dimension = 0)
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateProperty(
            model,
            "actedIn",
            relationship,
            "embedding",
            relationship.properties["embedding"]!!,
            issues
        )

        assertEquals(1, issues.size)
        val issue = issues.first()
        assertEquals("invalid_vector_dimension", issue.code)
        assertEquals(
            "Vector property 'embedding' on relationship 'actedIn' has dimension 0 outside 1-4096",
            issue.message
        )
        assertEquals("relationships.actedIn.properties.embedding.dimension", issue.path)
    }

    @Test
    fun `fail when relationship vector property dimension exceeds max`() {
        val relationship = Relationship(
            type = "ACTED_IN",
            from = RelationshipTarget(),
            to = RelationshipTarget(),
            properties = mutableMapOf(
                "embedding" to Property(type = Neo4jType.VECTOR_FLOAT, dimension = 4097)
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateProperty(
            model,
            "actedIn",
            relationship,
            "embedding",
            relationship.properties["embedding"]!!,
            issues
        )

        assertEquals(1, issues.size)
        assertEquals("invalid_vector_dimension", issues.first().code)
    }

    @Test
    fun `pass when relationship vector property dimension is within bounds`() {
        val relationship = Relationship(
            type = "ACTED_IN",
            from = RelationshipTarget(),
            to = RelationshipTarget(),
            properties = mutableMapOf(
                "embedding" to Property(type = Neo4jType.VECTOR_FLOAT, dimension = 1),
                "embedding2" to Property(type = Neo4jType.VECTOR_FLOAT, dimension = 4096)
            )
        )
        val issues = mutableListOf<Issue>()

        for ((propertyId, property) in relationship.properties) {
            validator.validateProperty(model, "actedIn", relationship, propertyId, property, issues)
        }

        assertTrue(issues.isEmpty(), "Expected no issues when dimension is within 1-4096 inclusive")
    }
}
