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
import model.spec.SpecRequired
import kotlin.js.JsExport

/**
 * v1 `labels` object (ontology-graph-spec.schema.json `$defs.labels`, additionalProperties:false):
 * the identifying label plus implied and optional labels. A node with no implied/optional
 * labels uses the `label` shorthand instead.
 */
@JsExport
@Serializable
@SerialName("Labels")
@SpecDoc(
    "The full labels object: identifier (the main label) plus implied and optional " +
        "labels. A node with no implied/optional labels uses the label shorthand instead."
)
data class Labels(
    @SpecRequired
    @SpecDoc("The identifying (main) label.")
    var identifier: String? = null,
    @SpecDoc("Labels entailed by the identifying label (documented, never inferred).")
    val implied: MutableSet<String> = mutableSetOf(),
    @SpecDoc("Labels that may be present on instances but are not guaranteed.")
    val optional: MutableSet<String> = mutableSetOf()
)
