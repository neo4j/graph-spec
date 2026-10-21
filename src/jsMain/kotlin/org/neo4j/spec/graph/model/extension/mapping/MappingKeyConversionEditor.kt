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

import org.neo4j.spec.graph.model.GraphModelJs
import org.neo4j.spec.graph.model.toMap

/**
 * Switches mappings between identifying by key columns and by key properties, see [MappingKeyConversion].
 *
 * Mappings are changed in place, and a failed conversion (an `IllegalArgumentException`) changes nothing.
 */
@JsExport
class MappingKeyConversionEditor {
    companion object {
        /** Converts the keys of [mapping], which must be one of [model]'s mappings, to key properties. */
        @JsStatic
        fun useKeyProperties(model: GraphModelJs, mapping: MappingJs) = convert(model, arrayOf(mapping), true)

        /** Converts the keys of [mapping], which must be one of [model]'s mappings, to key columns. */
        @JsStatic
        fun useKeyColumns(model: GraphModelJs, mapping: MappingJs) = convert(model, arrayOf(mapping), false)

        /** Converts the keys of every mapping in [model] to key properties. */
        @JsStatic
        fun useAllKeyProperties(model: GraphModelJs) = convert(model, allMappings(model), true)

        /** Converts the keys of every mapping in [model] to key columns. */
        @JsStatic
        fun useAllKeyColumns(model: GraphModelJs) = convert(model, allMappings(model), false)

        private fun allMappings(model: GraphModelJs): Array<MappingJs> =
            model.extensions.mappings.toMap().values.flatMap { it.toList() }.toTypedArray()

        private fun convert(model: GraphModelJs, mappings: Array<MappingJs>, toProperties: Boolean) {
            val context = context(model)
            val converted = mappings.map { it to it.toClass() }
            val plans = converted.map { (_, mapping) -> planKeyConversion(mapping, context, toProperties) }
            plans.forEach { it() }
            converted.forEach { (js, mapping) -> copyKeys(mapping, js) }
        }

        private fun context(model: GraphModelJs): KeyConversionContext {
            val relationships = model.relationships.toMap()
            val mappings = allMappings(model).filter { it.type == MappingType.NODE }.map { it.unsafeCast<NodeMappingJs>() }
            return KeyConversionContext(
                endpoints = { id -> relationships[id]?.let { it.from.node to it.to.node } },
                nodePropertyMappings = { nodeId ->
                    mappings.filter { it.node == nodeId }.map { node ->
                        node.properties.toMap().mapValues { it.value.toClass() }
                    }
                }
            )
        }

        private fun copyKeys(mapping: Mapping, js: MappingJs) {
            when (mapping) {
                is NodeMapping -> js.unsafeCast<NodeMappingJs>().apply {
                    keyColumns = mapping.keyColumns.toTypedArray()
                    keyProperties = mapping.keyProperties.toTypedArray()
                }
                is RelationshipMapping -> js.unsafeCast<RelationshipMappingJs>().apply {
                    keyColumns = mapping.keyColumns.toTypedArray()
                    keyProperties = mapping.keyProperties.toTypedArray()
                    copyTarget(mapping.fromNode, fromNode)
                    copyTarget(mapping.toNode, toNode)
                }
                is QueryMapping -> {}
            }
        }

        private fun copyTarget(target: TargetMapping, js: TargetMappingJs) {
            js.nodeKeyColumn = target.nodeKeyColumn
            js.nodeKeyProperty = target.nodeKeyProperty
        }
    }
}
