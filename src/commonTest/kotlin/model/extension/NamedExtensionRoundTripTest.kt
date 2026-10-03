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

import kotlinx.serialization.json.jsonObject
import model.GraphModel
import model.extension.neo4j.index.IndexExtensionModule
import model.extension.ui.display.DisplayExtensionModule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Pins the unknown-content contract for named extensions (ADR-0005, format invariant 9):
 * the raw extensions map carries unknown fields untouched; typed decode ignores unknown
 * keys; a typed re-encode drops them (typed modules are an authoring API, not a
 * pass-through - consumers needing fidelity use the raw map).
 */
class NamedExtensionRoundTripTest {
    private fun modelWithIndexPayload(payload: MutableMap<String, ExtensionValue>) = GraphModel(
        schema = "s",
        id = "t",
        version = 1,
        nodes =
        mutableMapOf(
            "n" to
                model.node.Node(label = "N").apply {
                    extensions[IndexExtensionModule.key] = MapValue(payload)
                }
        )
    )

    @Test
    fun `getNamed returns null when the key is absent`() {
        assertNull(GraphModel(schema = "s", id = "t", version = 1).getNamed(IndexExtensionModule))
    }

    @Test
    fun `the raw map carries unknown fields untouched`() {
        val model =
            modelWithIndexPayload(
                mutableMapOf(
                    "name" to StringValue("ix"),
                    "ownerField" to StringValue("owner-specific")
                )
            )

        val raw = model.nodes["n"]!!.extensions[IndexExtensionModule.key] as MapValue

        assertEquals(StringValue("owner-specific"), raw.value["ownerField"])
    }

    @Test
    fun `typed decode ignores unknown keys without failing`() {
        val model =
            modelWithIndexPayload(
                mutableMapOf(
                    "name" to StringValue("ix"),
                    "ownerField" to StringValue("owner-specific")
                )
            )

        val index = model.nodes["n"]!!.getNamed(IndexExtensionModule)

        assertNotNull(index)
        assertEquals("ix", index.name)
    }

    @Test
    fun `typed re-encode drops unknown keys - pinned behaviour`() {
        val model =
            modelWithIndexPayload(
                mutableMapOf(
                    "name" to StringValue("ix"),
                    "ownerField" to StringValue("owner-specific")
                )
            )
        val index = model.nodes["n"]!!.getNamed(IndexExtensionModule)!!

        val reEncoded =
            kotlinx.serialization.json.Json.encodeToJsonElement(IndexExtensionModule.serializer, index).jsonObject

        assertEquals("ix", (reEncoded["name"] as kotlinx.serialization.json.JsonPrimitive).content)
        assertFalse(reEncoded.containsKey("ownerField"))
    }

    @Test
    fun `a payload under an undeclared key is never decoded by a module`() {
        val model =
            GraphModel(
                schema = "s",
                id = "t",
                version = 1,
                nodes =
                mutableMapOf(
                    "n" to
                        model.node.Node(label = "N").apply {
                            extensions["ui:display"] =
                                MapValue(mutableMapOf("color" to StringValue("#fff")))
                        }
                )
            )

        assertNull(model.nodes["n"]!!.getNamed(IndexExtensionModule))
        assertNotNull(model.nodes["n"]!!.getNamed(DisplayExtensionModule))
        assertTrue(model.nodes["n"]!!.extensions.containsKey("ui:display"))
    }
}
