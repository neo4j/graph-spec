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

import model.GraphModel
import model.property.Property
import validate.Issue
import validate.Validation
import validate.forEachProperty

private const val SCALAR_TOKENS =
    "STRING|INTEGER|FLOAT|BOOLEAN|DATE|TIME|LOCALTIME|DATETIME|LOCALDATETIME|DURATION|POINT|BYTES"

// Keep identical to ontology-graph-spec.schema.json $defs.propertyType.pattern.
private val TYPE_TOKEN = Regex("^(ANY|LIST<ANY>|($SCALAR_TOKENS)|(LIST|VECTOR)<($SCALAR_TOKENS)>)$")

/**
 * v1 rule: a property's `type` is a schema type token — a scalar, `ANY`, `LIST<ANY>`,
 * `LIST<scalar>` or `VECTOR<scalar>`; element types are scalars only, no union types
 * (ontology-graph-spec.schema.json `$defs.propertyType.pattern`, mirrored verbatim in
 * [TYPE_TOKEN]; docs/ontology-graph-spec-v1-proposal.md §Data types) — and `dimension` rides
 * only on `VECTOR<...>` as its companion, minimum 1
 * (ontology-graph-spec.schema.json `$defs.property.dimension`: "VECTOR companion: element
 * count. Absent = unconstrained."). The schema enforces both on parsed documents; the
 * Kotlin model carries plain strings and ints, so a programmatically built model can
 * violate them — this is the model-level check, run on node and relationship
 * properties alike.
 */
object PropertyTypeToken : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        model.forEachProperty { path, property ->
            validateProperty(path, property, issues)
        }
    }

    private fun validateProperty(path: String, property: Property, issues: MutableList<Issue>) {
        val type = property.type
        if (type != null && !TYPE_TOKEN.matches(type)) {
            issues.add(
                Issue(
                    code = "invalid_property_type",
                    message = "Property type '$type' is not a v1 type token",
                    path = "$path.type"
                )
            )
        }
        val dimension = property.dimension ?: return
        if (type == null || !type.startsWith("VECTOR<")) {
            issues.add(
                Issue(
                    code = "dimension_on_non_vector_property",
                    message = "Property dimension is the VECTOR companion but type is '${type ?: "absent"}'",
                    path = "$path.dimension"
                )
            )
        }
        if (dimension < 1) {
            issues.add(
                Issue(
                    code = "invalid_property_dimension",
                    message = "Property dimension must be >= 1, was $dimension",
                    path = "$path.dimension"
                )
            )
        }
    }
}
