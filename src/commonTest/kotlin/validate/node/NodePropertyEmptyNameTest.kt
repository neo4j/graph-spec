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

class NodePropertyEmptyNameTest {

    private val validator = NodePropertyEmptyName
    private val model = GraphModel("4.0.0")

    @Test
    fun `fail when property name is blank`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("p:1" to Property(name = ""))
        )
        val issues = mutableListOf<Issue>()

        validator.validateProperty(model, "n:1", node, "p:1", node.properties["p:1"]!!, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_property_name", issues.first().code)
    }

    @Test
    fun `fail when property name is null`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("p:1" to Property(name = null))
        )
        val issues = mutableListOf<Issue>()

        validator.validateProperty(model, "n:1", node, "p:1", node.properties["p:1"]!!, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_property_name", issues.first().code)
    }

    @Test
    fun `pass when property has a valid name`() {
        val node = Node(
            labels = Labels(identifier = "Person"),
            properties = mutableMapOf("p:1" to Property(name = "email"))
        )
        val issues = mutableListOf<Issue>()

        validator.validateProperty(model, "n:1", node, "p:1", node.properties["p:1"]!!, issues)

        assertTrue(issues.isEmpty())
    }
}
