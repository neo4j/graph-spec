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

import model.Rename.prettify
import model.Rename.rename
import model.mapping.NodeMapping
import model.mapping.RelationshipMapping
import model.node.Constraint
import model.node.Node
import model.property.Property
import model.relationship.Relationship
import model.type.ConstraintType
import model.type.Named
import kotlin.collections.component1
import kotlin.collections.component2

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
        display.nodes.rename(renames)
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
            prettifyProperties(node.constraints, node.properties) { constraint ->
                node.labels.identifier ==
                    constraint.label
            }
        }
        renameNodeMappingProperties(this, renames)
        renameTargetNodeProperties(this, renames)
    }

    private fun <C : Constraint> prettifyProperties(
        constraints: MutableMap<String, C>,
        properties: MutableMap<String, Property>,
        check: (C) -> Boolean = {
            true
        }
    ) {
        val prettifiedConstraints = mutableSetOf<String>()
        for ((key, constraint) in constraints) {
            if (constraint.properties.size == 1 && check(constraint)) { // TODO and using generated constraint name
                val propertyId = constraint.properties.first()
                val property = properties[propertyId] ?: continue
                when (constraint.type) {
                    ConstraintType.EXISTS -> property.mustExist = true
                    ConstraintType.KEY -> property.key = true
                    ConstraintType.UNIQUE -> property.unique = true
                    else -> continue
                }
                prettifiedConstraints.add(key)
            }
        }
        prettifiedConstraints.forEach { key ->
            constraints.remove(key)
        }
    }

    internal fun renameTargetNodeProperties(model: GraphModel, renames: MutableMap<String, String>) {
        model.relationships.values.forEach { relationship ->
            relationship.from.property =
                renames["${relationship.from.node}:${relationship.from.property}"] ?: relationship.from.property
            relationship.to.property =
                renames["${relationship.to.node}:${relationship.to.property}"] ?: relationship.to.property
        }
    }

    internal fun renameNodeMappingProperties(model: GraphModel, renames: Map<String, String>) {
        model.mappings.filterIsInstance<NodeMapping>().forEach { mapping ->
            mapping.properties.rename(renames, mapping.node)
            mapping.key.rename(renames, mapping.node)
        }
        model.mappings.filterIsInstance<RelationshipMapping>().forEach { mapping ->
            mapping.from.properties.rename(renames, mapping.from.node)
            mapping.to.properties.rename(renames, mapping.to.node)
            mapping.key.rename(renames, mapping.from.node)
            mapping.key.rename(renames, mapping.to.node)
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
            prettifyProperties(relationship.constraints, relationship.properties)
        }
        renameRelationshipMappingProperties(this, renames)
    }

    internal fun renameRelationshipMappingProperties(model: GraphModel, renames: Map<String, String>) {
        model.mappings.filterIsInstance<RelationshipMapping>().forEach { mapping ->
            mapping.properties.rename(renames, mapping.relationship)
            mapping.key.rename(renames, mapping.relationship)
        }
    }
}
