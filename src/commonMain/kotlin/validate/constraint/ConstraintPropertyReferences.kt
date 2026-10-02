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
import model.node.Constraint
import model.property.Property
import validate.Issue
import validate.Validation
import validate.constraintPropertiesPath
import validate.constraintPropertyEntryPath
import validate.forEachElementConstraints

/**
 * v1 rule: a constraint object's `properties` list references properties declared on
 * the same element — "a node's constraints name that node's properties, a
 * relationship's constraints that relationship's properties"
 * (docs/ontology-spec-v1-proposal.md:91; ontology-spec.schema.json
 * `$defs.constraint.properties`). The schema's `minItems: 1` guards parsed documents
 * against an empty list; the Kotlin model's [model.node.Constraint.properties] is a
 * plain mutable list, so a programmatically built model can violate both the
 * same-element reference and the non-emptiness — these are the model-level checks,
 * run on node and relationship constraints alike.
 */
object ConstraintPropertyReferences : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachElementConstraints { path, properties, constraints ->
            constraints.forEachIndexed { index, constraint ->
                validateConstraintProperties(path, index, properties, constraint, issues)
            }
        }
    }

    private fun validateConstraintProperties(
        elementPath: String,
        constraintIndex: Int,
        properties: Map<String, Property>,
        constraint: Constraint,
        issues: MutableList<Issue>,
    ) {
        if (constraint.properties.isEmpty()) {
            issues.add(
                Issue(
                    code = "empty_constraint_properties",
                    message = "Constraint properties list is empty; expected at least one property reference",
                    path = constraintPropertiesPath(elementPath, constraintIndex),
                ),
            )
            return
        }
        constraint.properties.forEachIndexed { entryIndex, propertyId ->
            if (propertyId !in properties) {
                issues.add(
                    Issue(
                        code = "unknown_constraint_property",
                        message = "Constraint references undeclared property '$propertyId'",
                        path = constraintPropertyEntryPath(elementPath, constraintIndex, entryIndex),
                    ),
                )
            }
        }
    }
}
