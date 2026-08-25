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
import validate.Issue

object NodeConstraintNameConflict : NodeValidation {
    override fun validateNode(model: GraphModel, nodeId: String, node: Node, issues: MutableList<Issue>) {
        val nameToKey = mutableMapOf<String, MutableList<String>>()
        for ((constraintId, constraint) in node.constraints) {
            val name = constraint.name ?: continue
            val key = createPropertyLabelTypeKey(constraint.properties, nodeId, constraint.type.name)
            nameToKey.getOrPut(name) { mutableListOf() }.add(key)
        }
        for ((indexId, index) in node.indexes) {
            val name = index.name ?: continue
            val key = createPropertyLabelTypeKey(index.properties, nodeId, index.type.name)
            nameToKey.getOrPut(name) { mutableListOf() }.add(key)
        }

        val namesWithError = mutableSetOf<String>()
        for ((name, keys) in nameToKey) {
            if (keys.size > 1 && keys.toSet().size > 1) {
                namesWithError.add(name)
            }
        }

        for ((constraintId, constraint) in node.constraints) {
            val name = constraint.name ?: continue
            if (name in namesWithError) {
                issues.add(
                    Issue(
                        code = "duplicate_node_constraint_name",
                        message = "Constraint '$constraintId' name '$name' conflicts with another index/constraint",
                        path = "nodes.$nodeId.constraints.$constraintId"
                    )
                )
            }
        }
        for ((indexId, index) in node.indexes) {
            val name = index.name ?: continue
            if (name in namesWithError) {
                issues.add(
                    Issue(
                        code = "duplicate_node_index_name",
                        message = "Index '$indexId' name '$name' conflicts with another index/constraint",
                        path = "nodes.$nodeId.indexes.$indexId"
                    )
                )
            }
        }
    }

    private fun createPropertyLabelTypeKey(properties: Set<String>, nodeId: String, type: String): String {
        val propertyNames = properties.sorted().joinToString("_")
        return "${propertyNames}_${nodeId}_$type"
    }
}
