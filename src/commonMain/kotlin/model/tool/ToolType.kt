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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.put
import model.extension.ExtensionValueSerializer
import model.extension.putExtras
import validate.Validation

/**
 * The per-tool-type contract (ADR-0011): each predefined tool type (`canonicalQuery`, ...)
 * declares a module object implementing this interface in this package, mirroring
 * [model.extension.NamedExtension] on the tool surface. The contract is static: modules are
 * aggregated by explicit lists, not runtime discovery. The shape codec and validators are
 * owned by the tool type's team (proposal open item 4: per-type shapes stay owner-defined;
 * the module is the owner's contract in the SDK).
 */
interface ToolType<T : Any> {
    /** Wire token in [Tool.type], e.g. "canonicalQuery". */
    val type: String

    /** Shape codec for the tool payload (the core fields plus the type's own fields). */
    val serializer: KSerializer<T>

    /** The tool type's validators, aggregated into the Validations lists. */
    val validations: List<Validation>
}

/**
 * The registered tool-type modules (ADR-0011): the single source
 * [validate.Validations.core] aggregates, so a future module is picked up
 * automatically by every consumer of the module list. Mirrors
 * [model.extension.namedExtensionModules].
 */
val toolTypeModules: List<ToolType<*>> =
    listOf(
        CanonicalQueryToolModule
    )

/**
 * Typed access to a tool (ADR-0011): decodes the tool's full payload — `type`/`name`/
 * `description` plus the inlined [Tool.extra] fields — through [module]'s serializer.
 * Returns null when the tool's type token is not the module's. The raw [Tool] remains
 * available to consumers; this is the recommended path. Mirroring
 * [model.extension.getNamed], a payload that does not match the module's shape fails the
 * decode — validators inspect the raw fields instead of decoding.
 */
fun <T : Any> Tool.asTyped(module: ToolType<T>, json: Json = Json { ignoreUnknownKeys = true }): T? {
    if (type != module.type) return null
    return json.decodeFromJsonElement(module.serializer, toJsonObject())
}

/** The tool's full wire payload as a [JsonObject]: core fields plus the inlined extras. */
private fun Tool.toJsonObject(): JsonObject = buildJsonObject {
    put("type", type)
    name?.let { put("name", it) }
    description?.let { put("description", it) }
    putExtras(extra, ToolSerializer.knownKeys)
}
