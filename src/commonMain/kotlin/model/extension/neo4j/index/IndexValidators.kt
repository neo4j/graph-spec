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
package model.extension.neo4j.index

import model.GraphModel
import model.extension.getNamedList
import validate.Issue
import validate.Validation
import validate.constraintPath
import validate.extensionPath
import validate.forEachExtensionLevel
import validate.forEachNode
import validate.forEachRelationship

/**
 * `neo4j:index` rule (ports 4.0.0's NodeIndexesExists/NodeIndexProperties and their
 * relationship twins): every property an index covers must be declared on the element
 * carrying the index — at property level, on the enclosing element. Root-level
 * payloads are skipped: the root has no element properties to resolve against, so
 * root indexes get options/type coherence from [IndexOptionsMatch] only. Extension
 * payloads are not schema-validated, so this is the model-level check.
 */
object IndexPropertyReferences : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachExtensionLevel { path, owner, elementProperties ->
            if (elementProperties == null) return@forEachExtensionLevel // root: nothing to resolve against
            owner.getNamedList(IndexExtensionModule).forEach { index ->
                index.properties.forEach { property ->
                    if (!elementProperties.containsKey(property)) {
                        issues.add(
                            Issue(
                                code = "missing_index_property",
                                message = "Missing property '$property' for index " +
                                    "'${index.name ?: "neo4j:index"}' at '$path'",
                                path = "${extensionPath(path, IndexExtensionModule.key)}.properties.$property"
                            )
                        )
                    }
                }
            }
        }
    }
}

/**
 * `neo4j:index` rule (ports 4.0.0's NodeIndexOptions/RelationshipIndexOptions): an
 * options group is only meaningful with the matching index `type` — `fulltext` options
 * on a `vector` index and so on are flagged. Runs at every placement level (root,
 * node, relationship, property): options/type coherence needs no element scope.
 */
object IndexOptionsMatch : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        fun check(path: String, index: IndexExtension, issues: MutableList<Issue>) {
            val options = index.options ?: return
            val type = index.type ?: return
            val mismatched =
                buildList {
                    if (options.fulltext != null && type != IndexType.FULLTEXT) add("fulltext")
                    if (options.point != null && type != IndexType.POINT) add("point")
                    if (options.vector != null && type != IndexType.VECTOR) add("vector")
                }
            mismatched.forEach { group ->
                issues.add(
                    Issue(
                        code = "index_options_type_mismatch",
                        message = "Index '${index.name ?: "neo4j:index"}' has type '$type' but carries $group options",
                        path = "${extensionPath(path, IndexExtensionModule.key)}.options.$group"
                    )
                )
            }
        }
        model.forEachExtensionLevel { path, owner, _ ->
            owner.getNamedList(IndexExtensionModule).forEach { check(path, it, issues) }
        }
    }
}

/**
 * `neo4j:index` rule (ports 4.0.0's NodeIndexConstraintNameConflict): index names and
 * constraint names share one namespace per model — a name used by both (or twice) is
 * ambiguous for tooling. Index names are collected at every placement level (root,
 * node, relationship, property).
 */
object IndexConstraintNameConflict : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        val seen = mutableMapOf<String, String>() // name -> first path
        fun offer(path: String, name: String?, issues: MutableList<Issue>) {
            if (name.isNullOrEmpty()) return
            val first = seen[name]
            if (first == null) {
                seen[name] = path
            } else {
                issues.add(
                    Issue(
                        code = "duplicate_index_constraint_name",
                        message = "Name '$name' at '$path' is already used by an index or constraint at '$first'",
                        path = path
                    )
                )
            }
        }
        model.forEachExtensionLevel { path, owner, _ ->
            owner.getNamedList(IndexExtensionModule).forEach { index ->
                offer(extensionPath(path, IndexExtensionModule.key), index.name, issues)
            }
        }
        model.forEachNode { path, _, node ->
            node.constraints.forEachIndexed { i, constraint ->
                offer("${constraintPath(path, i)}.name", constraint.name, issues)
            }
        }
        model.forEachRelationship { path, _, relationship ->
            relationship.constraints.forEachIndexed { i, constraint ->
                offer("${constraintPath(path, i)}.name", constraint.name, issues)
            }
        }
    }
}
