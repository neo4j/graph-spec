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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class MappingKeysTest {

    private val properties = mutableMapOf(
        "id" to PropertyMapping("person_id"),
        "name" to PropertyMapping("person_name")
    )

    private fun node(columns: Set<String> = emptySet(), keys: Set<String> = emptySet()) = NodeMapping(
        node = "person",
        table = "people",
        properties = properties,
        keyColumns = columns.toMutableSet(),
        keyProperties = keys.toMutableSet()
    )

    private fun relationship(columns: Set<String> = emptySet(), keys: Set<String> = emptySet()) = RelationshipMapping(
        relationship = "knows",
        table = "people",
        fromNode = TargetMapping(),
        toNode = TargetMapping(),
        properties = properties,
        keyColumns = columns.toMutableSet(),
        keyProperties = keys.toMutableSet()
    )

    @Test
    fun `node columns to properties`() {
        val mapping = node(columns = setOf("person_id"))

        mapping.useKeyProperties()

        assertEquals(setOf("id"), mapping.keyProperties)
        assertTrue(mapping.keyColumns.isEmpty())
    }

    @Test
    fun `node properties to columns`() {
        val mapping = node(keys = setOf("id", "name"))

        mapping.useKeyColumns()

        assertEquals(setOf("person_id", "person_name"), mapping.keyColumns)
        assertTrue(mapping.keyProperties.isEmpty())
    }

    @Test
    fun `relationship columns to properties and back`() {
        val mapping = relationship(columns = setOf("person_name"))

        mapping.useKeyProperties()
        assertEquals(setOf("name"), mapping.keyProperties)
        assertTrue(mapping.keyColumns.isEmpty())

        mapping.useKeyColumns()
        assertEquals(setOf("person_name"), mapping.keyColumns)
        assertTrue(mapping.keyProperties.isEmpty())
    }

    @Test
    fun `column mapped by several properties yields all of them`() {
        val mapping = node(columns = setOf("shared"))
        mapping.properties["a"] = PropertyMapping("shared")
        mapping.properties["b"] = PropertyMapping("shared")

        mapping.useKeyProperties()

        assertEquals(setOf("a", "b"), mapping.keyProperties)
    }

    @Test
    fun `nothing to convert leaves the mapping unchanged`() {
        val mapping = node(keys = setOf("id"))

        mapping.useKeyProperties()

        assertEquals(setOf("id"), mapping.keyProperties)
    }

    @Test
    fun `unmapped key column fails without changing the mapping`() {
        val mapping = node(columns = setOf("person_id", "missing"))

        assertFailsWith<IllegalArgumentException> { mapping.useKeyProperties() }

        assertEquals(setOf("person_id", "missing"), mapping.keyColumns)
        assertTrue(mapping.keyProperties.isEmpty())
    }

    @Test
    fun `unmapped key property fails without changing the mapping`() {
        val mapping = relationship(keys = setOf("id", "missing"))

        assertFailsWith<IllegalArgumentException> { mapping.useKeyColumns() }

        assertEquals(setOf("id", "missing"), mapping.keyProperties)
        assertTrue(mapping.keyColumns.isEmpty())
    }
}
