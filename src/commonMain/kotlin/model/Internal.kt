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

import model.Rename.assignIds
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
        val renames = nodes.assignIds("node")
        Pretty.renameNodeMappings(this, renames)
        nodes.forEach { (key, node) ->
            node.indexes.assignIds("index", key)
        }
        relationships.values.forEach { relationship ->
            relationship.start.node = renames[relationship.start.node] ?: relationship.start.node
            relationship.end.node = renames[relationship.end.node] ?: relationship.end.node
        }
        display.nodes.rename(renames)
    }

    private fun GraphModel.internaliseNodeProperties() {
        val renames = mutableMapOf<String, String>()
        nodes.forEach { (key, node) ->
            val shorthand = internaliseProperties(
                node.constraints,
                node.properties,
                node.name ?: key,
                node.labels.identifier ?: ""
            ) { type, props ->
                NodeConstraint(type, node.labels.identifier, props)
            }
            node.constraints.assignIds("constraint", key)
            val propertyRenames = node.properties.assignIds("property", key)
            renames.putAll(propertyRenames)
            node.constraints.values.forEach { it.properties.rename(renames, key) }
            node.indexes.values.forEach { it.properties.rename(renames, key) }
            shorthand.forEach { (constraint, name) -> if (name.isNotBlank()) constraint.name = name }
        }
        Pretty.renameNodeMappingProperties(this, renames)
    }

    /**
     * Converts shorthand constraints into long-hand
     * Doesn't check or transform (e.g. for overlapping)
     *
     * Returns each new constraint with the name it should carry once ids are assigned. Naming it here
     * would make [Rename.assignIds] skip it, leaving it unnumbered.
     */
    private fun <C : Constraint> internaliseProperties(
        constraints: MutableMap<String, C>,
        properties: MutableMap<String, Property>,
        entity: String,
        label: String,
        constraint: (type: ConstraintType, properties: MutableSet<String>) -> C
    ): List<Pair<C, String>> {
        val shorthand = mutableListOf<Pair<C, String>>()
        for ((key, property) in properties) {
            fun add(type: ConstraintType): Pair<C, String> =
                addConstraint(constraints, property.name ?: key, key, constraint, type, entity, label)

            if (property.key == true) {
                property.key = null
                shorthand += add(ConstraintType.KEY)
            }
            if (property.unique == true) {
                property.unique = null
                shorthand += add(ConstraintType.UNIQUE)
            }
            if (property.mustExist == true) {
                property.mustExist = null
                shorthand += add(ConstraintType.EXISTS)
            }
        }
        return shorthand
    }

    /**
     * The key stays the deterministic id, so it is unique even when two property tokens collapse to the
     * same name. The name is returned rather than set, so the constraint is still unnamed when
     * [Rename.assignIds] numbers it and keeps [Named.name] as the marker of an already internal entry.
     */
    private fun <C : Constraint> addConstraint(
        constraints: MutableMap<String, C>,
        propertyToken: String,
        key: String,
        constraint: (ConstraintType, MutableSet<String>) -> C,
        type: ConstraintType,
        entity: String,
        label: String
    ): Pair<C, String> {
        val created = constraint(type, mutableSetOf(key))
        constraints[deterministicId(entity, type, propertyToken)] = created
        return created to NameFormat.constraintName(listOf(propertyToken), label, type)
    }

    internal fun deterministicId(entity: String, type: ConstraintType, vararg properties: String): String =
        "${type.name.lowercase()}_${entity}_${properties.distinct().sorted().joinToString("_")}"

    /*
        Relationships
     */

    private fun GraphModel.internaliseRelationships() {
        val renames = relationships.assignIds("relationship")
        Pretty.renameRelationshipMappings(this, renames)
        relationships.forEach { (key, relationship) ->
            relationship.indexes.assignIds("index", key)
        }
    }

    private fun GraphModel.internaliseRelationshipProperties() {
        val renames = mutableMapOf<String, String>()
        relationships.forEach { (key, relationship) ->
            val shorthand = internaliseProperties(
                relationship.constraints,
                relationship.properties,
                relationship.name ?: key,
                relationship.type
            ) { type, props ->
                RelationshipConstraint(type, props)
            }
            relationship.constraints.assignIds("constraint", key)
            val propertyRenames = relationship.properties.assignIds("property", key)
            renames.putAll(propertyRenames)
            relationship.constraints.values.forEach { it.properties.rename(renames, key) }
            relationship.indexes.values.forEach { it.properties.rename(renames, key) }
            shorthand.forEach { (constraint, name) -> if (name.isNotBlank()) constraint.name = name }
        }
        Pretty.renameRelationshipMappingProperties(this, renames)
    }
}
