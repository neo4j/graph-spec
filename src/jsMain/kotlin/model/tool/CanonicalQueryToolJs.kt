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

import kotlinx.js.JsPlainObject
import model.jso
import kotlin.js.JsExport

/**
 * JS twin of [CanonicalQueryTool] (ADR-0011). [type] is always the literal
 * `"canonicalQuery"`; Kotlin/JS emits `String` as `string` in the .d.mts and cannot pin a
 * literal type from Kotlin, so the [canonicalQueryToolJs] factory is the autocomplete entry
 * point — it sets the literal at construction.
 */
@JsExport
@JsPlainObject
external interface CanonicalQueryToolJs {
    /** The tool type discriminator; always "canonicalQuery" for this shape. */
    var type: String
    var name: String?
    var description: String?
    var cypher: String
}

@JsExport
fun canonicalQueryToolJs(cypher: String, name: String? = null, description: String? = null): CanonicalQueryToolJs =
    jso {
        this.type = "canonicalQuery"
        this.name = name
        this.description = description
        this.cypher = cypher
    }

fun CanonicalQueryTool.toJs() = canonicalQueryToolJs(
    cypher = cypher,
    name = name,
    description = description
)

fun CanonicalQueryToolJs.toClass() = CanonicalQueryTool(
    type = type,
    name = name,
    description = description,
    cypher = cypher
)

/** The open [ToolJs] form (cypher inlined into extra), for attaching to a node's tools array. */
fun CanonicalQueryToolJs.toToolJs(): ToolJs = toClass().toTool().toJs()
