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
package org.neo4j.spec.graph.v1.validate.mapping

import org.neo4j.spec.graph.v1.model.GraphModel
import org.neo4j.spec.graph.v1.model.extension.mapping.Mapping
import org.neo4j.spec.graph.v1.model.extension.mapping.NodeMapping
import org.neo4j.spec.graph.v1.model.extension.mapping.QueryMapping
import org.neo4j.spec.graph.v1.model.extension.mapping.RelationshipMapping
import org.neo4j.spec.graph.v1.validate.Issue
import org.neo4j.spec.graph.v1.validate.Validation

/**
 * A mapping's `depends_on` must reference the `id` of another mapping, and mapping ids must be unique and
 * free of dependency cycles so an import order can be derived.
 */
object MappingDependencies : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        val mappings = model.extensions.mappings
        val indexById = mutableMapOf<String, Int>()
        for ((index, mapping) in mappings.withIndex()) {
            val id = mapping.mappingId()
            if (id.isEmpty()) {
                continue
            }
            if (id in indexById) {
                issues.add(
                    Issue(
                        code = "mapping_id_duplicate",
                        message = "Mapping id '$id' is used by more than one mapping",
                        path = "extensions.mappings[$index].id"
                    )
                )
            } else {
                indexById[id] = index
            }
        }

        // Each mapping depends on at most one other, so follow the chain from every mapping looking for a loop.
        val next = mutableMapOf<Int, Int>()
        for ((index, mapping) in mappings.withIndex()) {
            val dependsOn = mapping.mappingDependsOn()
            if (dependsOn.isEmpty()) {
                continue
            }
            val target = indexById[dependsOn]
            val path = "extensions.mappings[$index].depends_on"
            when (target) {
                null -> issues.add(
                    Issue(
                        code = "mapping_depends_on_unknown",
                        message = "Mapping depends on '$dependsOn' which isn't the id of any mapping",
                        path = path
                    )
                )
                index -> issues.add(
                    Issue(
                        code = "mapping_depends_on_self",
                        message = "Mapping '$dependsOn' can't depend on itself",
                        path = path
                    )
                )
                else -> next[index] = target
            }
        }
        reportCycles(next, issues)
    }

    private fun reportCycles(next: Map<Int, Int>, issues: MutableList<Issue>) {
        val done = mutableSetOf<Int>()
        for (start in next.keys.sorted()) {
            if (start in done) {
                continue
            }
            val path = mutableListOf<Int>()
            var current: Int? = start
            while (current != null && current !in done && current !in path) {
                path.add(current)
                current = next[current]
            }
            if (current != null && current in path) {
                val cycle = path.subList(path.indexOf(current), path.size)
                val first = cycle.min()
                issues.add(
                    Issue(
                        code = "mapping_dependency_cycle",
                        message = "Mappings ${cycle.sorted().joinToString(", ") {
                            "[$it]"
                        }} depend on each other in a cycle",
                        path = "extensions.mappings[$first].depends_on"
                    )
                )
            }
            done.addAll(path)
        }
    }

    private fun Mapping.mappingId() = when (this) {
        is NodeMapping -> id
        is RelationshipMapping -> id
        is QueryMapping -> id
    }

    private fun Mapping.mappingDependsOn() = when (this) {
        is NodeMapping -> dependsOn
        is RelationshipMapping -> dependsOn
        is QueryMapping -> dependsOn
    }
}
