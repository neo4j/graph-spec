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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import validate.Validation

/**
 * The per-named-extension contract (ADR-0005): each named extension (`neo4j:index`,
 * `neo4j:display`, `neo4j-importer:table`, `neo4j-importer:mapping`, ...) declares a
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
 * Typed access to a named extension (ADR-0005): decodes the payload stored under
 * [module]'s key through the module's serializer. Returns null when the key is absent.
 * The raw map remains available to consumers; this is the recommended path.
 */
fun <T : Any> Extensions.getNamed(module: NamedExtension<T>, json: Json = Json { ignoreUnknownKeys = true }): T? {
    val value = extensions[module.key] ?: return null
    return json.decodeFromJsonElement(module.serializer, ExtensionValueSerializer.toJson(value))
}
