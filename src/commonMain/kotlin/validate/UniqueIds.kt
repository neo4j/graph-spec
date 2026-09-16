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
package validate

import model.GraphModel

/**
 * Every id in a model must be unique across the whole document. Ported from UPX
 * `validateUniqueIds` (platform/import-shared/src/schemas/data-model.schemas.ts:283) - one namespace
 * shared by nodes, relationships, and each entity's properties, constraints and indexes.
 */
object UniqueIds : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        val entries = mutableListOf<Pair<String, String>>()
        for ((nodeId, node) in model.nodes) {
            entries.add(nodeId to "nodes.$nodeId")
            for (propertyId in node.properties.keys) {
                entries.add(propertyId to "nodes.$nodeId.properties.$propertyId")
            }
            for (constraintId in node.constraints.keys) {
                entries.add(constraintId to "nodes.$nodeId.constraints.$constraintId")
            }
            for (indexId in node.indexes.keys) {
                entries.add(indexId to "nodes.$nodeId.indexes.$indexId")
            }
        }
        for ((relationshipId, relationship) in model.relationships) {
            entries.add(relationshipId to "relationships.$relationshipId")
            for (propertyId in relationship.properties.keys) {
                entries.add(propertyId to "relationships.$relationshipId.properties.$propertyId")
            }
            for (constraintId in relationship.constraints.keys) {
                entries.add(constraintId to "relationships.$relationshipId.constraints.$constraintId")
            }
            for (indexId in relationship.indexes.keys) {
                entries.add(indexId to "relationships.$relationshipId.indexes.$indexId")
            }
        }

        val seen = mutableSetOf<String>()
        val duplicated = mutableSetOf<String>()
        for ((id, _) in entries) {
            if (!seen.add(id)) duplicated.add(id)
        }

        for ((id, path) in entries) {
            if (id in duplicated) {
                issues.add(
                    Issue(
                        code = "duplicate_id",
                        message = "Duplicate id '$id' in the model",
                        path = path
                    )
                )
            }
        }
    }
}
