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
package validate.node.index

import model.GraphModel
import model.node.Node
import model.node.NodeConstraint
import model.node.NodeIndex
import model.type.ConstraintType
import validate.Issue
import validate.constraint.isCompositeConstraint
import validate.node.NodeValidation

object NodeIndexDuplicatePropertySet : NodeValidation {
    override fun validateIndex(
        model: GraphModel,
        nodeId: String,
        node: Node,
        indexId: String,
        index: NodeIndex,
        issues: MutableList<Issue>
    ) {
        if (index.properties.isEmpty()) return
        val duplicatesAnotherIndex = node.indexes.values.any { other ->
            other !== index && other.properties.toSet() == index.properties.toSet()
        }
        // UPX isCustomIndexPropertySetDuplicate: constraints imply backing
        // indexes, so an index matching a valid-for-index-view constraint's
        // property set is also a duplicate.
        val duplicatesConstraint = constraintDerivedPropertySets(node).any { it == index.properties.toSet() }
        if (duplicatesAnotherIndex || duplicatesConstraint) {
            issues.add(
                Issue(
                    code = "duplicate_index_property_set",
                    message = "Index '$indexId' property set duplicates another index on node '$nodeId'",
                    path = "nodes.$nodeId.indexes.$indexId.properties"
                )
            )
        }
    }

    // UPX isValidConstraintForIndexView: a constraint implies an index when it
    // has properties, a type other than existence, a name, and is not itself a
    // duplicate composite.
    private fun constraintDerivedPropertySets(node: Node): List<Set<String>> {
        val compositeConstraints = node.constraints.values.filter { isCompositeConstraint(it.properties) }
        return node.constraints.values
            .filterNot { constraint ->
                constraint.properties.isEmpty() ||
                    constraint.type == ConstraintType.EXISTS ||
                    constraint.name.isNullOrEmpty() ||
                    isDuplicateComposite(constraint, compositeConstraints)
            }
            .map { it.properties.toSet() }
    }

    private fun isDuplicateComposite(constraint: NodeConstraint, compositeConstraints: List<NodeConstraint>): Boolean =
        compositeConstraints.any { other ->
            other !== constraint && other.properties.toSet() == constraint.properties.toSet()
        }
}
