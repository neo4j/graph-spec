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
package org.neo4j.spec.graph.v1.validate

import org.neo4j.spec.graph.v1.validate.mapping.MappingDependencies
import org.neo4j.spec.graph.v1.validate.node.NodeConstraintCoverage
import org.neo4j.spec.graph.v1.validate.node.NodeConstraints
import org.neo4j.spec.graph.v1.validate.node.NodeIndexConstraintNameConflict
import org.neo4j.spec.graph.v1.validate.node.NodeIndexOptions
import org.neo4j.spec.graph.v1.validate.node.NodeIndexProperties
import org.neo4j.spec.graph.v1.validate.node.NodeIndexesExists
import org.neo4j.spec.graph.v1.validate.node.NodeLabel
import org.neo4j.spec.graph.v1.validate.node.NodeLabelToken
import org.neo4j.spec.graph.v1.validate.node.NodeMappingKey
import org.neo4j.spec.graph.v1.validate.node.NodeMappingKeyType
import org.neo4j.spec.graph.v1.validate.node.NodeProperties
import org.neo4j.spec.graph.v1.validate.node.NodePropertyDuplicateName
import org.neo4j.spec.graph.v1.validate.node.NodePropertyEmptyName
import org.neo4j.spec.graph.v1.validate.node.constraint.NodeCompositeConstraintPropertyType
import org.neo4j.spec.graph.v1.validate.node.constraint.NodeConstraintDuplicatePropertySet
import org.neo4j.spec.graph.v1.validate.node.constraint.NodeConstraintProperties
import org.neo4j.spec.graph.v1.validate.node.constraint.NodeExistenceCompositeConflict
import org.neo4j.spec.graph.v1.validate.node.constraint.NodeExistenceConstraint
import org.neo4j.spec.graph.v1.validate.node.constraint.NodeKeyOverlap
import org.neo4j.spec.graph.v1.validate.node.constraint.NodeTypeConstraint
import org.neo4j.spec.graph.v1.validate.relationship.RelationshipConstraints
import org.neo4j.spec.graph.v1.validate.relationship.RelationshipIndexOptions
import org.neo4j.spec.graph.v1.validate.relationship.RelationshipIndexProperties
import org.neo4j.spec.graph.v1.validate.relationship.RelationshipIndexes
import org.neo4j.spec.graph.v1.validate.relationship.RelationshipNodes
import org.neo4j.spec.graph.v1.validate.relationship.RelationshipPropertyDuplicateName
import org.neo4j.spec.graph.v1.validate.relationship.RelationshipPropertyEmptyName
import org.neo4j.spec.graph.v1.validate.relationship.RelationshipTargetCount
import org.neo4j.spec.graph.v1.validate.relationship.RelationshipType
import org.neo4j.spec.graph.v1.validate.relationship.RelationshipTypeToken
import org.neo4j.spec.graph.v1.validate.relationship.constraint.RelationshipExistenceConstraint
import org.neo4j.spec.graph.v1.validate.relationship.constraint.RelationshipKeyOverlap
import org.neo4j.spec.graph.v1.validate.relationship.constraint.RelationshipTypeConstraint
import org.neo4j.spec.graph.v1.validate.table.TableColumnDuplicateName
import org.neo4j.spec.graph.v1.validate.table.TableColumnEmptyName
import org.neo4j.spec.graph.v1.validate.table.TableColumnType
import kotlin.js.JsExport
import kotlin.js.JsStatic

@JsExport
class Validations {
    companion object {
        // Graph-spec validators added independent of any UPX call site.
        @JsStatic
        val core: List<Validation> = listOf(
            NodeTypeConstraint,
            RelationshipTypeConstraint,
            NodeConstraints,
            RelationshipConstraints,
            NodeIndexesExists,
            NodeIndexOptions,
            RelationshipIndexes,
            RelationshipIndexOptions,
            NodeExistenceConstraint,
            RelationshipExistenceConstraint,
            RelationshipNodes,
            RelationshipTargetCount
        )

        // UPX kg-builder `validateStructuredSchema` (schemas-validators/) - gates accepting
        // an AI-generated schema before it's applied to the model.
        @JsStatic
        val kgbuilderComplete: List<Validation> = listOf(
            NodeLabel,
            NodeProperties,
            NodeConstraintCoverage,
            NodeKeyOverlap,
            NodeExistenceConstraint,
            RelationshipType,
            RelationshipKeyOverlap,
            RelationshipExistenceConstraint,
            RelationshipNodes
        )

        // UPX `getDataModelErrors` (errors.ts) - shared call site, gates "Run Import"
        // in both kg-builder's data-model-slice.ts and import's data-model.ts.
        @JsStatic
        val importComplete: List<Validation> = listOf(
            NodeLabel,
            RelationshipType,
            NodeMappingKey,
            NodeIndexProperties,
            RelationshipIndexProperties,
            NodeConstraintProperties,
            NodeConstraintDuplicatePropertySet,
            NodeExistenceCompositeConflict,
            NodeIndexConstraintNameConflict,
            TableColumnEmptyName,
            TableColumnDuplicateName,
            TableColumnType,
            NodePropertyEmptyName,
            NodePropertyDuplicateName,
            RelationshipPropertyEmptyName,
            RelationshipPropertyDuplicateName,
            MappingDependencies
        )

        // UPX `migrateDataModelToLatestVersion` (migrations.ts) - throws and aborts loading a
        // model, called by both import and ontologies whenever a saved model is loaded/uploaded.
        @JsStatic
        val draft: List<Validation> = listOf(
            NodeLabel,
            RelationshipType
        )

        // UPX `apps/import/.../data-model.utils.ts` - import-app-only bulk pass.
        @JsStatic
        val bulkImportComplete: List<Validation> = listOf(
            NodeLabelToken,
            RelationshipTypeToken,
            NodeMappingKeyType,
            NodeCompositeConstraintPropertyType
        )

        @JsStatic
        val all: List<Validation> =
            (core + kgbuilderComplete + importComplete + draft + bulkImportComplete).distinct()
    }
}
