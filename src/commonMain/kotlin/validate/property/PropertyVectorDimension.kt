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
package validate.property

import model.property.Neo4jType
import model.property.Property
import validate.Issue

// UPX rule 8: getVectorDimensionPropertyErrors (errors.ts lines 124-130).
// Flags a property when its type is a VECTOR variant and dimension is null.
internal fun vectorDimensionIssue(ownerKind: String, ownerId: String, propertyId: String, property: Property): Issue? {
    val type = property.type
    if (!isVectorType(type) || property.dimension != null) return null
    return Issue(
        code = "missing_vector_dimension",
        message = "Property '$propertyId' on '$ownerId' is a vector type but has no dimension set",
        path = "$ownerKind.$ownerId.properties.$propertyId.dimension"
    )
}

private fun isVectorType(type: Neo4jType): Boolean = when (type) {
    Neo4jType.VECTOR_FLOAT,
    Neo4jType.VECTOR_FLOAT32,
    Neo4jType.VECTOR_INTEGER,
    Neo4jType.VECTOR_INTEGER32,
    Neo4jType.VECTOR_INTEGER16,
    Neo4jType.VECTOR_INTEGER8 -> true
    else -> false
}
