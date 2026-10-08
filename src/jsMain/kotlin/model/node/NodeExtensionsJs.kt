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
package model.node

import js.objects.Record
import js.objects.toRecord
import kotlinx.js.JsPlainObject
import model.associateBy
import model.emptyRecord
import model.extension.ExtensionValueJs
import model.extension.toClass
import model.extension.toJs
import model.jso

@JsExport
@JsPlainObject
external interface NodeExtensionsJs {
    val custom: Record<String, ExtensionValueJs>
    val indexes: Record<String, NodeIndexJs>
    var display: NodeDisplayJs?
}

fun nodeExtensionsJs(
    custom: Record<String, ExtensionValueJs> = emptyRecord(),
    indexes: Record<String, NodeIndexJs> = emptyRecord(),
    display: NodeDisplayJs? = null
): NodeExtensionsJs = jso {
    this.custom = custom
    this.indexes = indexes
    this.display = display
}

fun NodeExtensions.toJs() = nodeExtensionsJs(
    custom = custom.mapValues { (_, extension) -> extension.toJs() }.toRecord(),
    indexes = indexes.mapValues { (_, index) -> index.toJs() }.toRecord(),
    display = display?.toJs()
)

fun NodeExtensionsJs.toClass(): NodeExtensions = NodeExtensions(
    custom = custom.associateBy { _, value -> value.toClass() }.toMutableMap(),
    indexes = indexes.associateBy { _, value -> value.toClass() },
    display = display?.toClass()
)
