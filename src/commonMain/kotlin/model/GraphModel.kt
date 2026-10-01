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
import validate.Issue
import validate.Validation
import validate.ValidationTree
import kotlin.js.JsExport
import kotlin.js.JsStatic

/**
 * Root of an ontology spec v1 document (ontology-spec.schema.json).
 * [version] is the ontology's own version (identity metadata), not the format version;
 * the format version rides in [schema] (`$schema`).
 */
@JsExport
@Serializable
@SerialName("GraphModel")
data class GraphModel(
    @SerialName("\$schema")
    val schema: String,
    val id: String,
    val version: Int,
    val name: String? = null,
    val description: String? = null,
    val nodes: MutableMap<String, Node> = mutableMapOf(),
    val relationships: MutableMap<String, Relationship> = mutableMapOf(),
    override val extensions: MutableMap<String, ExtensionValue> = mutableMapOf(),
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
