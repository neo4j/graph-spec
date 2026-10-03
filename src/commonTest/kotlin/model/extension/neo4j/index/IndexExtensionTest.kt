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
package model.extension.neo4j.index

import model.GraphModel
import model.extension.ExtensionValue
import model.extension.ListValue
import model.extension.MapValue
import model.extension.StringValue
import model.extension.getNamed
import model.node.Node
import model.property.Property
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class IndexExtensionTest {
    private fun nodeWithIndex(payload: Map<String, ExtensionValue>): Node =
        Node(label = "N", properties = mutableMapOf("name" to Property(type = "STRING"))).apply {
            extensions[IndexExtensionModule.key] = MapValue(payload.toMutableMap())
        }

    @Test
    fun `decodes the minimal v1 payload`() {
        val node =
            nodeWithIndex(
                mutableMapOf(
                    "name" to StringValue("actor_names"),
                    "properties" to ListValue(mutableListOf(StringValue("name")))
                )
            )

        val index = node.getNamed(IndexExtensionModule)!!

        assertEquals("actor_names", index.name)
        assertEquals(listOf("name"), index.properties)
        assertNull(index.type)
    }

    @Test
    fun `IndexPropertyReferences flags a property the element does not declare`() {
        val node =
            nodeWithIndex(
                mutableMapOf(
                    "properties" to ListValue(mutableListOf(StringValue("name"), StringValue("missing")))
                )
            )
        val model = GraphModel(schema = "s", id = "t", version = 1, nodes = mutableMapOf("n" to node))
        val issues = mutableListOf<Issue>()

        IndexPropertyReferences.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_index_property", issues[0].code)
        assertTrue(issues[0].message.contains("missing"))
    }

    @Test
    fun `IndexPropertyReferences checks every payload of a list-valued extension`() {
        val node =
            Node(label = "N", properties = mutableMapOf("name" to Property(type = "STRING"))).apply {
                extensions[IndexExtensionModule.key] =
                    ListValue(
                        mutableListOf(
                            MapValue(mutableMapOf("properties" to ListValue(mutableListOf(StringValue("name"))))),
                            MapValue(mutableMapOf("properties" to ListValue(mutableListOf(StringValue("missing")))))
                        )
                    )
            }
        val model = GraphModel(schema = "s", id = "t", version = 1, nodes = mutableMapOf("n" to node))
        val issues = mutableListOf<Issue>()

        IndexPropertyReferences.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("nodes.n.extensions[\"neo4j:index\"].properties.missing", issues[0].path)
    }

    @Test
    fun `IndexPropertyReferences resolves property-level indexes against the enclosing element`() {
        val node =
            Node(
                label = "N",
                properties =
                mutableMapOf(
                    "embedding" to
                        Property(type = "VECTOR<FLOAT>").apply {
                            extensions[IndexExtensionModule.key] =
                                MapValue(
                                    mutableMapOf(
                                        "type" to StringValue("vector"),
                                        "properties" to
                                            ListValue(mutableListOf(StringValue("embedding"), StringValue("missing")))
                                    )
                                )
                        }
                )
            )
        val model = GraphModel(schema = "s", id = "t", version = 1, nodes = mutableMapOf("n" to node))
        val issues = mutableListOf<Issue>()

        IndexPropertyReferences.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_index_property", issues[0].code)
        assertEquals(
            "nodes.n.properties.embedding.extensions[\"neo4j:index\"].properties.missing",
            issues[0].path
        )
    }

    @Test
    fun `IndexPropertyReferences skips root-level payloads`() {
        val model = GraphModel(schema = "s", id = "t", version = 1).apply {
            extensions[IndexExtensionModule.key] =
                MapValue(mutableMapOf("properties" to ListValue(mutableListOf(StringValue("anything")))))
        }
        val issues = mutableListOf<Issue>()

        IndexPropertyReferences.validate(model, issues)

        assertEquals(emptyList(), issues)
    }

    @Test
    fun `IndexOptionsMatch flags options of the wrong type`() {
        val node =
            nodeWithIndex(
                mutableMapOf(
                    "type" to StringValue("vector"),
                    "options" to MapValue(mutableMapOf("fulltext" to MapValue(mutableMapOf())))
                )
            )
        val model = GraphModel(schema = "s", id = "t", version = 1, nodes = mutableMapOf("n" to node))
        val issues = mutableListOf<Issue>()

        IndexOptionsMatch.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("index_options_type_mismatch", issues[0].code)
    }

    @Test
    fun `IndexOptionsMatch runs at root and property level too`() {
        val mismatched =
            MapValue(
                mutableMapOf(
                    "type" to StringValue("vector"),
                    "options" to MapValue(mutableMapOf("fulltext" to MapValue(mutableMapOf())))
                )
            )
        val model = GraphModel(
            schema = "s",
            id = "t",
            version = 1,
            nodes =
            mutableMapOf(
                "n" to
                    Node(
                        label = "N",
                        properties =
                        mutableMapOf(
                            "embedding" to
                                Property(type = "VECTOR<FLOAT>").apply {
                                    extensions[IndexExtensionModule.key] = mismatched
                                }
                        )
                    )
            )
        ).apply { extensions[IndexExtensionModule.key] = mismatched }
        val issues = mutableListOf<Issue>()

        IndexOptionsMatch.validate(model, issues)

        assertEquals(2, issues.size)
        assertEquals(
            listOf(
                "extensions[\"neo4j:index\"].options.fulltext",
                "nodes.n.properties.embedding.extensions[\"neo4j:index\"].options.fulltext"
            ),
            issues.map { it.path }
        )
    }

    @Test
    fun `IndexConstraintNameConflict flags a name shared with a constraint`() {
        val node =
            nodeWithIndex(mutableMapOf("name" to StringValue("shared"))).apply {
                constraints.add(
                    model.node.Constraint(type = "unique", properties = mutableListOf("name"), name = "shared")
                )
            }
        val model = GraphModel(schema = "s", id = "t", version = 1, nodes = mutableMapOf("n" to node))
        val issues = mutableListOf<Issue>()

        IndexConstraintNameConflict.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("duplicate_index_constraint_name", issues[0].code)
    }

    @Test
    fun `IndexConstraintNameConflict sees names at every placement level`() {
        val model = GraphModel(
            schema = "s",
            id = "t",
            version = 1,
            nodes =
            mutableMapOf(
                "n" to
                    Node(
                        label = "N",
                        properties =
                        mutableMapOf(
                            "embedding" to
                                Property(type = "VECTOR<FLOAT>").apply {
                                    extensions[IndexExtensionModule.key] =
                                        MapValue(mutableMapOf("name" to StringValue("shared")))
                                }
                        )
                    )
            )
        ).apply { extensions[IndexExtensionModule.key] = MapValue(mutableMapOf("name" to StringValue("shared"))) }
        val issues = mutableListOf<Issue>()

        IndexConstraintNameConflict.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("duplicate_index_constraint_name", issues[0].code)
    }

    @Test
    fun `IndexConstraintNameConflict flags duplicate names within one element's payload list`() {
        val node =
            Node(label = "N", properties = mutableMapOf("name" to Property(type = "STRING"))).apply {
                extensions[IndexExtensionModule.key] =
                    ListValue(
                        mutableListOf(
                            MapValue(mutableMapOf("name" to StringValue("dup"))),
                            MapValue(mutableMapOf("name" to StringValue("dup")))
                        )
                    )
            }
        val model = GraphModel(schema = "s", id = "t", version = 1, nodes = mutableMapOf("n" to node))
        val issues = mutableListOf<Issue>()

        IndexConstraintNameConflict.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("duplicate_index_constraint_name", issues[0].code)
    }

    @Test
    fun `IndexConstraintNameConflict stays silent for distinct names in one payload list`() {
        val node =
            Node(label = "N", properties = mutableMapOf("name" to Property(type = "STRING"))).apply {
                extensions[IndexExtensionModule.key] =
                    ListValue(
                        mutableListOf(
                            MapValue(mutableMapOf("name" to StringValue("a"))),
                            MapValue(mutableMapOf("name" to StringValue("b")))
                        )
                    )
            }
        val model = GraphModel(schema = "s", id = "t", version = 1, nodes = mutableMapOf("n" to node))
        val issues = mutableListOf<Issue>()

        IndexConstraintNameConflict.validate(model, issues)

        assertEquals(emptyList(), issues)
    }
}
