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

class RelationshipPropertyVectorDimensionTest {

    private val validator = RelationshipPropertyVectorDimension
    private val model = GraphModel("4.0.0")

    @Test
    fun `fail when relationship vector property has null dimension`() {
        // ARRANGE
        val relationship = Relationship(
            type = "ACTED_IN",
            from = RelationshipTarget(),
            to = RelationshipTarget(),
            properties = mutableMapOf(
                "embedding" to Property(type = Neo4jType.VECTOR_FLOAT, dimension = null)
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        validator.validateProperty(
            model,
            "actedIn",
            relationship,
            "embedding",
            relationship.properties["embedding"]!!,
            issues
        )

        // ASSERT
        assertEquals(1, issues.size)
        val issue = issues.first()
        assertEquals("missing_vector_dimension", issue.code)
        assertEquals("relationships.actedIn.properties.embedding.dimension", issue.path)
    }

    @Test
    fun `pass when relationship vector property has a dimension set`() {
        // ARRANGE
        val relationship = Relationship(
            type = "ACTED_IN",
            from = RelationshipTarget(),
            to = RelationshipTarget(),
            properties = mutableMapOf(
                "embedding" to Property(type = Neo4jType.VECTOR_FLOAT, dimension = 128)
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        validator.validateProperty(
            model,
            "actedIn",
            relationship,
            "embedding",
            relationship.properties["embedding"]!!,
            issues
        )

        // ASSERT
        assertTrue(issues.isEmpty(), "Expected no issues when vector property has a dimension")
    }

    @Test
    fun `pass when relationship property is a non-vector type with null dimension`() {
        // ARRANGE
        val relationship = Relationship(
            type = "ACTED_IN",
            from = RelationshipTarget(),
            to = RelationshipTarget(),
            properties = mutableMapOf(
                "name" to Property(type = Neo4jType.STRING, dimension = null)
            )
        )
        val issues = mutableListOf<Issue>()

        // ACT
        validator.validateProperty(
            model,
            "actedIn",
            relationship,
            "name",
            relationship.properties["name"]!!,
            issues
        )

        // ASSERT
        assertTrue(issues.isEmpty(), "Expected no issues for non-vector type with null dimension")
    }
}
