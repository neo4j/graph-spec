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
import model.type.ConstraintType
import validate.Issue
import validate.node.NodeValidation

object NodeConstraintDuplicatePropertySet : NodeValidation {
    override fun validateNode(model: GraphModel, nodeId: String, node: Node, issues: MutableList<Issue>) {
        val compositeConstraints = node.constraints.values.filter { it.properties.size >= 2 }
        for ((constraintId, constraint) in node.constraints) {
            if (constraint.properties.size < 2) continue
            val isDuplicate = compositeConstraints.any { other ->
                other !== constraint && other.properties.toSet() == constraint.properties.toSet()
            }
            if (isDuplicate) {
                issues.add(
                    Issue(
                        code = "duplicate_node_constraint_property_set",
                        message = "Constraint '$constraintId' property set duplicates " +
                            "another composite constraint on node '$nodeId'",
                        path = "nodes.$nodeId.constraints.$constraintId.properties"
                    )
                )
            }
        }
    }
}
