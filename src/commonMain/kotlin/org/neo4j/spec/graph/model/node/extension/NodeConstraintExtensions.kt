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
package org.neo4j.spec.graph.model.node.extension

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import org.neo4j.spec.graph.model.extension.Extensions
import org.neo4j.spec.graph.model.value.ExtensionValue
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("NodeConstraintExtensions")
@SerialDescription("Extensions for a node constraint.")
data class NodeConstraintExtensions(
    @SerialDescription("User-defined arbitrary data.")
    override val custom: MutableMap<String, ExtensionValue> = mutableMapOf()
) : Extensions
