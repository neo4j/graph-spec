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

import org.neo4j.spec.graph.model.toMap

@JsExport
class RelationshipMappingEditor {
    companion object {
        /**
         * Identifies the relationship by key properties instead of key columns. Does nothing if there are no key columns.
         *
         * @throws IllegalArgumentException if a key column is not mapped to a property, leaving the mapping unchanged.
         */
        @JsStatic
        fun useKeyProperties(mapping: RelationshipMappingJs) {
            if (mapping.keyColumns.isEmpty()) return
            val converted = columnsToProperties(mapping.keyColumns.toSet(), mapping.properties.toMap().mapValues { it.value.toClass() })
            mapping.keyProperties = converted.toTypedArray()
            mapping.keyColumns = emptyArray()
        }

        /**
         * Identifies the relationship by key columns instead of key properties. Does nothing if there are no key properties.
         *
         * @throws IllegalArgumentException if a key property is not mapped to a column, leaving the mapping unchanged.
         */
        @JsStatic
        fun useKeyColumns(mapping: RelationshipMappingJs) {
            if (mapping.keyProperties.isEmpty()) return
            val converted = propertiesToColumns(mapping.keyProperties.toSet(), mapping.properties.toMap().mapValues { it.value.toClass() })
            mapping.keyColumns = converted.toTypedArray()
            mapping.keyProperties = emptyArray()
        }
    }
}
