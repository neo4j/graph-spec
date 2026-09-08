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
import validate.node.NodeIndexConstraintNameConflict
import validate.node.NodeIndexProperties
import validate.node.NodeLabel
import validate.node.NodeMappingKey
import validate.node.NodePropertyDuplicateName
import validate.node.NodePropertyEmptyName
import validate.node.constraint.NodeConstraintDuplicatePropertySet
import validate.node.constraint.NodeConstraintProperties
import validate.node.constraint.NodeExistenceCompositeConflict
import validate.node.constraint.NodeExistenceConstraint
import validate.node.constraint.NodeKeyOverlap
import validate.relationship.RelationshipIndexProperties
import validate.relationship.RelationshipNodes
import validate.relationship.RelationshipPropertyDuplicateName
import validate.relationship.RelationshipPropertyEmptyName
import validate.relationship.RelationshipType
import validate.relationship.constraint.RelationshipExistenceConstraint
import validate.relationship.constraint.RelationshipKeyOverlap
import validate.table.TableColumnDuplicateName
import validate.table.TableColumnEmptyName
import validate.table.TableColumnType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ValidationsTest {

    private val importDraftSchemaRules = listOf(
        NodeLabel,
        RelationshipType,
        NodeIndexProperties,
        RelationshipIndexProperties,
        NodeConstraintProperties,
        NodeConstraintDuplicatePropertySet,
        NodeExistenceCompositeConflict,
        NodeIndexConstraintNameConflict,
        NodePropertyEmptyName,
        NodePropertyDuplicateName,
        RelationshipPropertyEmptyName,
        RelationshipPropertyDuplicateName
    )

    private val ontologyDraftSchemaRules = listOf(
        NodeLabel,
        RelationshipType,
        NodePropertyEmptyName,
        NodePropertyDuplicateName,
        RelationshipPropertyEmptyName,
        RelationshipPropertyDuplicateName,
        NodeIndexProperties,
        RelationshipIndexProperties,
        NodeIndexConstraintNameConflict,
        NodeConstraintProperties,
        NodeConstraintDuplicatePropertySet,
        NodeExistenceCompositeConflict,
        NodeExistenceConstraint,
        RelationshipExistenceConstraint,
        RelationshipNodes
    )

    @Test
    fun `importDraft covers import-ready schema-shape rules without data-source mappings`() {
        assertEquals(importDraftSchemaRules, Validations.importDraft)
    }

    @Test
    fun `importDraft excludes data-source mapping validators`() {
        assertTrue(TableColumnEmptyName !in Validations.importDraft)
        assertTrue(TableColumnDuplicateName !in Validations.importDraft)
        assertTrue(TableColumnType !in Validations.importDraft)
        assertTrue(NodeMappingKey !in Validations.importDraft)
    }

    @Test
    fun `ontologyDraft covers schema-shape rules without key-property or mapping validators`() {
        assertEquals(ontologyDraftSchemaRules, Validations.ontologyDraft)
    }

    @Test
    fun `ontologyDraft excludes key-property and mapping validators`() {
        assertTrue(NodeKeyOverlap !in Validations.ontologyDraft)
        assertTrue(RelationshipKeyOverlap !in Validations.ontologyDraft)
        assertTrue(NodeConstraintCoverage !in Validations.ontologyDraft)
        assertTrue(NodeMappingKey !in Validations.ontologyDraft)
    }

    @Test
    fun `all includes both draft groups`() {
        assertTrue(Validations.all.containsAll(Validations.importDraft))
        assertTrue(Validations.all.containsAll(Validations.ontologyDraft))
        assertEquals(Validations.all, Validations.all.distinct())
    }
}
