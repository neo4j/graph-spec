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
package model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.extension.ExtensionValue
import model.extension.Extensions
import model.node.Node
import model.relationship.Relationship
import model.spec.SpecDoc
import model.spec.SpecFormat
import model.spec.SpecMinimum
import validate.Issue
import validate.Validation
import validate.ValidationTree
import kotlin.js.JsExport
import kotlin.js.JsStatic

/**
 * Root of an ontology graph spec v1 document (ontology-graph-spec.schema.json).
 * [version] is the ontology's own version (identity metadata), not the format version;
 * the format version rides in [schema] (`$schema`).
 */
@JsExport
@Serializable
@SerialName("GraphModel")
@SpecDoc(
    "Draft JSON Schema for the Ontology Graph spec v1 proposal (2026-09-25 doc state, " +
        "working-session revision). Nodes and relationships are keyed by local ids; the " +
        "label/type lives inside the entry. Types are tokens. Tools are core. Extensions " +
        "ride the named extensions map: first-party keys, custom envelope under custom; " +
        "extension payloads are never validated by this schema."
)
data class GraphModel(
    @SerialName("\$schema")
    @SpecFormat("uri")
    @SpecDoc("Spec link including the version. Meta-level; doubles as document-type marker.")
    val schema: String,
    @SpecDoc("Unique identifier of the ontology.")
    val id: String,
    @SpecMinimum(0)
    @SpecDoc("The ontology's own version. Identity metadata, not lifecycle.")
    val version: Int,
    @SpecDoc("Human-readable name of the ontology.")
    val name: String? = null,
    @SpecDoc("Human-readable description of the ontology.")
    val description: String? = null,
    @SpecDoc(
        "Map key is the identifier within the document, not the label. The label lives in " +
            "label / labels.identifier, so label renames do not break references."
    )
    val nodes: MutableMap<String, Node> = mutableMapOf(),
    @SpecDoc(
        "Map key is the intra-document identifier; the relationship type lives in the type " +
            "field. Several keys may share a type across different endpoint pairs, but note " +
            "graph-type enforcement keys on the type alone and cannot distinguish pairs yet."
    )
    val relationships: MutableMap<String, Relationship> = mutableMapOf(),
    @SpecDoc("Named extensions map: first-party neo4j:* keys; custom envelopes under the reserved custom key.")
    override val extensions: MutableMap<String, ExtensionValue> = mutableMapOf()
) : Extensions {
    @JsExport.Ignore
    fun validate(validators: List<Validation>): List<Issue> {
        val tree = ValidationTree()
        tree.build(validators)
        return tree.validate(this)
    }

    companion object {
        @JsStatic
        fun validate(model: GraphModel, validators: List<Validation>) = model.validate(validators)
    }
}
