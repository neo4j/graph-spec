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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import model.extension.importer.mapping.MappingExtensionModule
import model.extension.importer.table.TableExtensionModule
import model.extension.neo4j.index.IndexExtensionModule
import model.extension.ui.display.DisplayExtensionModule
import validate.Validation

/**
 * The per-named-extension contract (ADR-0005): each named extension (`neo4j:index`,
 * `ui:display`, `importer:table`, `importer:mapping`, ...) declares a
 * module object implementing this interface in its owner-prefixed folder under this
 * package. The contract is static: modules are aggregated by explicit lists, not
 * runtime discovery. The shape codec and validators are owned by the extension's team.
 */
interface NamedExtension<T : Any> {
    /** Wire key in the extensions map, e.g. "neo4j:index". */
    val key: String

    /** Shape codec for the extension payload. */
    val serializer: KSerializer<T>

    /** The extension's validators, aggregated into the Validations lists. */
    val validations: List<Validation>
}

/**
 * The registered named-extension modules (ADR-0005): the single source both
 * [validate.Validations.namedExtensions] and the cross-cutting
 * [MalformedNamedExtensionPayload] rule aggregate, so a future module is picked up
 * automatically by every consumer of the module list.
 */
val namedExtensionModules: List<NamedExtension<*>> =
    listOf(
        IndexExtensionModule,
        DisplayExtensionModule,
        TableExtensionModule,
        MappingExtensionModule
    )

/**
 * Strict single-payload access to a named extension (ADR-0005): decodes the payload
 * stored under [module]'s key through the module's serializer. Returns null when the
 * key is absent; THROWS [SerializationException] (or [IllegalArgumentException]) on a
 * list or malformed payload, so this accessor fits only single-object modules.
 *
 * For object-or-list modules [getNamedList] is the recommended path.
 * [MalformedNamedExtensionPayload] reports the malformed payload as an issue.
 */
fun <T : Any> Extensions.getNamed(module: NamedExtension<T>, json: Json = Json { ignoreUnknownKeys = true }): T? {
    val value = extensions[module.key] ?: return null
    return json.decodeFromJsonElement(module.serializer, ExtensionValueSerializer.toJson(value))
}

/**
 * List-normalising typed access (ADR-0005): a single-object payload decodes to a
 * one-element list, a list payload decodes element-wise. Validator-safe: a payload
 * decodable in neither form yields an empty list, and
 * [MalformedNamedExtensionPayload] is the rule that reports it.
 */
fun <T : Any> Extensions.getNamedList(
    module: NamedExtension<T>,
    json: Json = Json { ignoreUnknownKeys = true }
): List<T> {
    val value = extensions[module.key] ?: return emptyList()
    return decodeNamedListOrNull(module, value, json) ?: emptyList()
}

/**
 * The one object-or-list decode ladder behind [getNamedList] and
 * [MalformedNamedExtensionPayload] (ADR-0005): an object payload decodes to a
 * one-element list, a [ListValue] payload decodes element-wise, and a payload decodable
 * in neither form yields null. Never throws — the caller chooses between an empty list
 * (typed access) and an issue (the malformed-payload rule).
 */
internal fun <T : Any> decodeNamedListOrNull(module: NamedExtension<T>, value: ExtensionValue, json: Json): List<T>? {
    val element = ExtensionValueSerializer.toJson(value)
    return try {
        when (value) {
            is ListValue -> json.decodeFromJsonElement(ListSerializer(module.serializer), element)
            else -> listOf(json.decodeFromJsonElement(module.serializer, element))
        }
    } catch (e: SerializationException) {
        null
    } catch (e: IllegalArgumentException) {
        null
    }
}
