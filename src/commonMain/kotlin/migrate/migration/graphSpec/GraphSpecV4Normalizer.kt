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

    fun nodeId(reference: String): String = nodeIds[reference] ?: reference

    fun relationshipId(reference: String): String = relationshipIds[reference] ?: reference

    fun nodePropertyId(nodeKey: String, reference: String): String =
        nodePropertyIds[nodeKey]?.get(reference) ?: reference

    fun relationshipPropertyId(relationshipKey: String, reference: String): String =
        relationshipPropertyIds[relationshipKey]?.get(reference) ?: reference

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
    }
}
