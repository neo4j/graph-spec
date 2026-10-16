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
package org.neo4j.spec.graph.validate.node

import org.neo4j.spec.graph.model.GraphModel
import org.neo4j.spec.graph.model.extension.mapping.NodeMapping

internal data class LocatedNodeMapping(val mappingId: String, val index: Int, val mapping: NodeMapping)

/**
 * Finds every mapping of [nodeId] along with where each lives in the model's mappings.
 */
internal fun findNodeMappings(model: GraphModel, nodeId: String): List<LocatedNodeMapping> {
    val found = mutableListOf<LocatedNodeMapping>()
    for ((mappingId, mappings) in model.extensions.mappings) {
        for ((index, mapping) in mappings.withIndex()) {
            if (mapping is NodeMapping && mapping.node == nodeId) {
                found.add(LocatedNodeMapping(mappingId, index, mapping))
            }
        }
    }
    return found
}
