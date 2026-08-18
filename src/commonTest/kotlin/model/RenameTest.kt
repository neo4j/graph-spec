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
package model

import model.Rename.identify
import model.Rename.prettify
import model.Rename.rename
import model.node.Node
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class RenameTest {

    // ---------------------------------------------------------------------
    // MutableMap<String, T>.rename
    // ---------------------------------------------------------------------

    @Test
    fun `map rename replaces keys found in renames map`() {
        val map = mutableMapOf("a" to 1, "b" to 2)
        map.rename(mapOf("a" to "x"))
        assertEquals(mapOf("x" to 1, "b" to 2), map)
    }

    @Test
    fun `map rename leaves keys unchanged when no matching rename exists`() {
        val map = mutableMapOf("a" to 1, "b" to 2)
        map.rename(mapOf("c" to "z"))
        assertEquals(mapOf("a" to 1, "b" to 2), map)
    }

    @Test
    fun `map rename with parent looks up parent-prefixed key`() {
        val map = mutableMapOf("a" to 1)
        map.rename(mapOf("parent:a" to "x"), parent = "parent")
        assertEquals(mapOf("x" to 1), map)
    }

    @Test
    fun `map rename with parent falls back to original key not the parent-prefixed one when missing`() {
        val map = mutableMapOf("a" to 1)
        // Even though renames has an entry for plain "a", the parent-scoped
        // lookup only ever checks "parent:a" - the plain-key entry must be ignored.
        map.rename(mapOf("a" to "shouldNotBeUsed"), parent = "parent")
        assertEquals(mapOf("a" to 1), map)
    }

    @Test
    fun `map rename on empty map is a no-op`() {
        val map = mutableMapOf<String, Int>()
        map.rename(mapOf("a" to "b"))
        assertTrue(map.isEmpty())
    }

    @Test
    fun `map rename with empty renames leaves map unchanged`() {
        val map = mutableMapOf("a" to 1, "b" to 2)
        map.rename(emptyMap())
        assertEquals(mapOf("a" to 1, "b" to 2), map)
    }

    @Test
    fun `map rename collision keeps only the last processed entry`() {
        val map = linkedMapOf("a" to 1, "b" to 2)
        // Both "a" and "b" are renamed to "x" - later entry in iteration order wins.
        map.rename(mapOf("a" to "x", "b" to "x"))
        assertEquals(1, map.size)
        assertEquals(2, map["x"])
    }

    // ---------------------------------------------------------------------
    // MutableSet<String>.rename
    // ---------------------------------------------------------------------

    @Test
    fun `set rename replaces values found in renames map`() {
        val set = mutableSetOf("a", "b")
        set.rename(mapOf("a" to "x"))
        assertEquals(setOf("x", "b"), set)
    }

    @Test
    fun `set rename leaves values unchanged when no matching rename exists`() {
        val set = mutableSetOf("a", "b")
        set.rename(mapOf("c" to "z"))
        assertEquals(setOf("a", "b"), set)
    }

    @Test
    fun `set rename with parent looks up parent-prefixed key`() {
        val set = mutableSetOf("a")
        set.rename(mapOf("parent:a" to "x"), parent = "parent")
        assertEquals(setOf("x"), set)
    }

    @Test
    fun `set rename with parent falls back to original value when parent-prefixed key missing`() {
        val set = mutableSetOf("a")
        set.rename(mapOf("a" to "shouldNotBeUsed"), parent = "parent")
        assertEquals(setOf("a"), set)
    }

    @Test
    fun `set rename collision collapses to a single value`() {
        val set = mutableSetOf("a", "b")
        set.rename(mapOf("a" to "x", "b" to "x"))
        assertEquals(setOf("x"), set)
    }

    @Test
    fun `set rename on empty set is a no-op`() {
        val set = mutableSetOf<String>()
        set.rename(mapOf("a" to "b"))
        assertTrue(set.isEmpty())
    }

    // ---------------------------------------------------------------------
    // MutableMap<String, T : Named>.prettify
    // ---------------------------------------------------------------------

    @Test
    fun `prettify moves a named node under its name and clears the name field`() {
        val node = Node(name = "friendly")
        val map = mutableMapOf("og1" to node)

        val changes = map.prettify()

        assertEquals(setOf("friendly"), map.keys)
        assertNull(map.getValue("friendly").name)
        assertEquals(mapOf("og1" to "friendly"), changes)
    }

    @Test
    fun `prettify leaves a node with a null name under its original key and records no change`() {
        val node = Node(name = null)
        val map = mutableMapOf("og1" to node)

        val changes = map.prettify()

        assertEquals(setOf("og1"), map.keys)
        assertTrue(changes.isEmpty())
    }

    @Test
    fun `prettify records a parent-prefixed original key in the changes map`() {
        val node = Node(name = "friendly")
        val map = mutableMapOf("og1" to node)

        val changes = map.prettify(parent = "parent")

        assertEquals(mapOf("parent:og1" to "friendly"), changes)
        assertEquals(setOf("friendly"), map.keys)
    }

    @Test
    fun `prettify handles a mix of named and unnamed nodes independently`() {
        val named = Node(name = "friendly")
        val unnamed = Node(name = null)
        val map = mutableMapOf("og1" to named, "og2" to unnamed)

        val changes = map.prettify()

        assertEquals(setOf("friendly", "og2"), map.keys)
        assertEquals(mapOf("og1" to "friendly"), changes)
        assertNull(map.getValue("friendly").name)
        assertNull(map.getValue("og2").name)
    }

    // ---------------------------------------------------------------------
    // MutableMap<String, T : Named>.identify
    // ---------------------------------------------------------------------

    @Test
    fun `identify assigns a stable id to an unnamed node and stores the original key as its name`() {
        val node = Node(name = null)
        val map = mutableMapOf("og1" to node)

        val changes = map.identify(type = "node")

        assertEquals(setOf("node0"), map.keys)
        assertEquals("og1", map.getValue("node0").name)
        assertEquals(mapOf("og1" to "node0"), changes)
    }

    @Test
    fun `identify leaves an already-named node untouched under its original key`() {
        val node = Node(name = "alreadyNamed")
        val map = mutableMapOf("og1" to node)

        val changes = map.identify(type = "node")

        assertEquals(setOf("og1"), map.keys)
        assertEquals("alreadyNamed", map.getValue("og1").name)
        assertTrue(changes.isEmpty())
    }

    @Test
    fun `identify counter only increments for nodes that actually receive a new id`() {
        val alreadyNamed = Node(name = "keepMe")
        val unnamed1 = Node(name = null)
        val unnamed2 = Node(name = null)
        val map = linkedMapOf(
            "og1" to alreadyNamed,
            "og2" to unnamed1,
            "og3" to unnamed2
        )

        val changes = map.identify(type = "node")

        // The already-named entry must not consume a counter slot.
        assertEquals(setOf("og1", "node0", "node1"), map.keys)
        assertEquals("og2", map.getValue("node0").name)
        assertEquals("og3", map.getValue("node1").name)
        assertEquals(mapOf("og2" to "node0", "og3" to "node1"), changes)
    }

    @Test
    fun `identify records a parent-prefixed original key in the changes map`() {
        val node = Node(name = null)
        val map = mutableMapOf("og1" to node)

        val changes = map.identify(type = "node", parent = "parent")

        assertEquals(mapOf("parent:og1" to "node0"), changes)
        assertEquals("og1", map.getValue("node0").name)
    }

    @Test
    fun `identify on an empty map produces no changes and stays empty`() {
        val map = mutableMapOf<String, Node>()
        val changes = map.identify(type = "node")
        assertTrue(map.isEmpty())
        assertTrue(changes.isEmpty())
    }
}
