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
package model

import model.Rename.identify
import model.Rename.rename
import model.node.Constraint
import model.node.NodeConstraint
import model.property.Property
import model.relationship.RelationshipConstraint
import model.type.ConstraintType
import model.type.Named

/**
 * Gives all ids in the GraphModel a stable, sortable and predictable id.
 * Moving all human-readable ids into [Named.name]
 */
object Internal {
    fun internalise(model: GraphModel) {
        model.internaliseNodeLabels()
        model.internaliseNodes()
        model.internaliseNodeProperties()
        model.internaliseRelationships()
        model.internaliseRelationshipProperties()
        model.pretty = false
    }

    /*
        Nodes
     */

    private fun GraphModel.internaliseNodeLabels() {
        nodes.values.forEach { node ->
            val label = node.label
            if (label != null && node.labels.identifier == null) {
                node.labels.identifier = label
                node.label = null
            }
        }
    }

    private fun GraphModel.internaliseNodes() {
        val renames = nodes.identify("node")
        Pretty.renameNodeMappings(this, renames)
        nodes.values.forEach { node ->
            node.constraints.identify("nodeConstraint")
            node.indexes.identify("nodeIndex")
        }
        relationships.values.forEach { relationship ->
            relationship.from.node = renames[relationship.from.node] ?: relationship.from.node
            relationship.to.node = renames[relationship.to.node] ?: relationship.to.node
        }
        display.nodes.rename(renames)
    }

    private fun GraphModel.internaliseNodeProperties() {
        val renames = mutableMapOf<String, String>()
        nodes.forEach { (key, node) ->
            val propertyRenames = node.properties.identify("nodeProperty", key)
            renames.putAll(propertyRenames)
            node.constraints.values.forEach { it.properties.rename(renames, key) }
            node.indexes.values.forEach { it.properties.rename(renames, key) }
            internaliseProperties(node.constraints, node.properties) { type, props ->
                NodeConstraint(
                    type,
                    node.labels.identifier,
                    props
                )
            }
        }
        Pretty.renameNodeMappingProperties(this, renames)
        Pretty.renameTargetNodeProperties(this, renames)
    }

    private fun <C : Constraint> internaliseProperties(
        constraints: MutableMap<String, C>,
        properties: MutableMap<String, Property>,
        constraint: (type: ConstraintType, properties: MutableSet<String>) -> C
    ) {
        for ((key, property) in properties) {
            if (property.key == true) {
                property.key = null
                // TODO proper predictable id
                constraints["key_constraint_$key"] = constraint(ConstraintType.KEY, mutableSetOf(key))
            } else if (property.unique == true) {
                property.unique = null
                constraints["unique_constraint_$key"] = constraint(ConstraintType.UNIQUE, mutableSetOf(key))
            } else if (property.mustExist == true) {
                property.mustExist = null
                constraints["exists_constraint_$key"] = constraint(ConstraintType.EXISTS, mutableSetOf(key))
            }
        }
    }

    /*
        Relationships
     */

    private fun GraphModel.internaliseRelationships() {
        val renames = relationships.identify("relationship")
        Pretty.renameRelationshipMappings(this, renames)
        relationships.values.forEach { node ->
            node.constraints.identify("relationshipConstraint")
            node.indexes.identify("relationshipIndex")
        }
    }

    private fun GraphModel.internaliseRelationshipProperties() {
        val renames = mutableMapOf<String, String>()
        relationships.forEach { (key, relationship) ->
            val propertyRenames = relationship.properties.identify("relationshipProperty", key)
            renames.putAll(propertyRenames)
            relationship.constraints.values.forEach { it.properties.rename(renames, key) }
            relationship.indexes.values.forEach { it.properties.rename(renames, key) }
            internaliseProperties(relationship.constraints, relationship.properties) { type, props ->
                RelationshipConstraint(type, props)
            }
        }
        Pretty.renameRelationshipMappingProperties(this, renames)
    }
}
