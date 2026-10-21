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
