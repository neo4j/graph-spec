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
package org.neo4j.spec.graph.model.extension.mapping

import org.neo4j.spec.graph.model.GraphModel

/**
 * Switches mappings between identifying by key columns and by key properties.
 *
 * Node mappings and relationship mappings convert their own keys using their own property mappings. Relationship
 * mappings also convert the node key of their `from_node` and `to_node` targets using the property mappings of the
 * mappings of the node at that end of the relationship. A failed conversion changes nothing.
 *
 * All conversions throw [IllegalArgumentException] if a key can't be converted, e.g. a column isn't mapped to a
 * property.
 */
object MappingKeyConversion {
    /** Converts the keys of [mapping], which must be one of [model]'s mappings, to key properties. */
    fun useKeyProperties(model: GraphModel, mapping: Mapping) = planKeyConversion(mapping, context(model), true)()

    /** Converts the keys of [mapping], which must be one of [model]'s mappings, to key columns. */
    fun useKeyColumns(model: GraphModel, mapping: Mapping) = planKeyConversion(mapping, context(model), false)()

    /** Converts the keys of every mapping in [model] to key properties. */
    fun useKeyProperties(model: GraphModel) = convertAll(model, true)

    /** Converts the keys of every mapping in [model] to key columns. */
    fun useKeyColumns(model: GraphModel) = convertAll(model, false)

    private fun convertAll(model: GraphModel, toProperties: Boolean) {
        val context = context(model)
        model.extensions.mappings.values.flatten()
            .map { planKeyConversion(it, context, toProperties) }
            .forEach { it() }
    }

    private fun context(model: GraphModel) = KeyConversionContext(
        endpoints = { id -> model.relationships[id]?.let { it.from.node to it.to.node } },
        nodePropertyMappings = { nodeId ->
            model.extensions.mappings.values.flatten()
                .filterIsInstance<NodeMapping>()
                .filter { it.node == nodeId }
                .map { it.properties }
        }
    )
}
