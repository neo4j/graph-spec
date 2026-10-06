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
import model.relationship.Relationship
import model.relationship.RelationshipTarget
import validate.Issue

/**
 * A relationship target may define an exact `count` or a `minCount`/`maxCount` range, but not both.
 */
object RelationshipTargetCount : RelationshipValidation {
    override fun validateRelationship(
        model: GraphModel,
        relationshipId: String,
        relationship: Relationship,
        issues: MutableList<Issue>
    ) {
        validateTarget(relationshipId, "from", relationship.from, issues)
        validateTarget(relationshipId, "to", relationship.to, issues)
    }

    private fun validateTarget(
        relationshipId: String,
        side: String,
        target: RelationshipTarget,
        issues: MutableList<Issue>
    ) {
        val path = "relationships.$relationshipId.$side"
        val hasCount = target.count != -1
        val hasRange = target.minCount != 0 || target.maxCount != Int.MAX_VALUE
        if (hasCount && hasRange) {
            issues.add(
                Issue(
                    code = "relationship_count_conflict",
                    message = "Relationship '$relationshipId' $side can't set both count and min/max count",
                    path = path
                )
            )
        }
        if (hasCount && target.count < 0) {
            issues.add(
                Issue(
                    code = "relationship_count_negative",
                    message = "Relationship '$relationshipId' $side count must not be negative",
                    path = "$path.count"
                )
            )
        }
        if (target.minCount < 0) {
            issues.add(
                Issue(
                    code = "relationship_min_count_negative",
                    message = "Relationship '$relationshipId' $side min count must not be negative",
                    path = "$path.minCount"
                )
            )
        }
        if (target.minCount > target.maxCount) {
            issues.add(
                Issue(
                    code = "relationship_min_count_exceeds_max",
                    message = "Relationship '$relationshipId' $side min count is greater than max count",
                    path = path
                )
            )
        }
    }
}
