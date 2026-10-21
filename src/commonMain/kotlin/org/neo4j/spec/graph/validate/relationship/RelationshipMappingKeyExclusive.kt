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
import org.neo4j.spec.graph.model.relationship.Relationship
import org.neo4j.spec.graph.validate.Issue

/**
 * A relationship mapping may identify relationships by either key columns or key properties, never both.
 */
object RelationshipMappingKeyExclusive : RelationshipValidation {
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
                if (mapping.keyColumns.isNotEmpty() && mapping.keyProperties.isNotEmpty()) {
                    issues.add(
                        Issue(
                            code = "conflicting_relationship_mapping_key",
                            message = "Relationship '$relationshipId' mapping defines both key_columns and " +
                                "key_properties, use only one",
                            path = "extensions.mappings.$mappingId[$index]"
                        )
                    )
                }
            }
        }
    }
}
