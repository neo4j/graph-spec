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
package model.tool

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.extension.ExtensionValue
import kotlin.js.JsExport

/**
 * Core v1 tool definition (ontology-spec.schema.json `$defs/tool`, additionalProperties:true):
 * [type] discriminates (`canonicalQuery`, `externalRequest`, ...), [name] and [description]
 * are the readable surface. Remaining per-type fields are owner-defined and carried in
 * [extra], inlined on the wire (e.g. `cypher`, `url`). Definitions only, no behaviour.
 */
@JsExport
@Serializable(with = ToolSerializer::class)
@SerialName("Tool")
data class Tool(
    var type: String,
    var name: String? = null,
    var description: String? = null,
    val extra: MutableMap<String, ExtensionValue> = mutableMapOf(),
)
