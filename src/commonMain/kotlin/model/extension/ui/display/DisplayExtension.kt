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
package model.extension.ui.display

import kotlinx.serialization.Serializable
import model.extension.NamedExtension
import validate.Validation
import kotlin.js.JsExport

/**
 * The `ui:display` named extension (ADR-0005): presentation hints for a node or
 * relationship type, kept out of the v1 core format. The minimal v1 form is
 * `{ color?, caption?, icon? }` (see the movies/org examples); [x]/[y] carry 4.0.0's
 * canvas coordinates so 4.0.0-era display payloads decode, though an ontology
 * document has no canvas and consumers should treat them as advisory.
 *
 * Display is presentation-only: no validators (4.0.0 had none either).
 */
@JsExport
@Serializable
data class DisplayExtension(
    val color: String? = null,
    val caption: String? = null,
    val icon: String? = null,
    val x: Double? = null,
    val y: Double? = null
)

/** The `ui:display` module: wire key, payload codec, no validators. */
object DisplayExtensionModule : NamedExtension<DisplayExtension> {
    override val key: String = "ui:display"
    override val serializer = DisplayExtension.serializer()
    override val validations: List<Validation> = emptyList()
}
