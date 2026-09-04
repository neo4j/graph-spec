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

// UPX isCustomIndexPropertySetDuplicate (json-helpers.ts): an index property
// set is a duplicate when it matches another index's set on the same entity,
// or a valid-for-index-view constraint's set - constraints imply backing
// indexes. Only an empty set is exempt; there is no composite gate.

/** True when [properties] matches another index's set in [otherIndexPropertySets]. */
fun isDuplicateIndexPropertySet(properties: Set<String>, otherIndexPropertySets: Collection<Set<String>>): Boolean =
    properties.isNotEmpty() &&
        otherIndexPropertySets.any { it == properties }

/** Property sets of constraints that imply a backing index.
 *
 * UPX isValidConstraintForIndexView (json-helpers.ts): a constraint implies an
 * index when it has properties, a type other than existence, a name, and is
 * not itself a duplicate composite (those are flagged by the constraint
 * duplicate rule instead). The null constraint type check from UPX is not
 * portable - graph-spec's ConstraintType is non-nullable.
 */
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
