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
package validate.node

import model.GraphModel
import model.node.Labels
import model.node.Node
import model.property.Property
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodePropertyDuplicateNameTest {

    private val validator = NodePropertyDuplicateName
    private val model = GraphModel("4.0.0")

    @Test
    fun `fail on duplicate property name - all occurrences flagged`() {
        // UPX findArrayDuplicates + includes flags ALL properties with a duplicated name
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf(
                "p:1" to Property(name = "email"),
                "p:2" to Property(name = "name"),
                "p:3" to Property(name = "email")
            )
        )
        val issuesFirst = mutableListOf<Issue>()
        val issuesThird = mutableListOf<Issue>()

        validator.validateProperty(model, "n:1", node, "p:1", node.properties["p:1"]!!, issuesFirst)
        validator.validateProperty(model, "n:1", node, "p:3", node.properties["p:3"]!!, issuesThird)

        assertEquals(1, issuesFirst.size, "First occurrence should also be flagged as duplicate")
        assertEquals("duplicate_property_name", issuesFirst.first().code)
        assertEquals(1, issuesThird.size)
        assertEquals("duplicate_property_name", issuesThird.first().code)
    }

    @Test
    fun `pass when all property names are unique`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf(
                "p:1" to Property(name = "email"),
                "p:2" to Property(name = "name")
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateProperty(model, "n:1", node, "p:1", node.properties["p:1"]!!, issues)
        validator.validateProperty(model, "n:1", node, "p:2", node.properties["p:2"]!!, issues)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun `pass when property name is blank`() {
        // blank names are the empty-name validator's concern, not this one
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("p:1" to Property(name = ""))
        )
        val issues = mutableListOf<Issue>()

        validator.validateProperty(model, "n:1", node, "p:1", node.properties["p:1"]!!, issues)

        assertTrue(issues.isEmpty())
    }
}
