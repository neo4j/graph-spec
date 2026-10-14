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
package org.neo4j.spec.graph.validate.mapping

import org.neo4j.spec.graph.model.GraphModel
import org.neo4j.spec.graph.model.extension.mapping.Mapping
import org.neo4j.spec.graph.model.extension.mapping.NodeMapping
import org.neo4j.spec.graph.model.extension.mapping.QueryMapping
import org.neo4j.spec.graph.model.extension.mapping.RelationshipMapping
import org.neo4j.spec.graph.model.extension.mapping.TargetMapping
import org.neo4j.spec.graph.validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MappingDependenciesTest {

    private fun node(dependsOn: String = "") =
        NodeMapping(node = "n", table = "t", properties = mutableMapOf(), dependsOn = dependsOn)

    private fun rel(dependsOn: String = "") = RelationshipMapping(
        relationship = "r",
        table = "t",
        fromNode = TargetMapping(),
        toNode = TargetMapping(),
        dependsOn = dependsOn
    )

    private fun query(dependsOn: String = "") = QueryMapping(table = "t", query = "RETURN 1", dependsOn = dependsOn)

    private fun validate(vararg mappings: Pair<String, Mapping>): List<Issue> {
        val model = GraphModel(schema = "https://neo4j.com/ontology-graph-spec/4.0.0/", version = "1.0.0", id = "test")
        mappings.groupBy({ it.first }, { it.second }).forEach { (id, group) ->
            model.extensions.mappings[id] = group.toMutableList()
        }
        val issues = mutableListOf<Issue>()
        MappingDependencies.validate(model, issues)
        return issues
    }

    @Test
    fun `no mappings is valid`() {
        assertTrue(validate().isEmpty())
    }

    @Test
    fun `mappings without dependencies are valid`() {
        assertTrue(validate("a" to node(), "b" to rel(), "c" to query()).isEmpty())
    }

    @Test
    fun `dependency on another mapping is valid for every mapping type`() {
        assertTrue(validate("a" to node(), "b" to rel("a"), "c" to query("b"), "d" to node("c")).isEmpty())
    }

    @Test
    fun `unknown dependency is invalid`() {
        val issues = validate("a" to node(), "b" to rel("missing"))
        assertEquals(listOf("mapping_depends_on_unknown"), issues.map { it.code })
        assertEquals("extensions.mappings.b[0].depends_on", issues.single().path)
    }

    @Test
    fun `self dependency is invalid`() {
        val issues = validate("a" to node("a"))
        assertEquals(listOf("mapping_depends_on_self"), issues.map { it.code })
        assertEquals("extensions.mappings.a[0].depends_on", issues.single().path)
    }

    @Test
    fun `two mapping cycle is reported once`() {
        val issues = validate("a" to node("b"), "b" to node("a"))
        assertEquals(listOf("mapping_dependency_cycle"), issues.map { it.code })
        assertEquals("extensions.mappings.a", issues.single().path)
    }

    @Test
    fun `longer cycle with a tail is reported once`() {
        val issues = validate("tail" to node("a"), "a" to node("b"), "b" to node("c"), "c" to node("a"))
        assertEquals(listOf("mapping_dependency_cycle"), issues.map { it.code })
        assertEquals("extensions.mappings.a", issues.single().path)
    }

    @Test
    fun `separate cycles are each reported`() {
        val issues = validate("a" to node("b"), "b" to node("a"), "c" to node("d"), "d" to node("c"))
        assertEquals(2, issues.count { it.code == "mapping_dependency_cycle" })
    }

    @Test
    fun `mappings in the same group can depend on different groups`() {
        val issues = validate("a" to node(), "b" to node(), "c" to node("a"), "c" to rel("b"))
        assertTrue(issues.isEmpty())
    }

    @Test
    fun `dependency on the group a mapping belongs to is invalid`() {
        val issues = validate("a" to node(), "b" to node(), "b" to rel("b"))
        assertEquals(listOf("mapping_depends_on_self"), issues.map { it.code })
        assertEquals("extensions.mappings.b[1].depends_on", issues.single().path)
    }

    @Test
    fun `long chain is valid`() {
        assertTrue(validate("a" to node(), "b" to node("a"), "c" to node("b"), "d" to node("c")).isEmpty())
    }
}
