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
external interface GraphModelExtensionsJs {
    val custom: Record<String, ExtensionValueJs>
}

fun graphModelExtensionsJs(custom: Record<String, ExtensionValueJs> = emptyRecord()): GraphModelExtensionsJs = jso {
    this.custom = custom
}

fun GraphModelExtensions.toJs() = graphModelExtensionsJs(
    custom = custom.mapValues { (_, extension) -> extension.toJs() }.toRecord()
)

fun GraphModelExtensionsJs.toClass(): GraphModelExtensions = GraphModelExtensions(
    custom = custom.associateBy { _, value -> value.toClass() }.toMutableMap()
)
