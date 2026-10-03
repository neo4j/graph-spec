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
package model.extension.importer.mapping

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.extension.NamedExtension
import validate.Validation
import kotlin.js.JsExport

/** Mapping kind tokens (`kind` field): lowercase, matching the v1 token style. */
@JsExport
object MappingKind {
    const val NODE = "node"
    const val RELATIONSHIP = "relationship"
    const val QUERY = "query"
}

/** Mapping mode tokens (`mode` field): lowercase, matching the v1 token style. */
@JsExport
object MappingMode {
    const val MERGE = "merge"
    const val CREATE = "create"
}

/**
 * The `importer:mapping` named extension (ADR-0005): how source tables map onto
 * node/relationship types, kept out of the v1 core format. Lives at the document root
 * (v1 placement rule). The minimal v1 form is `{ kind: "node", node, table, key }`
 * (see the movies example); [properties]/[mode]/[matchLabel] and the relationship
 * endpoint mappings carry the 4.0.0 mapping surface so 4.0.0-era payloads decode.
 * `start_node`/`end_node` keep 4.0.0's final wire names (graph-spec #103).
 */
@JsExport
@Serializable
data class MappingExtension(
    @SerialName("\$schema")
    val schema: String? = null,
    val kind: String,
    val node: String? = null,
    val relationship: String? = null,
    val table: String? = null,
    val query: String? = null,
    val key: Set<String> = emptySet(),
    val properties: Map<String, PropertyMapping> = emptyMap(),
    val mode: String? = null,
    val matchLabel: String? = null,
    @SerialName("start_node")
    val startNode: TargetMapping? = null,
    @SerialName("end_node")
    val endNode: TargetMapping? = null
)

@JsExport
@Serializable
data class PropertyMapping(val column: String)

@JsExport
@Serializable
data class TargetMapping(val node: String, val properties: Map<String, PropertyMapping> = emptyMap())

/** The `importer:mapping` module: wire key, payload codec, and the mapping validators. */
object MappingExtensionModule : NamedExtension<MappingExtension> {
    override val key: String = "importer:mapping"
    override val serializer = MappingExtension.serializer()
    override val validations: List<Validation> =
        listOf(MappingReferences, MappingKeyPresent, MappingKeyType)
}
