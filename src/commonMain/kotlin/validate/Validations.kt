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

import validate.constraint.ConstraintOverlap
import validate.constraint.ConstraintPropertyReferences
import validate.constraint.KnownConstraintType
import validate.node.NodeLabelExclusivity
import validate.node.NodeLabelsIdentifier
import validate.property.PropertyTypeToken
import validate.relationship.EndpointCardinality
import validate.relationship.EndpointNodeReferences
import validate.relationship.RelationshipType
import kotlin.js.JsExport
import kotlin.js.JsStatic

@JsExport
class Validations {
    companion object {
        /*
            The v1 suite (track 2, ADR-0004): the rules ontology-graph-spec.schema.json cannot
            express. Each validator's KDoc cites the schema section or proposal line it
            enforces. The constraint-package rules are KnownConstraintType
            (constraint_type enum), ConstraintPropertyReferences (a constraint's
            properties resolve to properties declared on the same element, and the list
            is non-empty) and ConstraintOverlap (redundant and conflicting constraints
            on one element: key overlap, duplicated shorthand flags, existence/composite
            conflict, duplicate composite property sets). The 4.0.0 validators were
            deleted with their model subjects in the v1 model rewrite (track 1).
         */

        // Graph-spec validators added independent of any UPX call site.
        @JsStatic
        val core: List<Validation> = listOf(
            EndpointNodeReferences,
            NodeLabelExclusivity,
            NodeLabelsIdentifier,
            PropertyTypeToken,
            KnownConstraintType,
            EndpointCardinality,
            RelationshipType,
            ConstraintPropertyReferences,
            ConstraintOverlap,
        )

        // Named-extension validators (ADR-0005): each owner subpackage under
        // model/extension/ exposes its module's validations here once it lands; no
        // named-extension modules exist yet.
        @JsStatic
        val namedExtensions: List<Validation> = emptyList()

        /*
            The UPX call-site groups below encoded 4.0.0 consumer contracts: per-call-site
            selections from a 4.0.0 rule set whose validators are gone. v1 has exactly one
            soundness floor (core), so every group aliases it — deleting the groups would
            break the public API ADR-0006 keeps stable, and leaving them empty would fail
            open at call sites that gate on validation. The per-call-site differentiation
            returns when a consumer specifies a v1 grouping.
         */

        // UPX kg-builder `validateStructuredSchema` (schemas-validators/) - gates accepting
        // an AI-generated schema before it's applied to the model.
        @JsStatic
        val kgbuilderReady: List<Validation> = core

        // UPX `getDataModelErrors` (errors.ts) - shared call site, gates "Run Import"
        // in both kg-builder's data-model-slice.ts and import's data-model.ts.
        @JsStatic
        val importReady: List<Validation> = core

        // UPX `migrateDataModelToLatestVersion` (migrations.ts) - throws and aborts loading a
        // model, called by both apps whenever a saved model is loaded/uploaded.
        @JsStatic
        val importParseIntegrity: List<Validation> = core

        // UPX `apps/import/.../data-model.utils.ts` - import-app-only bulk pass.
        @JsStatic
        val bulkImportReady: List<Validation> = core

        @JsStatic
        val all: List<Validation> =
            (core + kgbuilderReady + importReady + importParseIntegrity + bulkImportReady + namedExtensions).distinct()
    }
}
