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

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.node.extension.LabelsExtensions
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName("Labels")
@SerialDescription("The labels of a node.")
data class Labels(
    @SerialDescription("The label which identifies the node.")
    var identifier: String? = null,
    @SerialDescription("Labels implied by the node in addition to the identifier.")
    val implied: MutableSet<String> = mutableSetOf(),
    @SerialDescription("Labels which may be present on the node but aren't required.")
    val optional: MutableSet<String> = mutableSetOf(),
    @SerialDescription("Extensions for the labels.")
    val extensions: LabelsExtensions = LabelsExtensions()
)
