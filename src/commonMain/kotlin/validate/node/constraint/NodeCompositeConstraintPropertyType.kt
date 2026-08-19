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
package validate.node.constraint

import model.GraphModel
import model.node.Node
import model.node.NodeConstraint
import model.property.Neo4jType
import validate.Issue
import validate.node.NodeValidation

object NodeCompositeConstraintPropertyType : NodeValidation {
    override fun validateConstraint(
        model: GraphModel,
        nodeId: String,
        node: Node,
        constraintId: String,
        constraint: NodeConstraint,
        issues: MutableList<Issue>
    ) {
        val isComposite = constraint.properties.size >= 2 ||
            (constraint.properties.size == 1 && constraint.name.isNullOrBlank())
        if (!isComposite) return

        for (propertyId in constraint.properties) {
            val property = node.properties[propertyId] ?: continue
            if (property.type != Neo4jType.STRING && property.type != Neo4jType.INTEGER) {
                issues.add(
                    Issue(
                        code = "invalid_node_composite_constraint_property_type",
                        message = "Composite constraint '$constraintId' property " +
                            "'$propertyId' must be STRING or INTEGER for bulk import",
                        path = "nodes.$nodeId.constraints.$constraintId.properties.$propertyId"
                    )
                )
            }
        }
    }
}
