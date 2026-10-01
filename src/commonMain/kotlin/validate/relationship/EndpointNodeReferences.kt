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
 * v1 rule: every relationship endpoint's `node` value resolves to a key in the
 * document's `nodes` map (ontology-spec.schema.json `$defs.endpoint.node`: "A node id
 * from the nodes map"; docs/ontology-spec-v1-proposal.md §Relationship: "`from`, `to` |
 * `{ node: <id>, ... }` — references a nodes-map key"). A cross-reference the schema
 * cannot express, so it is enforced here.
 */
object EndpointNodeReferences : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachRelationship { path, relationshipId, relationship ->
            if (!model.nodes.containsKey(relationship.from.node)) {
                issues.add(
                    Issue(
                        code = "missing_relation_from_node",
                        message = "Missing node with id '${relationship.from.node}' for relationship '$relationshipId'",
                        path = "$path.from.node",
                    ),
                )
            }
            if (!model.nodes.containsKey(relationship.to.node)) {
                issues.add(
                    Issue(
                        code = "missing_relation_to_node",
                        message = "Missing node with id '${relationship.to.node}' for relationship '$relationshipId'",
                        path = "$path.to.node",
                    ),
                )
            }
        }
    }
}
