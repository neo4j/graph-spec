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
import validate.node.NodeIndexOptions
import validate.node.NodeIndexesExists
import validate.node.NodeLabel
import validate.node.NodeLabelToken
import validate.node.NodeMappingKey
import validate.node.NodeMappingKeyType
import validate.node.NodeProperties
import validate.node.constraint.NodeCompositeConstraintPropertyType
import validate.node.constraint.NodeConstraintDuplicatePropertySet
import validate.node.constraint.NodeConstraintProperties
import validate.node.constraint.NodeExistenceCompositeConflict
import validate.node.constraint.NodeExistenceConstraint
import validate.node.constraint.NodeKeyOverlap
import validate.node.constraint.NodeTypeConstraint
import validate.relationship.RelationshipConstraints
import validate.relationship.RelationshipIndexOptions
import validate.relationship.RelationshipIndexes
import validate.relationship.RelationshipNodes
import validate.relationship.RelationshipType
import validate.relationship.RelationshipTypeToken
import validate.relationship.constraint.RelationshipExistenceConstraint
import validate.relationship.constraint.RelationshipKeyOverlap
import validate.relationship.constraint.RelationshipTypeConstraint
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
            RelationshipNodes
        )

        // UPX kg-builder `validateStructuredSchema` (schemas-validators/) - gates accepting
        // an AI-generated schema before it's applied to the model.
        @JsStatic
        val kgbuilderReady: List<Validation> = listOf(
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
        val importReady: List<Validation> = listOf(
            NodeLabel,
            RelationshipType,
            NodeMappingKey,
            NodeConstraintProperties,
            NodeConstraintDuplicatePropertySet,
            NodeExistenceCompositeConflict
        )

        // UPX `migrateDataModelToLatestVersion` (migrations.ts) - throws and aborts loading a
        // model, called by both apps whenever a saved model is loaded/uploaded.
        @JsStatic
        val importParseIntegrity: List<Validation> = listOf(
            NodeLabel,
            RelationshipType
        )

        // UPX `apps/import/.../data-model.utils.ts` - import-app-only bulk pass.
        @JsStatic
        val bulkImportReady: List<Validation> = listOf(
            NodeLabelToken,
            RelationshipTypeToken,
            NodeMappingKeyType,
            NodeCompositeConstraintPropertyType
        )

        @JsStatic
        val all: List<Validation> =
            (core + kgbuilderReady + importReady + importParseIntegrity + bulkImportReady).distinct()
    }
}
