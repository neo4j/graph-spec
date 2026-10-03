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
package migrate.migration.graphSpec

import codec.schema.SchemaMap

/**
 * The ADR-0008 §2 identity normaliser. 4.0.0 documents come in two serialisations of
 * the same model — pretty (map keys are labels/type/property names) and internal
 * (keys are `node0`/`nodeProperty0`/…, the readable name in `name`) — and this one
 * rule covers both forms and hand-written hybrids with no detection step: the v1 id
 * of a node, relationship, or property is its `name` field if present, else its map
 * key. References that do not resolve through the maps are carried unchanged.
 */
internal class GraphSpecV4Normalizer(schema: SchemaMap) {

    /** Old node map key -> v1 node id. */
    val nodeIds: Map<String, String> = idMap(schema.mapOfMapsOrNull("nodes"), "nodes")

    /** Old relationship map key -> v1 relationship id. */
    val relationshipIds: Map<String, String> = idMap(schema.mapOfMapsOrNull("relationships"), "relationships")

    /** Old node map key -> (old property key -> v1 property id). */
    val nodePropertyIds: Map<String, Map<String, String>> =
        propertyIdMaps(schema.mapOfMapsOrNull("nodes"), "nodes")

    /** Old relationship map key -> (old property key -> v1 property id). */
    val relationshipPropertyIds: Map<String, Map<String, String>> =
        propertyIdMaps(schema.mapOfMapsOrNull("relationships"), "relationships")

    /**
     * 4.0.0 identifier label -> v1 node id, for index `labels` fan-out (ADR-0008 §8).
     * Only explicit labels land here (the v1 label rule's `?: the resolved id` fallback
     * is already covered by [nodeIds]); a label shared by several nodes is ambiguous
     * and excluded.
     */
    val nodeIdsByLabel: Map<String, String> = labelIdMap(schema.mapOfMapsOrNull("nodes"))

    fun nodeId(reference: String): String = nodeIds[reference] ?: reference

    fun relationshipId(reference: String): String = relationshipIds[reference] ?: reference

    fun nodePropertyId(nodeKey: String, reference: String): String =
        nodePropertyIds[nodeKey]?.get(reference) ?: reference

    fun relationshipPropertyId(relationshipKey: String, reference: String): String =
        relationshipPropertyIds[relationshipKey]?.get(reference) ?: reference

    /**
     * Resolves a 4.0.0 index `labels` entry to a v1 node id: identifier label first
     * (internal form), then the §2 key map (pretty form, where the key is the label);
     * null when neither knows it.
     */
    fun nodeIdForLabel(label: String): String? = nodeIdsByLabel[label] ?: nodeIds[label]

    companion object {
        /** The v1 id of an element: its `name` field if present, else its map key. */
        fun idOf(key: String, element: SchemaMap): String = element.stringOrNull("name") ?: key

        private fun idMap(elements: Map<String, SchemaMap>?, owner: String): Map<String, String> {
            val ids = LinkedHashMap<String, String>()
            for ((key, element) in elements.orEmpty()) {
                val id = idOf(key, element)
                require(!ids.containsValue(id)) {
                    "4.0.0 $owner id collision: '$key' and another entry both resolve to '$id'"
                }
                ids[key] = id
            }
            return ids
        }

        private fun propertyIdMaps(elements: Map<String, SchemaMap>?, owner: String): Map<String, Map<String, String>> =
            elements.orEmpty().mapValues { (key, element) ->
                idMap(element.mapOfMapsOrNull("properties"), "$owner.$key.properties")
            }

        private fun labelIdMap(nodes: Map<String, SchemaMap>?): Map<String, String> {
            val byLabel = LinkedHashMap<String, String>()
            val ambiguous = mutableSetOf<String>()
            for ((key, node) in nodes.orEmpty()) {
                val label = node.mapOrNull("labels")?.stringOrNull("identifier")
                    ?: node.stringOrNull("label")
                    ?: continue
                val id = idOf(key, node)
                if (byLabel.containsKey(label) && byLabel[label] != id) {
                    ambiguous.add(label)
                } else {
                    byLabel[label] = id
                }
            }
            byLabel.keys.removeAll(ambiguous)
            return byLabel
        }
    }
}
