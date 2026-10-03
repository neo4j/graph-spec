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

import model.tool.toolTypeModules
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

        // Graph-spec validators added independent of any UPX call site. Tool type shapes
        // stay owner-defined (proposal open item 4), but tools are a core field, so the
        // predefined types' rules (ADR-0011, starting with canonicalQuery) live in core,
        // aggregated from the single module list in model.tool.toolTypeModules.
        @JsStatic
        val core: List<Validation> =
            listOf(
                EndpointNodeReferences,
                NodeLabelExclusivity,
                NodeLabelsIdentifier,
                PropertyTypeToken,
                KnownConstraintType,
                EndpointCardinality,
                RelationshipType,
                ConstraintPropertyReferences,
                ConstraintOverlap
            ) + toolTypeModules.flatMap { it.validations }

        // Named-extension validators (ADR-0005): each owner subpackage under
        // model/extension/ exposes its module's validations here, aggregated from the
        // single module list in model.extension.namedExtensionModules.
        @JsStatic
        val namedExtensions: List<Validation> =
            model.extension.namedExtensionModules.flatMap { it.validations } +
                // Cross-cutting rule, not owned by any module: reports payloads under
                // declared keys that no module can decode (ADR-0005), so it is appended
                // explicitly rather than via a module's validations.
                model.extension.MalformedNamedExtensionPayload

        /*
            The UPX call-site groups below encoded 4.0.0 consumer contracts: per-call-site
            selections from a 4.0.0 rule set whose validators are gone. v1 has exactly one
            soundness floor (core) plus the named-extension rules (ADR-0005); deleting the
            groups would break the public API ADR-0006 keeps stable. The import call sites
            gate on mapping/table/index correctness, so importReady and bulkImportReady
            include namedExtensions; kgbuilderReady (AI-generated schema, no source
            tables) and importParseIntegrity (load-time integrity) stay core-only.
         */

        // UPX kg-builder `validateStructuredSchema` (schemas-validators/) - gates accepting
        // an AI-generated schema before it's applied to the model.
        @JsStatic
        val kgbuilderReady: List<Validation> = core

        // UPX `getDataModelErrors` (errors.ts) - shared call site, gates "Run Import"
        // in both kg-builder's data-model-slice.ts and import's data-model.ts.
        // Import gates on the mapping/table/index checks too: those rules live in the
        // named extensions (ADR-0005), so importReady = core + namedExtensions.
        @JsStatic
        val importReady: List<Validation> = core + namedExtensions

        // UPX `migrateDataModelToLatestVersion` (migrations.ts) - throws and aborts loading a
        // model, called by both apps whenever a saved model is loaded/uploaded.
        @JsStatic
        val importParseIntegrity: List<Validation> = core

        // UPX `apps/import/.../data-model.utils.ts` - import-app-only bulk pass; like
        // importReady, it gates an import and needs the extension rules.
        @JsStatic
        val bulkImportReady: List<Validation> = core + namedExtensions

        @JsStatic
        val all: List<Validation> =
            (core + kgbuilderReady + importReady + importParseIntegrity + bulkImportReady + namedExtensions).distinct()
    }
}
