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
package validate

import model.GraphModel
import model.node.Constraint
import model.node.Node
import model.property.Property
import model.relationship.Relationship
import model.relationship.RelationshipTarget

/*
 * The one owner of the model traversal and the issue-path grammar: `nodes.<id>`,
 * `relationships.<id>`, and the `properties.<pid>` / `constraints[<index>]` /
 * `constraints[<index>].properties` / `constraints[<index>].properties[<entry>]` /
 * `from` / `to` segments beneath them. Validators delegate here and keep only their
 * rule; the grammar is pinned byte-for-byte by the validator and bridge tests, so a
 * path change belongs in this file alone — never re-derived per validator.
 */

/** `properties.<pid>` beneath an element path. */
internal fun propertyPath(elementPath: String, propertyId: String) = "$elementPath.properties.$propertyId"

/** `constraints[<index>]` beneath an element path. */
internal fun constraintPath(elementPath: String, index: Int) = "$elementPath.constraints[$index]"

/** `constraints[<index>].properties` beneath an element path. */
internal fun constraintPropertiesPath(elementPath: String, index: Int) =
    "${constraintPath(elementPath, index)}.properties"

/** `constraints[<index>].properties[<entryIndex>]` beneath an element path. */
internal fun constraintPropertyEntryPath(elementPath: String, index: Int, entryIndex: Int) =
    "${constraintPropertiesPath(elementPath, index)}[$entryIndex]"

internal fun GraphModel.forEachNode(action: (path: String, id: String, node: Node) -> Unit) {
    for ((id, node) in nodes) action("nodes.$id", id, node)
}

internal fun GraphModel.forEachRelationship(action: (path: String, id: String, relationship: Relationship) -> Unit) {
    for ((id, relationship) in relationships) action("relationships.$id", id, relationship)
}

internal fun GraphModel.forEachProperty(action: (path: String, property: Property) -> Unit) {
    forEachNode { nodePath, _, node ->
        for ((propertyId, property) in node.properties) action(propertyPath(nodePath, propertyId), property)
    }
    forEachRelationship { relationshipPath, _, relationship ->
        for ((propertyId, property) in relationship.properties) {
            action(propertyPath(relationshipPath, propertyId), property)
        }
    }
}

internal fun GraphModel.forEachConstraint(action: (path: String, constraint: Constraint) -> Unit) {
    forEachNode { nodePath, _, node ->
        node.constraints.forEachIndexed { index, constraint -> action(constraintPath(nodePath, index), constraint) }
    }
    forEachRelationship { relationshipPath, _, relationship ->
        relationship.constraints.forEachIndexed { index, constraint ->
            action(constraintPath(relationshipPath, index), constraint)
        }
    }
}

/**
 * Hands each element's path, its properties map, and its constraints list together,
 * for nodes and relationships alike. For validators that cross-check a constraint's
 * `properties` entries against the owning element's property keys and the [Property]
 * objects' shorthand flags — the element path is derived here, never by the caller.
 */
internal fun GraphModel.forEachElementConstraints(
    action: (path: String, properties: Map<String, Property>, constraints: List<Constraint>) -> Unit
) {
    forEachNode { nodePath, _, node -> action(nodePath, node.properties, node.constraints) }
    forEachRelationship { relationshipPath, _, relationship ->
        action(relationshipPath, relationship.properties, relationship.constraints)
    }
}

internal fun GraphModel.forEachEndpoint(
    action: (path: String, side: String, relationshipId: String, endpoint: RelationshipTarget) -> Unit
) {
    forEachRelationship { relationshipPath, relationshipId, relationship ->
        action("$relationshipPath.from", "from", relationshipId, relationship.from)
        action("$relationshipPath.to", "to", relationshipId, relationship.to)
    }
}
