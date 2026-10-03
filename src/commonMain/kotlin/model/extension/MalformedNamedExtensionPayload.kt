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

import kotlinx.serialization.json.Json
import model.GraphModel
import validate.Issue
import validate.Validation
import validate.extensionPath
import validate.forEachExtensionLevel

/**
 * Cross-cutting ADR-0005 rule ("a payload under a declared key that does not match the
 * module's shape is reported by the new validators"): for every registered module in
 * [namedExtensionModules], a payload under the module's key — on the root, on any node,
 * relationship, or property — that the module cannot decode, neither as its object
 * shape nor as a list of it, is reported here, once per (key, location). This is what
 * keeps `Validations.all` total on malformed documents: the module validators read
 * through [getNamedList] and skip payloads they cannot decode; this
 * rule is the one that speaks up about them. The traversal and the path grammar are
 * owned once by validate/ModelWalk.kt ([forEachExtensionLevel]); the decode ladder is
 * the shared [decodeNamedListOrNull].
 */
object MalformedNamedExtensionPayload : Validation {
    override fun validate(model: GraphModel, issues: MutableList<Issue>) {
        val json = Json { ignoreUnknownKeys = true }
        model.forEachExtensionLevel { path, owner, _ ->
            for (module in namedExtensionModules) {
                val value = owner.extensions[module.key] ?: continue
                if (decodeNamedListOrNull(module, value, json) != null) continue
                issues.add(
                    Issue(
                        code = "malformed_named_extension_payload",
                        message = "Payload under declared key '${module.key}' at " +
                            "'${path.ifEmpty {
                                "root"
                            }}' does not match the module's shape (object or list of objects)",
                        path = extensionPath(path, module.key)
                    )
                )
            }
        }
    }
}
