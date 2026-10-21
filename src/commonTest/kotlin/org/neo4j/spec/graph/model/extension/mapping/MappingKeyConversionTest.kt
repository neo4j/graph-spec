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
package org.neo4j.spec.graph.model.extension.mapping

import org.neo4j.spec.graph.model.GraphModel
import org.neo4j.spec.graph.model.extension.GraphModelExtensions
import org.neo4j.spec.graph.model.node.Node
import org.neo4j.spec.graph.model.relationship.Relationship
import org.neo4j.spec.graph.model.relationship.RelationshipTarget
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MappingKeyConversionTest {

    private fun person(columns: Set<String> = emptySet(), keys: Set<String> = emptySet()) = NodeMapping(
        node = "person",
        table = "people",
        properties = mutableMapOf("id" to PropertyMapping("person_id")),
        keyColumns = columns.toMutableSet(),
        keyProperties = keys.toMutableSet()
    )

    private fun company() = NodeMapping(
        node = "company",
        table = "companies",
        properties = mutableMapOf("code" to PropertyMapping("company_code")),
        keyProperties = mutableSetOf("code")
    )

    private fun worksAt(from: TargetMapping, to: TargetMapping, relationship: String = "worksAt") = RelationshipMapping(
        relationship = relationship,
        table = "jobs",
        fromNode = from,
        toNode = to,
        properties = mutableMapOf("since" to PropertyMapping("start_year")),
        keyColumns = mutableSetOf("start_year")
    )

    private fun model(vararg mappings: Mapping) = GraphModel(
        version = "1",
        id = "test",
        nodes = mutableMapOf("person" to Node(), "company" to Node()),
        relationships = mutableMapOf(
            "worksAt" to Relationship(
                type = "WORKS_AT",
                from = RelationshipTarget(node = "person"),
                to = RelationshipTarget(node = "company")
            )
        ),
        extensions = GraphModelExtensions(mappings = mutableMapOf("m0" to mappings.toMutableList()))
    )

    @Test
    fun `converts a relationship mapping and its targets to properties`() {
        val relationship = worksAt(
            TargetMapping(nodeKeyColumn = "person_id", relationshipColumn = "pid"),
            TargetMapping(nodeKeyColumn = "company_code", relationshipColumn = "cid")
        )
        val model = model(person(), company(), relationship)

        MappingKeyConversion.useKeyProperties(model, relationship)

        assertEquals(setOf("since"), relationship.keyProperties)
        assertTrue(relationship.keyColumns.isEmpty())
        assertEquals(TargetMapping(nodeKeyProperty = "id", relationshipColumn = "pid"), relationship.fromNode)
        assertEquals(TargetMapping(nodeKeyProperty = "code", relationshipColumn = "cid"), relationship.toNode)
    }

    @Test
    fun `converts a relationship mapping and its targets to columns`() {
        val relationship = worksAt(
            TargetMapping(nodeKeyProperty = "id", relationshipColumn = "pid"),
            TargetMapping(nodeKeyProperty = "code", relationshipColumn = "cid")
        )
        val model = model(person(), company(), relationship)

        MappingKeyConversion.useKeyColumns(model, relationship)

        assertEquals(TargetMapping(nodeKeyColumn = "person_id", relationshipColumn = "pid"), relationship.fromNode)
        assertEquals(TargetMapping(nodeKeyColumn = "company_code", relationshipColumn = "cid"), relationship.toNode)
    }

    @Test
    fun `converts every mapping in the model`() {
        val node = person(columns = setOf("person_id"))
        val relationship = worksAt(
            TargetMapping(nodeKeyColumn = "person_id", relationshipColumn = "pid"),
            TargetMapping(nodeKeyColumn = "company_code", relationshipColumn = "cid")
        )

        MappingKeyConversion.useKeyProperties(model(node, company(), relationship))

        assertEquals(setOf("id"), node.keyProperties)
        assertEquals("id", relationship.fromNode.nodeKeyProperty)
        assertEquals("code", relationship.toNode.nodeKeyProperty)
    }

    @Test
    fun `converts every mapping in the model to columns`() {
        val node = person(keys = setOf("id"))
        val relationship = worksAt(
            TargetMapping(nodeKeyProperty = "id", relationshipColumn = "pid"),
            TargetMapping(nodeKeyProperty = "code", relationshipColumn = "cid")
        )
        val company = company()

        MappingKeyConversion.useKeyColumns(model(node, company, relationship))

        assertEquals(setOf("person_id"), node.keyColumns)
        assertTrue(node.keyProperties.isEmpty())
        assertEquals(setOf("company_code"), company.keyColumns)
        assertTrue(company.keyProperties.isEmpty())
        assertEquals("person_id", relationship.fromNode.nodeKeyColumn)
        assertEquals("company_code", relationship.toNode.nodeKeyColumn)
        assertTrue(relationship.fromNode.nodeKeyProperty.isEmpty())
    }

    @Test
    fun `a failed conversion changes nothing`() {
        val relationship = worksAt(
            TargetMapping(nodeKeyColumn = "person_id", relationshipColumn = "pid"),
            TargetMapping(nodeKeyColumn = "unknown", relationshipColumn = "cid")
        )
        val model = model(person(), company(), relationship)

        assertFailsWith<IllegalArgumentException> { MappingKeyConversion.useKeyProperties(model) }

        assertEquals(setOf("start_year"), relationship.keyColumns)
        assertEquals("person_id", relationship.fromNode.nodeKeyColumn)
    }

    @Test
    fun `fails when the node mapping is missing`() {
        val relationship = worksAt(
            TargetMapping(nodeKeyColumn = "person_id", relationshipColumn = "pid"),
            TargetMapping()
        )

        assertFailsWith<IllegalArgumentException> {
            MappingKeyConversion.useKeyProperties(model(relationship), relationship)
        }
    }
}
