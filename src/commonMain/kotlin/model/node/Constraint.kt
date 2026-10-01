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
import kotlin.js.JsExport

/**
 * v1 constraint object (ontology-spec.schema.json `$defs.constraint`): the nameable
 * alternative to the `mustExist`/`unique`/`key` property shorthand flags. Shared by
 * node and relationship entries; [type] is a [model.type.ConstraintType] token.
 */
@JsExport
@Serializable
@SerialName("Constraint")
data class Constraint(
    @SerialName("constraint_type")
    var type: String,
    val properties: MutableList<String>,
    var name: String? = null
)
