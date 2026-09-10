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
package validate.index

import model.node.Constraint
import model.type.ConstraintType
import model.type.Named
import validate.constraint.isCompositeConstraint

// Ported from UPX isCustomIndexPropertySetDuplicate (json-helpers.ts:474).
fun isDuplicateIndexPropertySet(properties: Set<String>, otherIndexPropertySets: Collection<Set<String>>): Boolean =
    properties.isNotEmpty() &&
        otherIndexPropertySets.any { it == properties }

// Ported from UPX isValidConstraintForIndexView (json-helpers.ts:448).
fun <T> constraintDerivedIndexPropertySets(
    constraints: Collection<T>
): List<Set<String>>
    where T : Constraint, T : Named {
    val compositeConstraints = constraints.filter { isCompositeConstraint(it.properties) }
    return constraints
        .filterNot { constraint ->
            constraint.properties.isEmpty() ||
                constraint.type == ConstraintType.EXISTS ||
                constraint.name.isNullOrEmpty() ||
                isDuplicateCompositeConstraint(constraint, compositeConstraints)
        }
        .map { it.properties.toSet() }
}

private fun <T> isDuplicateCompositeConstraint(
    constraint: T,
    compositeConstraints: List<T>
): Boolean
    where T : Constraint, T : Named = compositeConstraints.any { other ->
    other !== constraint && other.properties.toSet() == constraint.properties.toSet()
}
