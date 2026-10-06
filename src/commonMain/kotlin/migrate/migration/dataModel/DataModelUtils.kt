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
package migrate.migration.dataModel

import codec.schema.SchemaMap
import codec.schema.schemaMapOf
import model.type.ConstraintType
import model.type.ConstraintType.EXISTS
import model.type.ConstraintType.KEY
import model.type.ConstraintType.PROPERTY_TYPE
import model.type.ConstraintType.UNIQUE
import model.type.IndexType
import model.type.IndexType.FULLTEXT
import model.type.IndexType.LOOKUP
import model.type.IndexType.POINT
import model.type.IndexType.RANGE
import model.type.IndexType.TEXT
import model.type.IndexType.VECTOR

internal fun SchemaMap.ref() = string("\$ref").removePrefix("#")

internal fun SchemaMap.ref(key: String) = map(key).ref()

internal fun SchemaMap.id() = string("\$id")

internal fun unwrap(schema: SchemaMap): SchemaMap {
    if (schema.containsKey("dataModel")) {
        val model = schema.map("dataModel")
        schema.remove("dataModel")
        schema.remove("version")
        model.putAll(schema)
        return model
    }
    return schema
}

internal fun refOf(id: String) = schemaMapOf("\$ref" to "#${id.removePrefix("#")}")

internal fun dataModelConstraintTypeFrom(word: String): ConstraintType? = when (word) {
    "uniqueness" -> UNIQUE
    "propertyExistence" -> EXISTS
    "propertyType" -> PROPERTY_TYPE
    "key" -> KEY
    else -> null
}

internal fun dataModelIndexTypeFrom(word: String): IndexType? = when (word.lowercase()) {
    "lookup" -> LOOKUP
    "default", "range" -> RANGE
    "fulltext" -> FULLTEXT
    "point" -> POINT
    "text" -> TEXT
    "vector" -> VECTOR
    else -> null
}
