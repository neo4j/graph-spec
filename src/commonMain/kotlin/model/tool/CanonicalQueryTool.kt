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

import kotlinx.serialization.Serializable
import model.extension.StringValue
import validate.Validation
import kotlin.js.JsExport

/** The `canonicalQuery` tool type token (camelCase, per the proposal's tool table). */
const val CANONICAL_QUERY_TYPE = "canonicalQuery"

/**
 * The predefined `canonicalQuery` tool shape (ADR-0011): a Cypher query an agent runs
 * against the graph, e.g. the proposal's findCoActors
 * (`MATCH (a:Actor)-[:ACTED_IN]->(m:Movie)<-[:ACTED_IN]-(co:Actor) WHERE a.name = $name
 * RETURN co`, docs/ontology-graph-spec-v1-proposal.md). The type shape stays owner-defined
 * format-wise (proposal open item 4 — the schema's `$defs/tool` is deliberately open); this
 * typed shape is the canonicalQuery owner's contract in the SDK. [type] is fixed to
 * [CANONICAL_QUERY_TYPE]; [cypher] is the shape's only required per-type field.
 */
@JsExport
@Serializable
data class CanonicalQueryTool(
    val type: String = CANONICAL_QUERY_TYPE,
    val name: String? = null,
    val description: String? = null,
    val cypher: String
)

/**
 * The authoring bridge (ADR-0011): the open [Tool] form of this typed tool, with [cypher]
 * carried in [Tool.extra] and inlined on the wire by [ToolSerializer].
 */
fun CanonicalQueryTool.toTool(): Tool = Tool(
    type = type,
    name = name,
    description = description,
    extra = mutableMapOf("cypher" to StringValue(cypher))
)

/** The `canonicalQuery` module: wire token, payload codec, and the type's validators. */
object CanonicalQueryToolModule : ToolType<CanonicalQueryTool> {
    override val type: String = CANONICAL_QUERY_TYPE
    override val serializer = CanonicalQueryTool.serializer()
    override val validations: List<Validation> = listOf(CanonicalQueryToolShape)
}
