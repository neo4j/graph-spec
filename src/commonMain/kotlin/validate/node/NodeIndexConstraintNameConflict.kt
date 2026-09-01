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

/**
 * Ported from UPX `getNonUniqueNames` (import-shared/src/data-model/errors.ts).
 *
 * A name may be reused only when the whole definition is identical (same properties, label, and
 * index/constraint type). This supports mapping multiple files or columns to the same node.
 */
object NodeIndexConstraintNameConflict : NodeValidation {
    override fun validateNode(model: GraphModel, nodeId: String, node: Node, issues: MutableList<Issue>) {
        val conflicting = conflictingNames(model)

        for ((indexId, index) in node.indexes) {
            val name = index.name ?: continue
            if (name in conflicting) {
                issues.add(
                    Issue(
                        code = "duplicate_index_constraint_name",
                        message = "Index '$indexId' on node '$nodeId' reuses name '$name' " +
                            "for a different index or constraint definition",
                        path = "nodes.$nodeId.indexes.$indexId.name"
                    )
                )
            }
        }

        for ((constraintId, constraint) in node.constraints) {
            val name = constraint.name ?: continue
            if (name in conflicting) {
                issues.add(
                    Issue(
                        code = "duplicate_index_constraint_name",
                        message = "Constraint '$constraintId' on node '$nodeId' reuses name '$name' " +
                            "for a different index or constraint definition",
                        path = "nodes.$nodeId.constraints.$constraintId.name"
                    )
                )
            }
        }
    }

    private fun conflictingNames(model: GraphModel): Set<String> {
        val definitionsByName = mutableMapOf<String, MutableSet<String>>()
        for (node in model.nodes.values) {
            for (index in node.indexes.values) {
                val name = index.name
                if (name.isNullOrEmpty()) continue
                definitionsByName.getOrPut(name) { mutableSetOf() }
                    .add(definition("index", index.properties, index.labels, index.type.name))
            }
            for (constraint in node.constraints.values) {
                val name = constraint.name
                if (name.isNullOrEmpty()) continue
                definitionsByName.getOrPut(name) { mutableSetOf() }
                    .add(
                        definition(
                            "constraint",
                            constraint.properties,
                            setOfNotNull(constraint.label),
                            constraint.type.name
                        )
                    )
            }
        }
        // An index and a constraint sharing a name always conflict, hence the kind in the definition.
        return definitionsByName.filterValues { it.size > 1 }.keys
    }

    private fun definition(kind: String, properties: Set<String>, labels: Set<String>, type: String): String =
        "$kind:${properties.sorted().joinToString("_")}:${labels.sorted().joinToString("_")}:$type"
}
