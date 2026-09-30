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
package validate.relationship

import model.GraphModel
import model.property.Property
import model.relationship.Relationship
import validate.Issue
import validate.property.MAX_DIMENSION
import validate.property.MIN_DIMENSION
import validate.property.isVectorType

object RelationshipPropertyVectorDimensionBounds : RelationshipValidation {
    override fun validateProperty(
        model: GraphModel,
        relationshipId: String,
        relationship: Relationship,
        propertyId: String,
        property: Property,
        issues: MutableList<Issue>
    ) {
        // Bounds: UPX MIN_DIMENSION/MAX_DIMENSION (details-panel/constants.ts#L46-L47).
        val dimension = property.dimension
        if (isVectorType(property.type) && dimension != null &&
            (dimension < MIN_DIMENSION || dimension > MAX_DIMENSION)
        ) {
            issues.add(
                Issue(
                    code = "invalid_vector_dimension",
                    message = "Vector property '$propertyId' on relationship '$relationshipId' has dimension " +
                        "$dimension outside $MIN_DIMENSION-$MAX_DIMENSION",
                    path = "relationships.$relationshipId.properties.$propertyId.dimension"
                )
            )
        }
    }
}
