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
package validate.relationship.index

import model.GraphModel
import model.property.Property
import model.relationship.Relationship
import model.relationship.RelationshipConstraint
import model.relationship.RelationshipIndex
import model.relationship.RelationshipTarget
import model.type.ConstraintType
import model.type.IndexType
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationshipIndexDuplicatePropertySetTest {

    private val validator = RelationshipIndexDuplicatePropertySet
    private val model = GraphModel("4.0.0")

    private fun relationshipWithIndexes(vararg indexes: Pair<String, Set<String>>): Relationship = Relationship(
        type = "KNOWS",
        from = RelationshipTarget("Person"),
        to = RelationshipTarget("Movie"),
        properties = mutableMapOf(
            "email" to Property(),
            "name" to Property(),
            "age" to Property()
        ),
        indexes = indexes.associate { (id, properties) ->
            id to RelationshipIndex(
                type = IndexType.RANGE,
                properties = properties.toMutableSet()
            )
        }.toMutableMap()
    )

    private fun validate(relationship: Relationship): List<Issue> {
        val issues = mutableListOf<Issue>()
        for ((indexId, index) in relationship.indexes) {
            validator.validateIndex(model, "rel1", relationship, indexId, index, issues)
        }
        return issues
    }

    @Test
    fun `pass when index property sets are distinct`() {
        val relationship = relationshipWithIndexes("idx1" to setOf("email", "name"), "idx2" to setOf("email"))

        val issues = validate(relationship)

        assertTrue(issues.isEmpty(), "Expected no issues when index property sets are distinct")
    }

    @Test
    fun `fail when two indexes share the same property set`() {
        val relationship = relationshipWithIndexes("idx1" to setOf("email", "name"), "idx2" to setOf("email", "name"))

        val issues = validate(relationship)

        assertEquals(2, issues.size)
        assertEquals("duplicate_index_property_set", issues.first().code)
        assertEquals("relationships.rel1.indexes.idx1.properties", issues.first().path)
    }

    @Test
    fun `fail when property sets are equal in different order`() {
        val relationship = Relationship(
            type = "KNOWS",
            from = RelationshipTarget("Person"),
            to = RelationshipTarget("Movie"),
            properties = mutableMapOf("email" to Property(), "name" to Property()),
            indexes = mutableMapOf(
                "idx1" to RelationshipIndex(
                    type = IndexType.RANGE,
                    properties = mutableSetOf("email", "name")
                ),
                "idx2" to RelationshipIndex(
                    type = IndexType.RANGE,
                    properties = mutableSetOf("name", "email")
                )
            )
        )

        val issues = validate(relationship)

        assertEquals(2, issues.size)
    }

    @Test
    fun `fail when two single-property indexes share the same property`() {
        val relationship = relationshipWithIndexes("idx1" to setOf("email"), "idx2" to setOf("email"))

        val issues = validate(relationship)

        assertEquals(2, issues.size)
    }

    @Test
    fun `pass when index property sets are empty`() {
        val relationship = relationshipWithIndexes("idx1" to emptySet(), "idx2" to emptySet())

        val issues = validate(relationship)

        assertTrue(issues.isEmpty(), "Empty property sets are never duplicates")
    }

    @Test
    fun `pass when one property set is a strict subset of another`() {
        val relationship = relationshipWithIndexes(
            "idx1" to setOf("email", "name"),
            "idx2" to setOf("email", "name", "age")
        )

        val issues = validate(relationship)

        assertTrue(issues.isEmpty(), "A subset is not an exact match")
    }

    @Test
    fun `fail when index property set matches a unique constraint's set`() {
        val relationship = relationshipWithIndexes("idx1" to setOf("email", "name"))
        relationship.constraints["uniq_email_name"] = RelationshipConstraint(
            type = ConstraintType.UNIQUE,
            properties = mutableSetOf("email", "name"),
            name = "uniq_email_name"
        )

        val issues = validate(relationship)

        assertEquals(1, issues.size)
        assertEquals("duplicate_index_property_set", issues.first().code)
    }

    @Test
    fun `pass when the matching constraint is an existence constraint`() {
        val relationship = relationshipWithIndexes("idx1" to setOf("email"))
        relationship.constraints["exists_email"] = RelationshipConstraint(
            type = ConstraintType.EXISTS,
            properties = mutableSetOf("email"),
            name = "exists_email"
        )

        val issues = validate(relationship)

        assertTrue(issues.isEmpty(), "Existence constraints never imply an index")
    }

    @Test
    fun `pass when the matching constraint has no name`() {
        val relationship = relationshipWithIndexes("idx1" to setOf("email"))
        relationship.constraints["uniq_email"] = RelationshipConstraint(
            type = ConstraintType.UNIQUE,
            properties = mutableSetOf("email"),
            name = null
        )

        val issues = validate(relationship)

        assertTrue(issues.isEmpty(), "Unnamed constraints are not valid for index view")
    }
}
