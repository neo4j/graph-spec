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
package org.neo4j.spec.graph.validate.relationship

import org.neo4j.spec.graph.model.GraphModel
import org.neo4j.spec.graph.model.extension.mapping.RelationshipMapping
import org.neo4j.spec.graph.model.extension.mapping.TargetMapping
import org.neo4j.spec.graph.model.relationship.Relationship
import org.neo4j.spec.graph.validate.Issue

/**
 * A target mapping identifies its node by either a key column or a key property, never both, and both ends of a
 * relationship mapping must use the same kind.
 */
object RelationshipMappingTargets : RelationshipValidation {
    override fun validateRelationship(
        model: GraphModel,
        relationshipId: String,
        relationship: Relationship,
        issues: MutableList<Issue>
    ) {
        for ((mappingId, mappings) in model.extensions.mappings) {
            for ((index, mapping) in mappings.withIndex()) {
                if (mapping !is RelationshipMapping || mapping.relationship != relationshipId) {
                    continue
                }
                val path = "extensions.mappings.$mappingId[$index]"
                for ((name, target) in listOf("from_node" to mapping.fromNode, "to_node" to mapping.toNode)) {
                    if (target.nodeKeyColumn.isNotEmpty() && target.nodeKeyProperty.isNotEmpty()) {
                        issues.add(
                            Issue(
                                code = "conflicting_target_mapping_key",
                                message = "Relationship '$relationshipId' mapping defines both node_key_column and " +
                                    "node_key_property on $name, use only one",
                                path = "$path.$name"
                            )
                        )
                    }
                }
                val from = mapping.fromNode.keyKind()
                val to = mapping.toNode.keyKind()
                if (from != null && to != null && from != to) {
                    issues.add(
                        Issue(
                            code = "mismatched_target_mapping_key",
                            message = "Relationship '$relationshipId' mapping must use the same node key type in " +
                                "from_node and to_node",
                            path = path
                        )
                    )
                }
            }
        }
    }

    /** The kind of node key used, or null when it is unset or ambiguous. */
    private fun TargetMapping.keyKind(): String? = when {
        nodeKeyColumn.isNotEmpty() && nodeKeyProperty.isEmpty() -> "column"
        nodeKeyProperty.isNotEmpty() && nodeKeyColumn.isEmpty() -> "property"
        else -> null
    }
}
