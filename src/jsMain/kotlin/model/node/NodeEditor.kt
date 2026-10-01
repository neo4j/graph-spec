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
package model.node

import model.GraphModelJs
import model.addUnique
import model.getOrThrow
import model.property.PropertyEditor
import model.property.PropertyJs
import model.property.propertyJs
import model.remove
import kotlin.collections.plus

@JsExport
class NodeEditor {
    companion object {
        @JsStatic
        fun setLabel(model: GraphModelJs, nodeId: String, label: String?) {
            val node = model.nodes.getOrThrow(nodeId, "Node")
            node.label = label
        }

        @JsStatic
        fun setDescription(model: GraphModelJs, nodeId: String, description: String?) {
            val node = model.nodes.getOrThrow(nodeId, "Node")
            node.description = description
        }

        /*
            Labels
         */

        @JsStatic
        fun setIdentifyingLabel(model: GraphModelJs, nodeId: String, label: String) {
            val labels = getOrCreateLabels(model, nodeId)
            LabelsEditor.setIdentifier(labels, label)
        }

        @JsStatic
        fun addImpliedLabel(model: GraphModelJs, nodeId: String, label: String) {
            val labels = getOrCreateLabels(model, nodeId)
            LabelsEditor.addImplied(labels, label)
        }

        @JsStatic
        fun removeImpliedLabel(model: GraphModelJs, nodeId: String, label: String) {
            val labels = getOrCreateLabels(model, nodeId)
            LabelsEditor.removeImplied(labels, label)
        }

        @JsStatic
        fun addOptionalLabel(model: GraphModelJs, nodeId: String, label: String) {
            val labels = getOrCreateLabels(model, nodeId)
            LabelsEditor.addOptional(labels, label)
        }

        @JsStatic
        fun removeOptionalLabel(model: GraphModelJs, nodeId: String, label: String) {
            val labels = getOrCreateLabels(model, nodeId)
            LabelsEditor.removeOptional(labels, label)
        }

        private fun getOrCreateLabels(model: GraphModelJs, nodeId: String): LabelsJs {
            val node = model.nodes.getOrThrow(nodeId, "Node")
            return node.labels ?: labelsJs().also { node.labels = it }
        }

        /*
            Properties
         */

        @JsStatic
        fun addProperty(model: GraphModelJs, nodeId: String): String {
            val node = model.nodes.getOrThrow(nodeId, "Node")
            return node.properties.addUnique("property") { id ->
                propertyJs(id = id)
            }
        }

        @JsStatic
        fun removeProperty(model: GraphModelJs, nodeId: String, propertyId: String) {
            val node = model.nodes.getOrThrow(nodeId, "Node")
            node.properties.remove(propertyId)
        }

        @JsStatic
        fun setPropertyType(model: GraphModelJs, nodeId: String, propertyId: String, type: String) {
            val property = getProperty(model, nodeId, propertyId)
            PropertyEditor.setType(property, type)
        }

        @JsStatic
        fun setPropertyDimension(model: GraphModelJs, nodeId: String, propertyId: String, dimension: Int?) {
            val property = getProperty(model, nodeId, propertyId)
            PropertyEditor.setDimension(property, dimension)
        }

        @JsStatic
        fun setPropertyMustExist(model: GraphModelJs, nodeId: String, propertyId: String, mustExist: Boolean) {
            val property = getProperty(model, nodeId, propertyId)
            PropertyEditor.setMustExist(property, mustExist)
        }

        @JsStatic
        fun setPropertyUnique(model: GraphModelJs, nodeId: String, propertyId: String, unique: Boolean) {
            val property = getProperty(model, nodeId, propertyId)
            PropertyEditor.setUnique(property, unique)
        }

        @JsStatic
        fun setPropertyKey(model: GraphModelJs, nodeId: String, propertyId: String, key: Boolean) {
            val property = getProperty(model, nodeId, propertyId)
            PropertyEditor.setKey(property, key)
        }

        private fun getProperty(model: GraphModelJs, nodeId: String, propertyId: String): PropertyJs {
            val node = model.nodes.getOrThrow(nodeId, "Node")
            val property = node.properties.getOrThrow(propertyId, "Property")
            return property
        }

        /*
            Constraints
         */

        @JsStatic
        fun addConstraint(
            model: GraphModelJs,
            nodeId: String,
            type: String,
            name: String? = null,
            properties: Array<String> = emptyArray(),
        ): Int {
            val node = model.nodes.getOrThrow(nodeId, "Node")
            node.constraints += nodeConstraintJs(type, name, properties)
            return node.constraints.size - 1
        }

        @JsStatic
        fun setConstraintType(model: GraphModelJs, nodeId: String, constraintIndex: Int, type: String) {
            val constraint = getConstraint(model, nodeId, constraintIndex)
            NodeConstraintEditor.setType(constraint, type)
        }

        @JsStatic
        fun setConstraintName(model: GraphModelJs, nodeId: String, constraintIndex: Int, name: String?) {
            val constraint = getConstraint(model, nodeId, constraintIndex)
            NodeConstraintEditor.setName(constraint, name)
        }

        @JsStatic
        fun addConstraintProperty(model: GraphModelJs, nodeId: String, constraintIndex: Int, propertyId: String) {
            val constraint = getConstraint(model, nodeId, constraintIndex)
            NodeConstraintEditor.addProperty(constraint, propertyId)
        }

        @JsStatic
        fun removeConstraintProperty(model: GraphModelJs, nodeId: String, constraintIndex: Int, propertyId: String) {
            val constraint = getConstraint(model, nodeId, constraintIndex)
            NodeConstraintEditor.removeProperty(constraint, propertyId)
        }

        private fun getConstraint(model: GraphModelJs, nodeId: String, constraintIndex: Int): NodeConstraintJs {
            val node = model.nodes.getOrThrow(nodeId, "Node")
            return node.constraints.getOrNull(constraintIndex)
                ?: error("Constraint with index '$constraintIndex' not found.")
        }
    }
}
