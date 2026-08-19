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
import model.node.Node
import model.property.Neo4jType
import validate.Issue

object NodeKeyProperty : NodeValidation {
    override fun validateNode(model: GraphModel, nodeId: String, node: Node, issues: MutableList<Issue>) {
        val hasKeyProperty = node.properties.values.any { it.key == true }
        if (!hasKeyProperty) {
            issues.add(
                Issue(
                    code = "missing_node_key_property",
                    message = "Node '$nodeId' has no key property defined",
                    path = "nodes.$nodeId.properties"
                )
            )
        }

        for ((propertyId, property) in node.properties) {
            if (property.key == true && property.type != Neo4jType.STRING && property.type != Neo4jType.INTEGER) {
                issues.add(
                    Issue(
                        code = "invalid_node_key_property_type",
                        message = "Key property '$propertyId' on node '$nodeId' must be STRING or INTEGER",
                        path = "nodes.$nodeId.properties.$propertyId"
                    )
                )
            }
        }
    }
}
