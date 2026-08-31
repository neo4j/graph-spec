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
import model.property.Property
import model.relationship.Relationship
import model.relationship.RelationshipTarget
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RelationshipPropertyDuplicateNameTest {

    private val validator = RelationshipPropertyDuplicateName
    private val model = GraphModel("4.0.0")
    private val target = RelationshipTarget(node = "n:1", label = "Person")

    @Test
    fun `fail on duplicate property name - all occurrences flagged`() {
        // UPX findArrayDuplicates + includes flags ALL properties with a duplicated name
        val rel = Relationship(
            type = "KNOWS",
            from = target,
            to = target,
            properties = mutableMapOf(
                "p:1" to Property(name = "since"),
                "p:2" to Property(name = "weight"),
                "p:3" to Property(name = "since")
            )
        )
        val issuesFirst = mutableListOf<Issue>()
        val issuesThird = mutableListOf<Issue>()

        validator.validateProperty(model, "r:1", rel, "p:1", rel.properties["p:1"]!!, issuesFirst)
        validator.validateProperty(model, "r:1", rel, "p:3", rel.properties["p:3"]!!, issuesThird)

        assertEquals(1, issuesFirst.size, "First occurrence should also be flagged as duplicate")
        assertEquals("duplicate_property_name", issuesFirst.first().code)
        assertEquals(1, issuesThird.size)
        assertEquals("duplicate_property_name", issuesThird.first().code)
    }

    @Test
    fun `pass when all property names are unique`() {
        val rel = Relationship(
            type = "KNOWS",
            from = target,
            to = target,
            properties = mutableMapOf(
                "p:1" to Property(name = "since"),
                "p:2" to Property(name = "weight")
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateProperty(model, "r:1", rel, "p:1", rel.properties["p:1"]!!, issues)
        validator.validateProperty(model, "r:1", rel, "p:2", rel.properties["p:2"]!!, issues)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun `pass when property name is blank`() {
        val rel = Relationship(
            type = "KNOWS",
            from = target,
            to = target,
            properties = mutableMapOf("p:1" to Property(name = ""))
        )
        val issues = mutableListOf<Issue>()

        validator.validateProperty(model, "r:1", rel, "p:1", rel.properties["p:1"]!!, issues)

        assertTrue(issues.isEmpty())
    }
}
