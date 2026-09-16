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
package validate

import model.GraphModel
import model.node.Labels
import model.node.Node
import model.property.Property
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UniqueIdsTest {

    @Test
    fun `pass when every id in the document is unique`() {
        val model = GraphModel(
            "4.0.0",
            nodes = mutableMapOf(
                "node0" to Node(
                    labels = Labels(identifier = "Person"),
                    properties = mutableMapOf("p:0" to Property(name = "email"))
                ),
                "node1" to Node(
                    labels = Labels(identifier = "Company"),
                    properties = mutableMapOf("p:1" to Property(name = "name"))
                )
            )
        )
        model.internalise()
        val issues = mutableListOf<Issue>()

        UniqueIds.validate(model, issues)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun `fail when two entities keep the same property id`() {
        val model = GraphModel(
            "4.0.0",
            nodes = mutableMapOf(
                "node0" to Node(
                    labels = Labels(identifier = "Person"),
                    properties = mutableMapOf("p:0" to Property(name = "email"))
                ),
                "node1" to Node(
                    labels = Labels(identifier = "Company"),
                    properties = mutableMapOf("p:0" to Property(name = "email"))
                )
            )
        )
        model.internalise()
        val issues = mutableListOf<Issue>()

        UniqueIds.validate(model, issues)

        assertEquals(2, issues.size, "both entries carrying the duplicate id should be flagged")
        assertTrue(issues.all { it.code == "duplicate_id" })
        assertEquals(
            setOf("nodes.node0.properties.p:0", "nodes.node1.properties.p:0"),
            issues.mapNotNull { it.path }.toSet()
        )
    }
}
