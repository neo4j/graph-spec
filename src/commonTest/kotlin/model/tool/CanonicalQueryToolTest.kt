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
package model.tool

import kotlinx.serialization.json.Json
import model.GraphModel
import model.extension.LongValue
import model.extension.StringValue
import model.node.Node
import model.relationship.Relationship
import model.relationship.RelationshipTarget
import validate.Issue
import validate.Validations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class CanonicalQueryToolTest {
    private val findCoActors =
        "MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(co:Actor) WHERE a.name = \$name RETURN co"

    private fun modelWithNodeTool(tool: Tool) = GraphModel(
        schema = "s",
        id = "t",
        version = 1,
        nodes = mutableMapOf("n" to Node(label = "N", tools = mutableListOf(tool)))
    )

    // -- Typed decode (Tool.asTyped) --

    @Test
    fun `asTyped returns null when the type token is not the module's`() {
        val tool = Tool(type = "externalRequest", extra = mutableMapOf("url" to StringValue("https://imdb.com/")))

        assertNull(tool.asTyped(CanonicalQueryToolModule))
    }

    @Test
    fun `asTyped decodes the full payload from a wire-decoded tool`() {
        val tool = Json.decodeFromString(
            ToolSerializer,
            """{
              "type": "canonicalQuery",
              "name": "findCoActors",
              "description": "Actors who shared a film with a given actor",
              "cypher": "$findCoActors"
            }"""
        )

        val typed = tool.asTyped(CanonicalQueryToolModule)

        assertNotNull(typed)
        assertEquals("canonicalQuery", typed.type)
        assertEquals("findCoActors", typed.name)
        assertEquals("Actors who shared a film with a given actor", typed.description)
        assertEquals(findCoActors, typed.cypher)
    }

    @Test
    fun `asTyped ignores unknown extras without failing`() {
        val tool = Tool(
            type = "canonicalQuery",
            extra = mutableMapOf("cypher" to StringValue("MATCH (n) RETURN n"), "ownerField" to StringValue("x"))
        )

        val typed = tool.asTyped(CanonicalQueryToolModule)

        assertNotNull(typed)
        assertEquals("MATCH (n) RETURN n", typed.cypher)
    }

    @Test
    fun `toTool and asTyped round-trip the typed shape`() {
        val typed = CanonicalQueryTool(name = "findCoActors", cypher = findCoActors)

        val tool = typed.toTool()

        assertEquals("canonicalQuery", tool.type)
        assertEquals(findCoActors, tool.extra["cypher"]?.asString)
        assertEquals(typed, tool.asTyped(CanonicalQueryToolModule))
    }

    // -- CanonicalQueryToolShape --

    @Test
    fun `pass when a canonicalQuery tool carries a cypher`() {
        val issues = mutableListOf<Issue>()

        CanonicalQueryToolShape.validate(
            modelWithNodeTool(
                Tool(type = "canonicalQuery", extra = mutableMapOf("cypher" to StringValue(findCoActors)))
            ),
            issues
        )

        assertTrue(issues.isEmpty(), "Expected no issues when cypher is set")
    }

    @Test
    fun `pass for tools of other types`() {
        val issues = mutableListOf<Issue>()

        CanonicalQueryToolShape.validate(modelWithNodeTool(Tool(type = "externalRequest")), issues)

        assertTrue(issues.isEmpty(), "Expected no issues for non-canonicalQuery tools")
    }

    @Test
    fun `fail when cypher is absent`() {
        val issues = mutableListOf<Issue>()

        CanonicalQueryToolShape.validate(
            modelWithNodeTool(Tool(type = "canonicalQuery", name = "findCoActors")),
            issues
        )

        assertEquals(1, issues.size)
        assertEquals("missing_canonical_query_cypher", issues.first().code)
        assertEquals("nodes.n.tools[0].cypher", issues.first().path)
    }

    @Test
    fun `fail when cypher is blank`() {
        val issues = mutableListOf<Issue>()

        CanonicalQueryToolShape.validate(
            modelWithNodeTool(Tool(type = "canonicalQuery", extra = mutableMapOf("cypher" to StringValue("  ")))),
            issues
        )

        assertEquals(1, issues.size)
        assertEquals("missing_canonical_query_cypher", issues.first().code)
    }

    @Test
    fun `fail when cypher is not a string`() {
        val issues = mutableListOf<Issue>()

        CanonicalQueryToolShape.validate(
            modelWithNodeTool(Tool(type = "canonicalQuery", extra = mutableMapOf("cypher" to LongValue(42)))),
            issues
        )

        assertEquals(1, issues.size)
        assertEquals("missing_canonical_query_cypher", issues.first().code)
    }

    @Test
    fun `fail with the relationship tool path`() {
        val model = GraphModel(
            schema = "s",
            id = "t",
            version = 1,
            relationships = mutableMapOf(
                "r" to Relationship(
                    type = "ACTED_IN",
                    from = RelationshipTarget(node = "a"),
                    to = RelationshipTarget(node = "b"),
                    tools = mutableListOf(Tool(type = "canonicalQuery"))
                )
            )
        )
        val issues = mutableListOf<Issue>()

        CanonicalQueryToolShape.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("relationships.r.tools[0].cypher", issues.first().path)
    }

    @Test
    fun `the rule is registered in Validations core through the module list`() {
        assertTrue(CanonicalQueryToolShape in CanonicalQueryToolModule.validations)
        assertTrue(CanonicalQueryToolModule in toolTypeModules)
        // Aggregated via toolTypeModules, not listed directly: exactly one registration.
        assertTrue(CanonicalQueryToolShape in Validations.core)
        assertEquals(1, Validations.core.count { it == CanonicalQueryToolShape })
    }
}
