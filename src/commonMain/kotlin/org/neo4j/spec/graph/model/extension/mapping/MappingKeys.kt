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
package org.neo4j.spec.graph.model.extension.mapping

/**
 * Converts [columns] to the ids of the properties mapped to them.
 *
 * @throws IllegalArgumentException if a column has no property mapped to it.
 */
internal fun columnsToProperties(columns: Set<String>, properties: Map<String, PropertyMapping>): MutableSet<String> {
    val result = mutableSetOf<String>()
    for (column in columns) {
        val ids = properties.filterValues { it.column == column }.keys
        require(ids.isNotEmpty()) { "Key column '$column' is not mapped to any property" }
        result.addAll(ids)
    }
    return result
}

/**
 * Converts the ids in [propertyIds] to the columns they are mapped to.
 *
 * @throws IllegalArgumentException if a property is not mapped to a column.
 */
internal fun propertiesToColumns(
    propertyIds: Set<String>,
    properties: Map<String, PropertyMapping>
): MutableSet<String> = propertyIds.mapTo(mutableSetOf()) { id ->
    requireNotNull(properties[id]) { "Key property '$id' is not mapped to a column" }.column
}

/**
 * What is needed beyond a mapping itself to convert the node keys of its relationship targets.
 *
 * @property endpoints the ids of the start and end nodes of a relationship.
 * @property nodePropertyMappings the property mappings of every mapping of a node.
 */
internal class KeyConversionContext(
    val endpoints: (relationshipId: String) -> Pair<String, String>?,
    val nodePropertyMappings: (nodeId: String) -> List<Map<String, PropertyMapping>>
)

/**
 * Works out the conversion of [mapping] to key properties or key columns without changing it, so a failure leaves
 * everything unchanged. Running the returned function applies the conversion.
 *
 * @return a lambda method with the changes so the caller can change atomically, checking all mappings are valid
 * before making any changes.
 *
 * @throws IllegalArgumentException if a key can't be converted.
 */
internal fun planKeyConversion(mapping: Mapping, context: KeyConversionContext, toProperties: Boolean): () -> Unit =
    when (mapping) {
        is NodeMapping -> planKeys(mapping.properties, mapping.keyColumns, mapping.keyProperties, toProperties)
        is RelationshipMapping -> {
            val own = planKeys(mapping.properties, mapping.keyColumns, mapping.keyProperties, toProperties)
            val endpoints = context.endpoints(mapping.relationship)
            val from = planTarget(mapping.fromNode, endpoints?.first, context, toProperties)
            val to = planTarget(mapping.toNode, endpoints?.second, context, toProperties)
            return {
                own()
                from()
                to()
            }
        }
        is QueryMapping -> return {}
    }

internal fun planKeys(
    properties: Map<String, PropertyMapping>,
    columns: MutableSet<String>,
    keys: MutableSet<String>,
    toProperties: Boolean
): () -> Unit {
    if (toProperties) {
        if (columns.isEmpty()) {
            return {}
        }
        val converted = columnsToProperties(columns, properties)
        return {
            keys.clear()
            keys.addAll(converted)
            columns.clear()
        }
    }
    if (keys.isEmpty()) {
        return {}
    }
    val converted = propertiesToColumns(keys, properties)
    return {
        columns.clear()
        columns.addAll(converted)
        keys.clear()
    }
}

private fun planTarget(
    target: TargetMapping,
    nodeId: String?,
    context: KeyConversionContext,
    toProperties: Boolean
): () -> Unit {
    if (toProperties) {
        if (target.nodeKeyColumn.isEmpty()) {
            return {}
        }
        val column = target.nodeKeyColumn
        val property = context.nodePropertyMappings(requireNotNull(nodeId) { "Relationship of mapping not found" })
            .firstNotNullOfOrNull { mappings -> mappings.entries.firstOrNull { it.value.column == column }?.key }
        requireNotNull(property) { "Node key column '$column' is not mapped to any property of node '$nodeId'" }
        return {
            target.nodeKeyProperty = property
            target.nodeKeyColumn = ""
        }
    }
    if (target.nodeKeyProperty.isEmpty()) {
        return {}
    }
    val property = target.nodeKeyProperty
    val column = context.nodePropertyMappings(requireNotNull(nodeId) { "Relationship of mapping not found" })
        .firstNotNullOfOrNull { it[property]?.column }
    requireNotNull(column) { "Node key property '$property' is not mapped to a column of node '$nodeId'" }
    return {
        target.nodeKeyColumn = column
        target.nodeKeyProperty = ""
    }
}
