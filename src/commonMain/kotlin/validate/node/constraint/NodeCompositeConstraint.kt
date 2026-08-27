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
import validate.Issue
import validate.constraint.isDraftCompositeConstraint
import validate.node.NodeValidation

object NodeCompositeConstraint : NodeValidation {
    override fun validateConstraint(
        model: GraphModel,
        nodeId: String,
        node: Node,
        constraintId: String,
        constraint: NodeConstraint,
        issues: MutableList<Issue>
    ) {
        if (isDraftCompositeConstraint(constraint.properties, constraint.name)) {
            issues.add(
                Issue(
                    code = "invalid_node_composite_constraint",
                    message = "Composite constraint '$constraintId' on node '$nodeId' requires at least 2 properties",
                    path = "nodes.$nodeId.constraints.$constraintId.properties"
                )
            )
        }
    }
}
