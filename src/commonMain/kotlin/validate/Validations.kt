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

import validate.node.NodeConstraintCoverage
import validate.node.NodeConstraints
import validate.node.NodeIndexesExists
import validate.node.NodeLabel
import validate.node.NodeLabelToken
import validate.node.NodeMappingKey
import validate.node.NodeMappingKeyType
import validate.node.NodeProperties
import validate.node.constraint.NodeExistenceConstraint
import validate.node.constraint.NodeTypeConstraint
import validate.relationship.RelationshipConstraints
import validate.relationship.RelationshipIndexes
import validate.relationship.RelationshipNodes
import validate.relationship.RelationshipType
import validate.relationship.RelationshipTypeToken
import validate.relationship.constraint.RelationshipExistenceConstraint
import validate.relationship.constraint.RelationshipTypeConstraint
import kotlin.js.JsExport
import kotlin.js.JsStatic

@JsExport
class Validations {
    companion object {
        @JsStatic
        val core: List<Validation> = listOf(
            // Nodes
            NodeConstraints,
            NodeIndexesExists,
            NodeLabel,
            NodeLabelToken,
            NodeProperties,
            NodeConstraintCoverage,
            NodeTypeConstraint,
            NodeExistenceConstraint,

            // Relationships
            RelationshipNodes,
            RelationshipIndexes,
            RelationshipConstraints,
            RelationshipType,
            RelationshipTypeToken,
            RelationshipExistenceConstraint,
            RelationshipTypeConstraint
        )

        @JsStatic
        val integrity: List<Validation> = listOf(
            NodeLabel,
            RelationshipType,
            RelationshipNodes
        )

        @JsStatic
        val mapping: List<Validation> = core + listOf(
            NodeMappingKey,
            NodeMappingKeyType
        )

        @JsStatic
        val all: List<Validation> = (core + integrity + mapping).distinct()
    }
}
