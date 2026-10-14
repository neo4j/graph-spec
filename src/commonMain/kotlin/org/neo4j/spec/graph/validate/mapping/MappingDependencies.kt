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
package org.neo4j.spec.graph.validate.mapping

import org.neo4j.spec.graph.model.GraphModel
import org.neo4j.spec.graph.model.extension.mapping.Mapping
import org.neo4j.spec.graph.model.extension.mapping.NodeMapping
import org.neo4j.spec.graph.model.extension.mapping.QueryMapping
import org.neo4j.spec.graph.model.extension.mapping.RelationshipMapping
import org.neo4j.spec.graph.validate.Issue
import org.neo4j.spec.graph.validate.Validation

/**
 * A mapping's `depends_on` must reference the id of another mapping group, and groups must be free of dependency
 * cycles so an import order can be derived.
 */
object MappingDependencies : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        val groups = model.extensions.mappings

        // Edges between groups, a group depends on every group any of its mappings depend on.
        val next = mutableMapOf<String, MutableSet<String>>()
        for ((groupId, mappings) in groups) {
            for ((index, mapping) in mappings.withIndex()) {
                val dependsOn = mapping.mappingDependsOn()
                if (dependsOn.isEmpty()) {
                    continue
                }
                val path = "extensions.mappings.$groupId[$index].depends_on"
                when {
                    dependsOn == groupId -> issues.add(
                        Issue(
                            code = "mapping_depends_on_self",
                            message = "Mapping '$dependsOn' can't depend on itself",
                            path = path
                        )
                    )
                    dependsOn !in groups -> issues.add(
                        Issue(
                            code = "mapping_depends_on_unknown",
                            message = "Mapping depends on '$dependsOn' which isn't the id of any mapping",
                            path = path
                        )
                    )
                    else -> next.getOrPut(groupId) { mutableSetOf() }.add(dependsOn)
                }
            }
        }
        reportCycles(next, issues)
    }

    private fun reportCycles(next: Map<String, Set<String>>, issues: MutableList<Issue>) {
        val done = mutableSetOf<String>()
        val reported = mutableSetOf<Set<String>>()

        fun visit(node: String, path: MutableList<String>) {
            if (node in path) {
                val cycle = path.subList(path.indexOf(node), path.size)
                if (reported.add(cycle.toSet())) {
                    val first = cycle.min()
                    issues.add(
                        Issue(
                            code = "mapping_dependency_cycle",
                            message = "Mappings ${cycle.sorted().joinToString(", ") {
                                "'$it'"
                            }} depend on each other in a cycle",
                            path = "extensions.mappings.$first"
                        )
                    )
                }
                return
            }
            if (node in done) {
                return
            }
            path.add(node)
            for (target in next[node].orEmpty()) {
                visit(target, path)
            }
            path.removeAt(path.size - 1)
            done.add(node)
        }

        for (start in next.keys) {
            visit(start, mutableListOf())
        }
    }

    private fun Mapping.mappingDependsOn() = when (this) {
        is NodeMapping -> dependsOn
        is RelationshipMapping -> dependsOn
        is QueryMapping -> dependsOn
    }
}
