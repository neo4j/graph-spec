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
package validate.constraint

import model.GraphModel
import model.type.ConstraintType
import validate.Issue
import validate.Validation
import validate.forEachConstraint

private val KNOWN_CONSTRAINT_TYPES = setOf(ConstraintType.KEY, ConstraintType.UNIQUE, ConstraintType.MUST_EXIST)

/**
 * v1 rule: a constraint object's `constraint_type` is one of `key`, `unique`,
 * `mustExist` (ontology-spec.schema.json `$defs.constraint.constraint_type` enum;
 * docs/ontology-spec-v1-proposal.md §Node: "constraint objects, optionally named
 * (`{ constraint_type, name?, properties }`)"). The schema enforces the enum on parsed
 * documents; the Kotlin model's [model.node.Constraint.type] is a plain string, so a
 * programmatically built model can violate it — this is the model-level check, run on
 * node and relationship constraints alike.
 */
object KnownConstraintType : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachConstraint { path, constraint ->
            validateConstraintType("$path.constraint_type", constraint.type, issues)
        }
    }

    private fun validateConstraintType(path: String, type: String, issues: MutableList<Issue>) {
        if (type !in KNOWN_CONSTRAINT_TYPES) {
            issues.add(
                Issue(
                    code = "unknown_constraint_type",
                    message = "Unknown constraint_type '$type'; expected one of ${KNOWN_CONSTRAINT_TYPES.joinToString(
                        ", ",
                    )}",
                    path = path,
                ),
            )
        }
    }
}
