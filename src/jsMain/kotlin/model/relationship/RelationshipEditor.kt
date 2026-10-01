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
package model.relationship

import model.GraphModelJs
import model.addUnique
import model.getOrThrow
import model.property.PropertyEditor
import model.property.PropertyJs
import model.property.propertyJs
import model.remove
import kotlin.collections.plus

@JsExport
class RelationshipEditor {
    companion object {
        @JsStatic
        fun setType(relationship: RelationshipJs, type: String) {
            relationship.type = type
        }

        @JsStatic
        fun setDescription(relationship: RelationshipJs, description: String?) {
            relationship.description = description
        }

        @JsStatic
        fun setSourceNode(relationship: RelationshipJs, node: String) {
            RelationshipTargetEditor.setNode(relationship.from, node)
        }

        @JsStatic
        fun setTargetNode(relationship: RelationshipJs, node: String) {
            RelationshipTargetEditor.setNode(relationship.to, node)
        }

        /*
            Properties
         */

        @JsStatic
        fun addProperty(model: GraphModelJs, relationshipId: String): String {
            val relationship = model.relationships.getOrThrow(relationshipId, "Relationship")
            return relationship.properties.addUnique("property") { id ->
                propertyJs(id = id)
            }
        }

        @JsStatic
        fun removeProperty(model: GraphModelJs, relationshipId: String, propertyId: String) {
            val relationship = model.relationships.getOrThrow(relationshipId, "Relationship")
            relationship.properties.remove(propertyId)
        }

        @JsStatic
        fun setPropertyType(model: GraphModelJs, relationshipId: String, propertyId: String, type: String) {
            val property = getProperty(model, relationshipId, propertyId)
            PropertyEditor.setType(property, type)
        }

        @JsStatic
        fun setPropertyDimension(model: GraphModelJs, relationshipId: String, propertyId: String, dimension: Int?) {
            val property = getProperty(model, relationshipId, propertyId)
            PropertyEditor.setDimension(property, dimension)
        }

        @JsStatic
        fun setPropertyMustExist(model: GraphModelJs, relationshipId: String, propertyId: String, mustExist: Boolean) {
            val property = getProperty(model, relationshipId, propertyId)
            PropertyEditor.setMustExist(property, mustExist)
        }

        @JsStatic
        fun setPropertyUnique(model: GraphModelJs, relationshipId: String, propertyId: String, unique: Boolean) {
            val property = getProperty(model, relationshipId, propertyId)
            PropertyEditor.setUnique(property, unique)
        }

        @JsStatic
        fun setPropertyKey(model: GraphModelJs, relationshipId: String, propertyId: String, key: Boolean) {
            val property = getProperty(model, relationshipId, propertyId)
            PropertyEditor.setKey(property, key)
        }

        private fun getProperty(model: GraphModelJs, relationshipId: String, propertyId: String): PropertyJs {
            val relationship = model.relationships.getOrThrow(relationshipId, "Relationship")
            val property = relationship.properties.getOrThrow(propertyId, "Property")
            return property
        }

        /*
            Constraints
         */

        @JsStatic
        fun addConstraint(
            model: GraphModelJs,
            relationshipId: String,
            type: String,
            name: String? = null,
            properties: Array<String> = emptyArray(),
        ): Int {
            val relationship = model.relationships.getOrThrow(relationshipId, "Relationship")
            relationship.constraints += relationshipConstraintJs(type, name, properties)
            return relationship.constraints.size - 1
        }

        @JsStatic
        fun setConstraintType(model: GraphModelJs, relationshipId: String, constraintIndex: Int, type: String) {
            val constraint = getConstraint(model, relationshipId, constraintIndex)
            RelationshipConstraintEditor.setType(constraint, type)
        }

        @JsStatic
        fun setConstraintName(model: GraphModelJs, relationshipId: String, constraintIndex: Int, name: String?) {
            val constraint = getConstraint(model, relationshipId, constraintIndex)
            RelationshipConstraintEditor.setName(constraint, name)
        }

        @JsStatic
        fun addConstraintProperty(
            model: GraphModelJs,
            relationshipId: String,
            constraintIndex: Int,
            propertyId: String,
        ) {
            val constraint = getConstraint(model, relationshipId, constraintIndex)
            RelationshipConstraintEditor.addProperty(constraint, propertyId)
        }

        @JsStatic
        fun removeConstraintProperty(
            model: GraphModelJs,
            relationshipId: String,
            constraintIndex: Int,
            propertyId: String,
        ) {
            val constraint = getConstraint(model, relationshipId, constraintIndex)
            RelationshipConstraintEditor.removeProperty(constraint, propertyId)
        }

        private fun getConstraint(
            model: GraphModelJs,
            relationshipId: String,
            constraintIndex: Int,
        ): RelationshipConstraintJs {
            val relationship = model.relationships.getOrThrow(relationshipId, "Relationship")
            return relationship.constraints.getOrNull(constraintIndex)
                ?: error("Constraint with index '$constraintIndex' not found.")
        }
    }
}
