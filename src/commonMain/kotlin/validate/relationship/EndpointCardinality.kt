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
import model.relationship.RelationshipTarget
import validate.Issue
import validate.Validation
import validate.forEachEndpoint

/**
 * v1 rule: endpoint cardinality sanity (ontology-spec.schema.json `$defs.endpoint`:
 * `count`/`min_count` minimum 0, `max_count` minimum 1, "count is the exact form,
 * min_count/max_count the ranged form; absent means unconstrained (0..*)";
 * docs/ontology-spec-v1-proposal.md changelog 2026-09-25 and Appendix A "Cardinality
 * lives on the endpoints"). The minimums are schema-enforced for parsed documents but
 * not for programmatically built models; the cross-field rules the schema cannot
 * express at all: `min_count` must not exceed `max_count` (a range with min above max
 * is empty), and `count` is not combined with `min_count`/`max_count` — the schema
 * defines the exact and the ranged form as the two alternatives of one mechanism, so a
 * combination has no defined semantics.
 */
object EndpointCardinality : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachEndpoint { path, endpoint ->
            validateEndpoint(path, endpoint, issues)
        }
    }

    private fun validateEndpoint(path: String, endpoint: RelationshipTarget, issues: MutableList<Issue>) {
        val count = endpoint.count
        val minCount = endpoint.minCount
        val maxCount = endpoint.maxCount

        if (count != null && count < 0) {
            issues.add(
                Issue(
                    code = "negative_endpoint_count",
                    message = "Endpoint count must be >= 0, was $count",
                    path = "$path.count",
                ),
            )
        }
        if (minCount != null && minCount < 0) {
            issues.add(
                Issue(
                    code = "negative_endpoint_min_count",
                    message = "Endpoint min_count must be >= 0, was $minCount",
                    path = "$path.min_count",
                ),
            )
        }
        if (maxCount != null && maxCount < 1) {
            issues.add(
                Issue(
                    code = "invalid_endpoint_max_count",
                    message = "Endpoint max_count must be >= 1, was $maxCount",
                    path = "$path.max_count",
                ),
            )
        }
        if (count != null && (minCount != null || maxCount != null)) {
            issues.add(
                Issue(
                    code = "endpoint_count_combined_with_range",
                    message = "Endpoint count is the exact form; do not combine it with min_count/max_count",
                    path = "$path.count",
                ),
            )
        }
        if (minCount != null && maxCount != null && minCount > maxCount) {
            issues.add(
                Issue(
                    code = "endpoint_min_count_above_max_count",
                    message = "Endpoint min_count ($minCount) must not exceed max_count ($maxCount)",
                    path = "$path.min_count",
                ),
            )
        }
    }
}
