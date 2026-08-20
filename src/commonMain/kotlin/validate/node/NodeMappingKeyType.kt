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
package validate.node

import model.GraphModel
import model.mapping.NodeMapping
import model.node.Node
import model.property.Neo4jType
import validate.Issue

object NodeMappingKeyType : NodeValidation {
    override fun validateNode(model: GraphModel, nodeId: String, node: Node, issues: MutableList<Issue>) {
        val nodeMapping = model.mappings.filterIsInstance<NodeMapping>().find { it.node == nodeId }
        if (nodeMapping != null) {
            for (propertyId in nodeMapping.key) {
                val property = node.properties[propertyId] ?: continue
                if (property.type != Neo4jType.STRING && property.type != Neo4jType.INTEGER) {
                    issues.add(
                        Issue(
                            // Replaced legacy code: invalid_node_key_property_type
                            code = "invalid_node_mapping_key_type",
                            message = "Mapping key '$propertyId' on node '$nodeId' must be STRING or INTEGER",
                            path = "mappings.$nodeId.key.$propertyId"
                        )
                    )
                }
            }
        }
    }
}
