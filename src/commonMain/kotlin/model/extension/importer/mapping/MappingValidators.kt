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
package model.extension.importer.mapping

import model.GraphModel
import model.extension.getNamedList
import model.extension.importer.table.TableExtensionModule
import validate.Issue
import validate.Validation
import validate.extensionPath

/**
 * `importer:mapping` rule: the mapping's references resolve — `node` and
 * `relationship` point at ids in the document's nodes/relationships maps, `table`
 * names a declared `importer:table`, and endpoint targets point at node ids.
 */
object MappingReferences : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        val declaredTables = model.getNamedList(TableExtensionModule).mapNotNull { it.name }
        model.forEachMapping { path, mapping ->
            mapping.node?.let { nodeId ->
                if (!model.nodes.containsKey(nodeId)) {
                    issues.add(
                        Issue(
                            code = "mapping_unknown_node",
                            message = "Mapping references unknown node '$nodeId'",
                            path = "$path.node"
                        )
                    )
                }
            }
            mapping.relationship?.let { relationshipId ->
                if (!model.relationships.containsKey(relationshipId)) {
                    issues.add(
                        Issue(
                            code = "mapping_unknown_relationship",
                            message = "Mapping references unknown relationship '$relationshipId'",
                            path = "$path.relationship"
                        )
                    )
                }
            }
            mapping.table?.let { tableName ->
                if (declaredTables.isNotEmpty() && tableName !in declaredTables) {
                    issues.add(
                        Issue(
                            code = "mapping_unknown_table",
                            message = "Mapping references table '$tableName' but the declared " +
                                "importer:table names are ${declaredTables.joinToString(", ") { "'$it'" }}",
                            path = "$path.table"
                        )
                    )
                }
            }
            listOfNotNull(mapping.startNode, mapping.endNode).forEach { target ->
                if (!model.nodes.containsKey(target.node)) {
                    issues.add(
                        Issue(
                            code = "mapping_unknown_node",
                            message = "Mapping endpoint references unknown node '${target.node}'",
                            path = path
                        )
                    )
                }
            }
        }
    }
}

/**
 * `importer:mapping` rule (ports 4.0.0's NodeMappingKey): a node mapping without
 * a `key` cannot merge rows deterministically.
 */
object MappingKeyPresent : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachMapping { path, mapping ->
            if (mapping.kind == MappingKind.NODE && mapping.key.isEmpty()) {
                issues.add(
                    Issue(
                        code = "missing_node_mapping_key",
                        message = "Node mapping '${mapping.node ?: path}' has no mapping key defined",
                        path = "$path.key"
                    )
                )
            }
        }
    }
}

/**
 * `importer:mapping` rule (ports 4.0.0's NodeMappingKeyType): a node mapping's
 * key properties must be STRING or INTEGER tokens on the referenced node — the two
 * types keying supports.
 */
object MappingKeyType : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachMapping { path, mapping ->
            if (mapping.kind != MappingKind.NODE) return@forEachMapping
            val node = model.nodes[mapping.node] ?: return@forEachMapping
            mapping.key.forEach { propertyId ->
                val property = node.properties[propertyId] ?: return@forEach
                if (property.type != "STRING" && property.type != "INTEGER") {
                    issues.add(
                        Issue(
                            code = "invalid_node_mapping_key_type",
                            message = "Mapping key '$propertyId' on node '${mapping.node}' must be STRING or INTEGER",
                            path = "$path.key.$propertyId"
                        )
                    )
                }
            }
        }
    }
}

/**
 * Root-level walk of `importer:mapping` payloads (top-level placement per ADR-0005).
 * Object-or-list aware: the 4.0.0 converter emits list payloads, so every element
 * is visited; per-element issue paths stay at the extension-key base (the same
 * choice the index validators make for list payloads).
 */
internal fun GraphModel.forEachMapping(action: (path: String, mapping: MappingExtension) -> Unit) {
    val path = extensionPath("", MappingExtensionModule.key)
    getNamedList(MappingExtensionModule).forEach { action(path, it) }
}
