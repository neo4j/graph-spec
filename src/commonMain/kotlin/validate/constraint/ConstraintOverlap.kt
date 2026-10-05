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
import model.type.ConstraintType
import validate.Issue
import validate.Validation
import validate.constraintPath
import validate.constraintPropertiesPath
import validate.forEachElementConstraints
import validate.propertyPath

/**
 * v1 rule: redundant and conflicting constraints on one element (node or relationship).
 * The `mustExist`/`unique`/`key` flags on a property are the single-property shorthand of
 * the object form, and `key` implies `unique` + `mustExist`
 * (docs/ontology-spec-v1-proposal.md:93; the object form is defined at :91). Four overlaps
 * are flagged, on node and relationship elements alike:
 *
 *  - `redundant_constraint_key_overlap` — a `unique`/`mustExist` constraint object whose
 *    property set a `key` constraint already covers (4.0.0 NodeKeyOverlap);
 *  - `duplicate_shorthand_constraint_flag` — a true-valued shorthand flag duplicated by a
 *    same-type constraint object on the same property (new in v1; only `true` asserts —
 *    an absent or `false` flag states nothing);
 *  - `existence_composite_conflict` — a single-property `mustExist` constraint on a
 *    property that participates in a composite (multi-property) constraint
 *    (4.0.0 NodeExistenceCompositeConflict);
 *  - `duplicate_constraint_property_set` — two composite constraints over the same
 *    property set (4.0.0 NodeConstraintDuplicatePropertySet).
 */
object ConstraintOverlap : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachElementConstraints { path, properties, constraints ->
            keyOverlap(path, constraints, issues)
            shorthandFlagDuplication(path, properties, constraints, issues)
            existenceCompositeConflict(path, constraints, issues)
            duplicateCompositePropertySets(path, constraints, issues)
        }
    }

    private fun keyOverlap(path: String, constraints: List<Constraint>, issues: MutableList<Issue>) {
        val keyPropertySets = constraints
            .filter { it.type == ConstraintType.KEY }
            .map { it.properties.toSet() }
            .toSet()
        if (keyPropertySets.isEmpty()) return

        constraints.forEachIndexed { index, constraint ->
            if (constraint.type != ConstraintType.UNIQUE && constraint.type != ConstraintType.MUST_EXIST) {
                return@forEachIndexed
            }
            if (constraint.properties.toSet() in keyPropertySets) {
                issues.add(
                    Issue(
                        code = "redundant_constraint_key_overlap",
                        message =
                        "Constraint ${constraintRef(constraint, index)} (${constraint.type}) is redundant - " +
                            "a key constraint already covers the same properties",
                        path = constraintPath(path, index)
                    )
                )
            }
        }
    }

    private fun shorthandFlagDuplication(
        path: String,
        properties: Map<String, Property>,
        constraints: List<Constraint>,
        issues: MutableList<Issue>
    ) {
        for ((propertyId, property) in properties) {
            checkShorthandFlag(path, propertyId, property.key, ConstraintType.KEY, constraints, issues)
            checkShorthandFlag(path, propertyId, property.unique, ConstraintType.UNIQUE, constraints, issues)
            checkShorthandFlag(path, propertyId, property.mustExist, ConstraintType.MUST_EXIST, constraints, issues)
        }
    }

    private fun checkShorthandFlag(
        path: String,
        propertyId: String,
        flagValue: Boolean?,
        type: String,
        constraints: List<Constraint>,
        issues: MutableList<Issue>
    ) {
        if (flagValue != true) return
        val duplicated = constraints.any { it.type == type && it.properties == listOf(propertyId) }
        if (duplicated) {
            issues.add(
                Issue(
                    code = "duplicate_shorthand_constraint_flag",
                    message = "Flag '$type: true' on property '$propertyId' duplicates " +
                        "a $type constraint object on the same property",
                    path = "${propertyPath(path, propertyId)}.$type"
                )
            )
        }
    }

    private fun existenceCompositeConflict(path: String, constraints: List<Constraint>, issues: MutableList<Issue>) {
        val composites = constraints.filter { it.type != ConstraintType.MUST_EXIST && it.properties.size > 1 }
        if (composites.isEmpty()) return

        constraints.forEachIndexed { index, constraint ->
            if (constraint.type != ConstraintType.MUST_EXIST || constraint.properties.size != 1) return@forEachIndexed
            val propertyId = constraint.properties.first()
            if (composites.any { propertyId in it.properties }) {
                issues.add(
                    Issue(
                        code = "existence_composite_conflict",
                        message =
                        "mustExist constraint ${constraintRef(constraint, index)} on property '$propertyId' " +
                            "conflicts with a composite constraint covering the same property",
                        path = constraintPath(path, index)
                    )
                )
            }
        }
    }

    private fun duplicateCompositePropertySets(
        path: String,
        constraints: List<Constraint>,
        issues: MutableList<Issue>
    ) {
        constraints.forEachIndexed { index, constraint ->
            if (constraint.properties.size <= 1) return@forEachIndexed
            val propertySet = constraint.properties.toSet()
            val isDuplicate = constraints.indices.any { otherIndex ->
                otherIndex != index &&
                    constraints[otherIndex].properties.size > 1 &&
                    constraints[otherIndex].properties.toSet() == propertySet
            }
            if (isDuplicate) {
                issues.add(
                    Issue(
                        code = "duplicate_constraint_property_set",
                        message = "Constraint ${constraintRef(constraint, index)} property set duplicates " +
                            "another composite constraint",
                        path = constraintPropertiesPath(path, index)
                    )
                )
            }
        }
    }

    private fun constraintRef(constraint: Constraint, index: Int) =
        constraint.name?.let { "'$it'" } ?: "at index $index"
}
