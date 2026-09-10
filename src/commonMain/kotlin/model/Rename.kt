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

import model.type.Named

internal object Rename {

    /**
     * Goes through a MutableMap, replacing the keys with replacements from [renames]
     * @param parent optionally used to look up the replacement key
     */
    internal fun <T> MutableMap<String, T>.rename(renames: Map<String, String>, parent: String? = null) {
        val original = toMutableMap()
        clear()
        for ((og, value) in original) {
            val key = if (parent != null) {
                renames["$parent:$og"]
            } else {
                renames[og]
            } ?: og
            this[key] = value
        }
    }

    /**
     * Goes through a MutableSet, replacing the keys with replacements from [renames]
     * @param parent optionally used to look up the replacement key
     */
    internal fun MutableSet<String>.rename(renames: Map<String, String>, parent: String? = null) {
        val original = toMutableSet()
        clear()
        for (og in original) {
            val key = if (parent != null) {
                renames["$parent:$og"]
            } else {
                renames[og]
            } ?: og
            add(key)
        }
    }

    /**
     * Removes any [Named.name]'s and places them as the key in the MutableMap
     * @param parent name to use as a key prefix in the @return map
     * @return Map of original keys to their replacements
     */
    internal fun <T : Named> MutableMap<String, T>.prettify(parent: String? = null): Map<String, String> =
        transformKeys(
            parent = parent,
            skip = { it.name == null },
            newKey = { _, node -> node.name!! },
            updateName = { _, node -> node.name = null }
        )

    /**
     * Replaces every key in the MutableMap with a predictable stable id.
     * Pushing existing keys into [Named.name]
     *
     * @param type The type of field in use to prefix the stable id e.g: node0, node1, node2 etc...
     * @param parent The parent field type to avoid stable id conflicts in a global map node0:property1, node0:property1
     * @param idParent When set, incorporated into the generated id to make it globally unique across
     *        entities (e.g. nodeConstraint_node0_0). Use for constraints/indexes that get flattened
     *        into a global list by consumers. Omit for properties, which are scoped per entity.
     * @return Map of original keys to their replacements
     */
    internal fun <T : Named> MutableMap<String, T>.identify(
        type: String,
        parent: String? = null,
        idParent: String? = null
    ): Map<String, String> {
        var i = 0
        return transformKeys(
            parent = parent,
            skip = { it.name != null },
            newKey = { _, _ -> if (idParent != null) "${type}_${idParent}_${i++}" else "$type${i++}" },
            updateName = { og, node -> node.name = og }
        )
    }

    /**
     * Snapshots the map, clears it, then re-inserts each entry under a new key while
     * tracking old->new mappings.
     *
     * @param skip if true for a node, it's re-inserted under its original key untouched
     * @param newKey computes the replacement key for a node that isn't skipped
     * @param updateName mutates [Named.name] to reflect the swap (called after newKey
     *        is computed, so it can safely read the "old" name state first)
     */
    private inline fun <T : Named> MutableMap<String, T>.transformKeys(
        parent: String?,
        skip: (T) -> Boolean,
        newKey: (String, T) -> String,
        updateName: (String, T) -> Unit
    ): Map<String, String> {
        val original = toMutableMap()
        clear()
        val changes = mutableMapOf<String, String>()
        for ((og, node) in original) {
            if (skip(node)) {
                this[og] = node
                continue
            }
            val key = newKey(og, node)
            updateName(og, node)
            this[key] = node
            changes[if (parent != null) "$parent:$og" else og] = key
        }
        return changes
    }
}
