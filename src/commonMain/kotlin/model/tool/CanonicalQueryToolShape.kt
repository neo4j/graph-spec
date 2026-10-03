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
package model.tool

import model.GraphModel
import validate.Issue
import validate.Validation
import validate.forEachTool

/**
 * `canonicalQuery` rule (ADR-0011): every tool of type `canonicalQuery` carries a non-blank
 * string `cypher` field — the typed shape's only required per-type field (the proposal's
 * tool example, docs/ontology-graph-spec-v1-proposal.md line ~222, always shows `cypher`).
 * Tool payloads are not schema-validated (`$defs/tool` is open, additionalProperties:true),
 * so this is the model-level check; a `cypher` of a non-string kind is no `cypher` at all.
 */
object CanonicalQueryToolShape : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachTool { path, tool ->
            if (tool.type != CanonicalQueryToolModule.type) return@forEachTool
            val cypher = tool.extra["cypher"]?.asString
            if (cypher.isNullOrBlank()) {
                issues.add(
                    Issue(
                        code = "missing_canonical_query_cypher",
                        message = "canonicalQuery tool '${tool.name ?: path}' requires a non-blank 'cypher' field",
                        path = "$path.cypher"
                    )
                )
            }
        }
    }
}
