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

import model.mapping.NodeMapping
import model.mapping.RelationshipMapping
import model.node.NodeConstraint
import model.relationship.RelationshipConstraint
import model.type.ConstraintType
import model.type.Named
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

/**
 * Removes any stable ids in favour of human-readable [Named.name]'s.
 */
object Pretty {
    fun prettify(model: GraphModel) {
        model.prettifyNodeLabels()
        model.prettifyNodeProperties()
        model.prettifyNodes()
        model.prettifyRelationshipProperties()
        model.prettifyRelationships()
        removeNames(model)
        model.pretty = true
    }

    private fun removeNames(model: GraphModel) {
        model.nodes.values.forEach { node ->
            node.properties.values.forEach { it.name = null }
            node.constraints.values.forEach { it.name = null }
            node.indexes.values.forEach { it.name = null }
            node.name = null
        }
        model.relationships.values.forEach { relationship ->
            relationship.properties.values.forEach { it.name = null }
            relationship.constraints.values.forEach { it.name = null }
            relationship.indexes.values.forEach { it.name = null }
            relationship.name = null
        }
        model.tables.values.forEach { table ->
            table.fields.values.forEach { it.name = null }
        }
    }

    /*
        Nodes
     */

    private fun GraphModel.prettifyNodeLabels() {
        nodes.values.forEach { node ->
            if (node.labels.implied.isEmpty() && node.labels.optional.isEmpty()) {
                if (node.labels.identifier != null) {
                    node.label = node.labels.identifier
                    node.labels.identifier = null
                }
            }
        }
    }

    private fun GraphModel.prettifyNodes() {
        val renames = nodes.prettify()
        renameNodeMappings(this, renames)
        nodes.values.forEach { node ->
            node.constraints.prettify()
            node.indexes.prettify()
        }
        relationships.values.forEach { relationship ->
            if (relationship.from.node != "") {
                relationship.from.node = renames[relationship.from.node] ?: relationship.from.node
            }
            if (relationship.to.node != "") {
                relationship.to.node = renames[relationship.to.node] ?: relationship.to.node
            }
        }
    }

    internal fun renameNodeMappings(model: GraphModel, renames: Map<String, String>) {
        model.mappings.filterIsInstance<NodeMapping>().forEach { mapping ->
            mapping.node = renames[mapping.node] ?: mapping.node
        }
        model.mappings.filterIsInstance<RelationshipMapping>().forEach { mapping ->
            mapping.from.node = renames[mapping.from.node] ?: mapping.from.node
            mapping.to.node = renames[mapping.to.node] ?: mapping.to.node
        }
    }

    private fun GraphModel.prettifyNodeProperties() {
        val renames = mutableMapOf<String, String>()
        nodes.forEach { (key, node) ->
            renames.putAll(node.properties.prettify(key))
            node.constraints.values.forEach { property ->
                property.properties.rename(renames, key)
            }
            node.indexes.values.forEach { property ->
                property.properties.rename(renames, key)
            }
            for ((key, property) in node.properties) {
                if (property.key == true) {
                    val duplicate = node.constraints.toList().firstOrNull { it.second.type == ConstraintType.UNIQUE || it.second.type == ConstraintType.EXISTS && it.second.properties.singleOrNull() == key }
                    if (duplicate != null) {
                        node.constraints.remove(duplicate.first)
                    }
                    node.constraints["${key}_key"] = NodeConstraint(
                        ConstraintType.KEY,
                        properties = mutableSetOf(key),
                    )
                    property.key = null
                } else {
                    if (property.unique == true) {
                        node.constraints["${key}_unique"] = NodeConstraint(
                            ConstraintType.UNIQUE,
                            properties = mutableSetOf(key),
                        )
                        property.unique = null
                    }
                    if (property.mustExist == true) {
                        node.constraints["${key}_exists"] = NodeConstraint(
                            ConstraintType.EXISTS,
                            properties = mutableSetOf(key),
                        )
                        property.mustExist = null
                    }
                }
            }
        }
        renameNodeMappingProperties(this, renames)
    }

    internal fun renameNodeMappingProperties(model: GraphModel, renames: Map<String, String>) {
        model.mappings.filterIsInstance<NodeMapping>().forEach { mapping ->
            mapping.properties.rename(renames, mapping.node)
        }
        model.mappings.filterIsInstance<RelationshipMapping>().forEach { mapping ->
            mapping.from.properties.rename(renames, mapping.from.node)
            mapping.to.properties.rename(renames, mapping.to.node)
        }
    }

    /*
        Relationships
     */

    private fun GraphModel.prettifyRelationships() {
        val renames = relationships.prettify()
        renameRelationshipMappings(this, renames)
        relationships.values.forEach { node ->
            node.constraints.prettify()
            node.indexes.prettify()
        }
    }

    internal fun renameRelationshipMappings(model: GraphModel, renames: Map<String, String>) {
        model.mappings.filterIsInstance<RelationshipMapping>().forEach { mapping ->
            mapping.relationship = renames[mapping.relationship] ?: mapping.relationship
        }
    }

    private fun GraphModel.prettifyRelationshipProperties() {
        val renames = mutableMapOf<String, String>()
        relationships.forEach { (key, relationship) ->
            renames.putAll(relationship.properties.prettify(key))
            relationship.constraints.values.forEach { property ->
                property.properties.rename(renames, key)
            }
            relationship.indexes.values.forEach { property ->
                property.properties.rename(renames, key)
            }
            for ((key, property) in relationship.properties) {
                if (property.key == true) {
                    val duplicate = relationship.constraints.toList().firstOrNull { it.second.type == ConstraintType.UNIQUE || it.second.type == ConstraintType.EXISTS && it.second.properties.singleOrNull() == key }
                    if (duplicate != null) {
                        relationship.constraints.remove(duplicate.first)
                    }
                    relationship.constraints["${key}_key"] = RelationshipConstraint(
                        ConstraintType.KEY,
                        properties = mutableSetOf(key),
                    )
                    property.key = null
                } else {
                    if (property.unique == true) {
                        relationship.constraints["${key}_unique"] = RelationshipConstraint(
                            ConstraintType.UNIQUE,
                            properties = mutableSetOf(key),
                        )
                        property.unique = null
                    }
                    if (property.mustExist == true) {
                        relationship.constraints["${key}_exists"] = RelationshipConstraint(
                            ConstraintType.EXISTS,
                            properties = mutableSetOf(key),
                        )
                        property.mustExist = null
                    }
                }
            }
        }
        renameRelationshipMappingProperties(this, renames)
    }

    internal fun renameRelationshipMappingProperties(model: GraphModel, renames: Map<String, String>) {
        model.mappings.filterIsInstance<RelationshipMapping>().forEach { mapping ->
            mapping.properties.rename(renames, mapping.relationship)
        }
    }

    /**
     * Goes through a MutableMap, replacing the keys with replacements from [renames]
     * @param parent optionally used to look up the replacement key
     */
    private fun <T> MutableMap<String, T>.rename(renames: Map<String, String>, parent: String? = null) {
        val original = toMutableMap()
        clear()
        for ((og, value) in original) {
            val key = if (parent != null) {
                renames["$parent:$og"]
            } else {
                renames[og]
            } ?: og
            this[key] = value
        }
    }

    /**
     * Goes through a MutableSet, replacing the keys with replacements from [renames]
     * @param parent optionally used to look up the replacement key
     */
    private fun MutableSet<String>.rename(renames: Map<String, String>, parent: String? = null) {
        val original = toMutableSet()
        clear()
        for (og in original) {
            val key = if (parent != null) {
                renames["$parent:$og"]
            } else {
                renames[og]
            } ?: og
            add(key)
        }
    }

    /**
     * Removes any [Named.name]'s and places them as the key in the MutableMap
     * @param parent name to use as a key prefix in the @return map
     * @return Map of original keys to their replacements
     */
    private fun <T : Named> MutableMap<String, T>.prettify(parent: String? = null): Map<String, String> {
        val original = toMutableMap()
        clear()
        val changes = mutableMapOf<String, String>()
        for ((og, node) in original) {
            val key = node.name
            if (key == null) {
                this[og] = node
                continue
            }
            node.name = null
            this[key] = node
            if (parent != null) {
                changes["$parent:$og"] = key
            } else {
                changes[og] = key
            }
        }
        return changes
    }
}
