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
package org.neo4j.spec.graph.v1.migrate.migration.dataModel

import org.neo4j.spec.graph.v1.codec.schema.SchemaMap
import org.neo4j.spec.graph.v1.codec.schema.schemaMapOf
import org.neo4j.spec.graph.v1.model.type.ConstraintType
import org.neo4j.spec.graph.v1.model.type.ConstraintType.EXISTS
import org.neo4j.spec.graph.v1.model.type.ConstraintType.KEY
import org.neo4j.spec.graph.v1.model.type.ConstraintType.PROPERTY_TYPE
import org.neo4j.spec.graph.v1.model.type.ConstraintType.UNIQUE
import org.neo4j.spec.graph.v1.model.type.IndexType
import org.neo4j.spec.graph.v1.model.type.IndexType.FULLTEXT
import org.neo4j.spec.graph.v1.model.type.IndexType.LOOKUP
import org.neo4j.spec.graph.v1.model.type.IndexType.POINT
import org.neo4j.spec.graph.v1.model.type.IndexType.RANGE
import org.neo4j.spec.graph.v1.model.type.IndexType.TEXT
import org.neo4j.spec.graph.v1.model.type.IndexType.VECTOR

internal fun SchemaMap.ref() = string("\$ref").removePrefix("#")

internal fun SchemaMap.ref(key: String) = map(key).ref()

internal fun SchemaMap.id() = string("\$id")

internal fun refOf(id: String) = schemaMapOf("\$ref" to "#${id.removePrefix("#")}")

internal fun dataModelConstraintTypeFrom(word: String): ConstraintType? = when (word) {
    "uniqueness" -> UNIQUE
    "propertyExistence" -> EXISTS
    "propertyType" -> PROPERTY_TYPE
    "key" -> KEY
    else -> null
}

internal fun dataModelIndexTypeFrom(word: String): IndexType? = when (word) {
    "lookup" -> LOOKUP
    "default", "range" -> RANGE
    "fullText" -> FULLTEXT
    "point" -> POINT
    "text" -> TEXT
    "vector" -> VECTOR
    else -> null
}
