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
package model.node

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.spec.SpecDoc
import model.spec.SpecEnum
import model.spec.SpecMinItems
import kotlin.js.JsExport

/**
 * v1 constraint object (ontology-graph-spec.schema.json `$defs.constraint`): the nameable
 * alternative to the `mustExist`/`unique`/`key` property shorthand flags. Shared by
 * node and relationship entries; [type] is a [model.type.ConstraintType] token.
 */
@JsExport
@Serializable
@SerialName("Constraint")
@SpecDoc(
    "Constraint object form (the alternative to the mustExist/unique/key shorthand flags). " +
        "Nameable, so tooling can reference individual constraints."
)
data class Constraint(
    @SerialName("constraint_type")
    @SpecEnum(["key", "unique", "mustExist"])
    @SpecDoc("The constraint kind: key (unique + mustExist), unique, or mustExist.")
    var type: String,
    @SpecMinItems(1)
    @SpecDoc("Names of properties on the same element this constraint covers.")
    val properties: MutableList<String>,
    @SpecDoc("Optional name, so tooling can reference the constraint.")
    var name: String? = null
)
