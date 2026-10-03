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
package model.extension

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import model.spec.SpecDoc
import model.spec.SpecFormat
import kotlin.js.JsExport

/**
 * The `$defs/extension` description. A const because CustomExtensionSerializer's
 * hand-built descriptor does not carry the class annotations — the schema generator
 * reads this const directly (ADR-0003's enumerated overrides), so the text lives
 * exactly once, next to the class.
 */
const val CUSTOM_EXTENSION_SPEC_DOC =
    "Custom extension envelope, four fields: type required; \$schema, name, definition " +
        "optional. definition is free-form and never validated by this spec. Unknown extra " +
        "fields are carried untouched."

/**
 * The fixed custom-extension envelope (ADR-0005; ontology-graph-spec.schema.json `$defs.extension`):
 * [type] required; [schema] (`$schema`), [name], [definition] optional; [definition] is
 * free-form and never validated. Unknown extra fields are carried untouched in [extra].
 * On the wire the reserved `custom` key of an extensions map holds a list of envelopes.
 */
@JsExport
@Serializable(with = CustomExtensionSerializer::class)
@SerialName("CustomExtension")
@SpecDoc(CUSTOM_EXTENSION_SPEC_DOC)
data class CustomExtension(
    @SpecDoc("The extension type, owner-namespaced (e.g. governance:pii).")
    var type: String,
    @SerialName("\$schema")
    @SpecFormat("uri")
    @SpecDoc("Optional link to the extension owner's schema for the definition payload.")
    var schema: String? = null,
    @SpecDoc("Optional human-readable extension name.")
    var name: String? = null,
    @SpecDoc("Free-form extension payload; never validated by this spec.")
    var definition: ExtensionValue? = null,
    val extra: MutableMap<String, ExtensionValue> = mutableMapOf()
)

/**
 * Typed access to the custom extensions of an [Extensions] carrier: decodes the `custom`
 * list into [CustomExtension] envelopes. Absent `custom` key = no custom extensions.
 */
fun Extensions.getCustom(json: Json = Json { ignoreUnknownKeys = true }): List<CustomExtension> {
    val value = extensions["custom"] as? ListValue ?: return emptyList()
    return value.value.map {
        json.decodeFromJsonElement(CustomExtensionSerializer, ExtensionValueSerializer.toJson(it))
    }
}
