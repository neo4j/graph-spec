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
package model.extension.importer.mapping

import model.GraphModel
import model.extension.ListValue
import model.extension.MapValue
import model.extension.StringValue
import model.extension.getNamed
import model.extension.importer.table.TableExtensionModule
import model.node.Node
import model.property.Property
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MappingExtensionTest {
    private fun modelWithMapping(payload: MutableMap<String, model.extension.ExtensionValue>, node: Node? = null) =
        GraphModel(
            schema = "s",
            id = "t",
            version = 1,
            nodes = node?.let { mutableMapOf("Actor" to it) } ?: mutableMapOf()
        ).apply {
            extensions[MappingExtensionModule.key] = MapValue(payload)
        }

    @Test
    fun `decodes the minimal v1 payload`() {
        val model =
            modelWithMapping(
                mutableMapOf(
                    "kind" to StringValue("node"),
                    "node" to StringValue("Actor"),
                    "table" to StringValue("actors"),
                    "key" to ListValue(mutableListOf(StringValue("actor_id")))
                ),
                node = Node(label = "Actor")
            )

        val mapping = model.getNamed(MappingExtensionModule)!!

        assertEquals("node", mapping.kind)
        assertEquals("Actor", mapping.node)
        assertEquals(setOf("actor_id"), mapping.key)
    }

    @Test
    fun `MappingReferences flags an unknown node`() {
        val model =
            modelWithMapping(
                mutableMapOf("kind" to StringValue("node"), "node" to StringValue("Ghost"))
            )
        val issues = mutableListOf<Issue>()

        MappingReferences.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("mapping_unknown_node", issues[0].code)
    }

    @Test
    fun `MappingReferences fires on a converter-style list payload`() {
        val model =
            GraphModel(schema = "s", id = "t", version = 1).apply {
                extensions[MappingExtensionModule.key] =
                    ListValue(
                        mutableListOf(
                            MapValue(
                                mutableMapOf("kind" to StringValue("node"), "node" to StringValue("Ghost"))
                            )
                        )
                    )
            }
        val issues = mutableListOf<Issue>()

        MappingReferences.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("mapping_unknown_node", issues[0].code)
        assertEquals("extensions[\"importer:mapping\"].node", issues[0].path)
    }

    @Test
    fun `MappingReferences resolves the table lookup against a list payload`() {
        val model =
            GraphModel(schema = "s", id = "t", version = 1).apply {
                extensions[TableExtensionModule.key] =
                    ListValue(
                        mutableListOf(
                            MapValue(mutableMapOf("name" to StringValue("actors"))),
                            MapValue(mutableMapOf("name" to StringValue("movies")))
                        )
                    )
                extensions[MappingExtensionModule.key] =
                    ListValue(
                        mutableListOf(
                            MapValue(
                                mutableMapOf("kind" to StringValue("node"), "table" to StringValue("movies"))
                            ),
                            MapValue(
                                mutableMapOf("kind" to StringValue("node"), "table" to StringValue("studios"))
                            )
                        )
                    )
            }
        val issues = mutableListOf<Issue>()

        MappingReferences.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("mapping_unknown_table", issues[0].code)
        assertEquals("extensions[\"importer:mapping\"].table", issues[0].path)
    }

    @Test
    fun `MappingKeyPresent flags a node mapping without key`() {
        val model =
            modelWithMapping(
                mutableMapOf("kind" to StringValue("node"), "node" to StringValue("Actor")),
                node = Node(label = "Actor")
            )
        val issues = mutableListOf<Issue>()

        MappingKeyPresent.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_node_mapping_key", issues[0].code)
    }

    @Test
    fun `MappingKeyType flags a non-string non-integer key property`() {
        val model =
            modelWithMapping(
                mutableMapOf(
                    "kind" to StringValue("node"),
                    "node" to StringValue("Actor"),
                    "key" to ListValue(mutableListOf(StringValue("embedding")))
                ),
                node =
                Node(
                    label = "Actor",
                    properties = mutableMapOf("embedding" to Property(type = "VECTOR<FLOAT>"))
                )
            )
        val issues = mutableListOf<Issue>()

        MappingKeyType.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("invalid_node_mapping_key_type", issues[0].code)
    }

    @Test
    fun `MappingKeyType passes for a STRING key`() {
        val model =
            modelWithMapping(
                mutableMapOf(
                    "kind" to StringValue("node"),
                    "node" to StringValue("Actor"),
                    "key" to ListValue(mutableListOf(StringValue("name")))
                ),
                node =
                Node(
                    label = "Actor",
                    properties = mutableMapOf("name" to Property(type = "STRING"))
                )
            )
        val issues = mutableListOf<Issue>()

        MappingKeyType.validate(model, issues)

        assertTrue(issues.isEmpty())
    }
}
