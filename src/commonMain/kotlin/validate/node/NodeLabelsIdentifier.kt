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
package validate.node

import model.GraphModel
import validate.Issue
import validate.Validation
import validate.forEachNode

/**
 * v1 rule: when the `labels` object is used, `labels.identifier` is present and
 * non-blank (ontology-spec.schema.json `$defs.labels` lists `identifier` in `required`
 * and describes it as "The identifying (main) label."). The schema enforces this on
 * parsed documents; the Kotlin model's [model.node.Labels.identifier] is nullable, so a
 * programmatically built model can violate it — this is the model-level check.
 */
object NodeLabelsIdentifier : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachNode { path, nodeId, node ->
            val labels = node.labels ?: return@forEachNode
            if (labels.identifier.isNullOrBlank()) {
                issues.add(
                    Issue(
                        code = "missing_node_identifier_label",
                        message = "Missing identifier label for node '$nodeId'",
                        path = "$path.labels.identifier",
                    ),
                )
            }
        }
    }
}
