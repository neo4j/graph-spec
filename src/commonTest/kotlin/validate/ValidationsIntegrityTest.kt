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
import model.relationship.Relationship
import model.relationship.RelationshipTarget
import kotlin.test.Test
import kotlin.test.assertTrue

class ValidationsIntegrityTest {

    @Test
    fun `integrity is a subset of all`() {
        // ARRANGE
        val allCodes = Validations.all.map { it::class.simpleName }.toSet()
        val integrityCodes = Validations.integrity.map { it::class.simpleName }.toSet()

        // ASSERT
        assertTrue(integrityCodes.isNotEmpty(), "integrity should not be empty")
        assertTrue(
            integrityCodes.all { it in allCodes },
            "every integrity validator should be in all: ${integrityCodes - allCodes}"
        )
    }

    @Test
    fun `integrity catches corrupt model with blank relationship type`() {
        // ARRANGE
        val model = GraphModel("4.0.0").apply {
            relationships["broken"] = Relationship(
                type = "  ",
                from = RelationshipTarget(),
                to = RelationshipTarget()
            )
        }
        val issues = model.validate(Validations.integrity)

        // ASSERT
        assertTrue(issues.any { it.code == "missing_relation_type" })
    }

    @Test
    fun `integrity catches corrupt model with dangling node reference`() {
        // ARRANGE
        val model = GraphModel("4.0.0").apply {
            relationships["broken"] = Relationship(
                type = "ACTED_IN",
                from = RelationshipTarget(node = "nonexistent"),
                to = RelationshipTarget(node = "also_nonexistent")
            )
        }
        val issues = model.validate(Validations.integrity)

        // ASSERT
        assertTrue(issues.any { it.code == "missing_relation_from_node" })
    }

    @Test
    fun `integrity passes on valid model`() {
        // ARRANGE
        val model = GraphModel("4.0.0").apply {
            nodes["person"] = Node(
                labels = Labels(identifier = "Person"),
                properties = mutableMapOf("name" to Property())
            )
            nodes["movie"] = Node(
                labels = Labels(identifier = "Movie"),
                properties = mutableMapOf("title" to Property())
            )
            relationships["acted_in"] = Relationship(
                type = "ACTED_IN",
                from = RelationshipTarget(node = "person"),
                to = RelationshipTarget(node = "movie")
            )
        }
        val issues = model.validate(Validations.integrity)
        // ASSERT
        assertTrue(issues.isEmpty(), "Expected no issues on valid model, got: ${issues.size}")
    }
}
