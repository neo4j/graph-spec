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
package validate.node

import model.GraphModel
import model.node.Node
import model.property.Property
import validate.Issue
import validate.property.MAX_DIMENSION
import validate.property.MIN_DIMENSION
import validate.property.isVectorType

object NodePropertyVectorDimension : NodeValidation {
    override fun validateProperty(
        model: GraphModel,
        nodeId: String,
        node: Node,
        propertyId: String,
        property: Property,
        issues: MutableList<Issue>
    ) {
        // UPX: `getVectorDimensionPropertyErrors` (errors.ts#L124-L130).
        if (isVectorType(property.type) && property.dimension == null) {
            issues.add(
                Issue(
                    code = "missing_vector_dimension",
                    message = "Missing dimension for vector property '$propertyId' on node '$nodeId'",
                    path = "nodes.$nodeId.properties.$propertyId.dimension"
                )
            )
        }
    }
}
