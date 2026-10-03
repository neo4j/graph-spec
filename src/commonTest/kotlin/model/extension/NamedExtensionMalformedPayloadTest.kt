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
package model.extension

import kotlinx.serialization.SerializationException
import model.GraphModel
import model.extension.importer.mapping.MappingExtensionModule
import model.extension.importer.table.TableExtensionModule
import model.extension.neo4j.index.IndexExtensionModule
import validate.Issue
import validate.Validations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Pins the malformed-payload contract for named extensions (ADR-0005: "a payload under
 * a declared key that does not match the module's shape is reported by the new
 * validators"): [getNamed] stays strict and throws, [getNamedList] is the
 * validator-safe path, [MalformedNamedExtensionPayload] reports the payload,
 * and `Validations.all` never throws on converter-shaped (list-payload) documents.
 */
class NamedExtensionMalformedPayloadTest {
    private fun nodeWithIndexPayload(payload: ExtensionValue) =
        model.node.Node(label = "N").apply { extensions[IndexExtensionModule.key] = payload }

    private val indexListPayload =
        ListValue(
            mutableListOf(
                MapValue(
                    mutableMapOf(
                        "name" to StringValue("ix"),
                        "properties" to ListValue(mutableListOf(StringValue("name")))
                    )
                )
            )
        )

    @Test
    fun `getNamed on a shape-mismatched payload throws - pinned strict contract`() {
        val node = nodeWithIndexPayload(indexListPayload)

        assertFailsWith<SerializationException> { node.getNamed(IndexExtensionModule) }
    }

    @Test
    fun `getNamedList normalises a single object to a one-element list`() {
        val node =
            nodeWithIndexPayload(
                MapValue(
                    mutableMapOf(
                        "name" to StringValue("ix"),
                        "properties" to ListValue(mutableListOf(StringValue("name")))
                    )
                )
            )

        val indexes = node.getNamedList(IndexExtensionModule)

        assertEquals(1, indexes.size)
        assertEquals("ix", indexes[0].name)
    }

    @Test
    fun `getNamedList decodes a list payload element-wise`() {
        val node = nodeWithIndexPayload(indexListPayload)

        val indexes = node.getNamedList(IndexExtensionModule)

        assertEquals(1, indexes.size)
        assertEquals(listOf("name"), indexes[0].properties)
    }

    @Test
    fun `getNamedList yields an empty list on a payload decodable in neither form`() {
        val node = nodeWithIndexPayload(StringValue("not-an-index"))

        assertEquals(emptyList(), node.getNamedList(IndexExtensionModule))
    }

    @Test
    fun `MalformedNamedExtensionPayload reports a list payload under neo4j index on a node`() {
        val model =
            GraphModel(
                schema = "s",
                id = "t",
                version = 1,
                nodes =
                mutableMapOf(
                    "n" to
                        nodeWithIndexPayload(
                            ListValue(mutableListOf(StringValue("not-an-index-object")))
                        )
                )
            )
        val issues = mutableListOf<Issue>()

        MalformedNamedExtensionPayload.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("malformed_named_extension_payload", issues[0].code)
        assertEquals("nodes.n.extensions[\"neo4j:index\"]", issues[0].path)
        assertTrue(issues[0].message.contains("neo4j:index"))
    }

    @Test
    fun `MalformedNamedExtensionPayload accepts object and list-of-objects payloads`() {
        val model =
            GraphModel(
                schema = "s",
                id = "t",
                version = 1,
                nodes = mutableMapOf("n" to nodeWithIndexPayload(indexListPayload))
            )
        val issues = mutableListOf<Issue>()

        MalformedNamedExtensionPayload.validate(model, issues)

        assertEquals(emptyList(), issues)
    }

    @Test
    fun `Validations all completes without throwing on converter-style list payloads`() {
        val model =
            GraphModel(
                schema = "s",
                id = "t",
                version = 1,
                nodes = mutableMapOf("n" to nodeWithIndexPayload(indexListPayload))
            ).apply {
                extensions[TableExtensionModule.key] =
                    ListValue(
                        mutableListOf(
                            MapValue(
                                mutableMapOf(
                                    "name" to StringValue("tbl"),
                                    "source" to StringValue("local"),
                                    "columns" to MapValue(mutableMapOf())
                                )
                            )
                        )
                    )
                extensions[MappingExtensionModule.key] =
                    ListValue(
                        mutableListOf(
                            MapValue(
                                mutableMapOf(
                                    "kind" to StringValue("node"),
                                    "node" to StringValue("n"),
                                    "table" to StringValue("tbl")
                                )
                            )
                        )
                    )
            }

        val issues = model.validate(Validations.all)

        // The pin is totality (no SerializationException escapes); issues may be reported.
        assertTrue(issues.none { it.code == "malformed_named_extension_payload" })
    }
}
