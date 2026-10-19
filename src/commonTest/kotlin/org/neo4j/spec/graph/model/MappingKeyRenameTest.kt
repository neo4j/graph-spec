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
package org.neo4j.spec.graph.model

import org.neo4j.spec.graph.model.extension.GraphModelExtensions
import org.neo4j.spec.graph.model.extension.mapping.RelationshipMapping
import org.neo4j.spec.graph.model.extension.mapping.TargetMapping
import org.neo4j.spec.graph.model.node.Node
import org.neo4j.spec.graph.model.property.Property
import org.neo4j.spec.graph.model.relationship.Relationship
import org.neo4j.spec.graph.model.relationship.RelationshipTarget
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * A relationship mapping key refers to relationship properties, so it must only be renamed with the relationship's
 * property renames even when a node at either end has a property with the same id.
 */
class MappingKeyRenameTest {

    private fun mapping() = RelationshipMapping(
        relationship = "KNOWS",
        table = "knows",
        fromNode = TargetMapping(),
        toNode = TargetMapping(),
        key = mutableSetOf("since", "age")
    )

    private fun model(mapping: RelationshipMapping, pretty: Boolean) = GraphModel(
        schema = "https://neo4j.com/docs/ontology-graph-spec/1.0.0/schema.json",
        version = "1.0",
        id = "test",
        nodes = mutableMapOf(
            "Person" to Node(
                label = "Person",
                properties = mutableMapOf("age" to Property())
            )
        ),
        relationships = mutableMapOf(
            "KNOWS" to Relationship(
                type = "KNOWS",
                from = RelationshipTarget(node = "Person"),
                to = RelationshipTarget(node = "Person"),
                properties = mutableMapOf("since" to Property(), "age" to Property())
            )
        ),
        extensions = GraphModelExtensions(mappings = mutableMapOf("m" to mutableListOf(mapping))),
        pretty = pretty
    )

    @Test
    fun `internalise only renames relationship mapping keys using relationship properties`() {
        val mapping = mapping()
        val model = model(mapping, pretty = true)

        model.internalise()

        assertEquals(setOf("relationship0_property0", "relationship0_property1"), mapping.key)
    }

    @Test
    fun `prettify only renames relationship mapping keys using relationship properties`() {
        val mapping = mapping()
        val model = model(mapping, pretty = true)
        model.internalise()

        model.prettify()

        assertEquals(setOf("since", "age"), mapping.key)
    }
}
