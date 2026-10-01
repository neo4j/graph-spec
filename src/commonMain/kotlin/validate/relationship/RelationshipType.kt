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
package validate.relationship

import model.GraphModel
import validate.Issue
import validate.Validation
import validate.forEachRelationship

/**
 * v1 rule: a relationship's `type` is present and non-blank
 * (ontology-spec.schema.json `$defs.relationshipEntry` requires `type` but types it as
 * a plain string, so a blank value passes schema validation;
 * docs/ontology-spec-v1-proposal.md §Relationship: "`type` | the relationship type
 * (required)"). A blank type is the required field's empty form, rejected here.
 */
object RelationshipType : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachRelationship { path, relationshipId, relationship ->
            if (relationship.type.isBlank()) {
                issues.add(
                    Issue(
                        code = "missing_relation_type",
                        message = "Missing type for relationship '$relationshipId'",
                        path = "$path.type",
                    ),
                )
            }
        }
    }
}
