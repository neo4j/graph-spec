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
package model.extension.neo4j.index

import kotlinx.serialization.Serializable
import model.extension.NamedExtension
import validate.Validation
import kotlin.js.JsExport

/** Index type tokens (`type` field): lowercase, matching the v1 token style. */
@JsExport
object IndexType {
    const val FULLTEXT = "fulltext"
    const val POINT = "point"
    const val RANGE = "range"
    const val TEXT = "text"
    const val VECTOR = "vector"
    const val LOOKUP = "lookup"
}

/**
 * The `neo4j:index` named extension (ADR-0005): indexes on node or relationship
 * properties, kept out of the v1 core format. The payload under the `neo4j:index`
 * key is one object OR a list of objects — a single object is a one-element list —
 * at any extension level: root, node, relationship, or property. Optional fields
 * carry the 4.0.0 index surface (options per type) so 4.0.0-era payloads decode;
 * the minimal v1 form is `{ name?, type?, properties }` (see the movies/org
 * examples).
 */
@JsExport
@Serializable
data class IndexExtension(
    val name: String? = null,
    val type: String? = null,
    val properties: List<String> = emptyList(),
    val options: IndexOptions? = null
)

/**
 * Index provider options, one group per type; a group is only meaningful with the
 * matching [IndexExtension.type] (enforced by [IndexOptionsMatch]). Field names keep
 * the 4.0.0 `IndexOption` wire keys so existing option payloads decode unchanged.
 */
@JsExport
@Serializable
data class IndexOptions(
    val fulltext: FullTextIndexOptions? = null,
    val point: PointIndexOptions? = null,
    val vector: VectorIndexOptions? = null
)

@JsExport
@Serializable
data class FullTextIndexOptions(
    val defaultAnalyzer: String = "standard-no-stop-words",
    val analyzer: String? = null,
    val eventuallyConsistent: Boolean = false
)

@JsExport
@Serializable
data class PointIndexOptions(
    val cartesianMin: List<Double>? = null,
    val cartesianMax: List<Double>? = null,
    val wgs84Min: List<Double>? = null,
    val wgs84Max: List<Double>? = null
)

@JsExport
@Serializable
data class VectorIndexOptions(val dimensions: Int? = null, val similarityFunction: String = "cosine")

/**
 * The `neo4j:index` module: wire key, payload codec, and the extension's validators.
 * The payload is one index object or a list of them (ADR-0005's object-or-list
 * rule), legal at the root, node, relationship, and property levels; typed access
 * goes through [model.extension.getNamedList], which normalises a single object to
 * a one-element list.
 */
object IndexExtensionModule : NamedExtension<IndexExtension> {
    override val key: String = "neo4j:index"
    override val serializer = IndexExtension.serializer()
    override val validations: List<Validation> =
        listOf(IndexPropertyReferences, IndexOptionsMatch, IndexConstraintNameConflict)
}
