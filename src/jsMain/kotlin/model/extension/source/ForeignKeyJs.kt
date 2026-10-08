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
package model.extension.source

import js.objects.Record
import kotlinx.js.JsPlainObject
import model.associateBy
import model.emptyRecord
import model.jso
import model.value.ExtensionValueJs
import model.value.toClass
import model.value.toJs
import kotlin.String

@JsExport
@JsPlainObject
external interface ForeignKeyJs {
    var columns: Array<String>
    val references: ForeignKeyReferenceJs
    val custom: Record<String, ExtensionValueJs>
}

fun foreignKeyJs(
    columns: Array<String>,
    references: ForeignKeyReferenceJs,
    custom: Record<String, ExtensionValueJs> = emptyRecord()
): ForeignKeyJs = jso {
    this.columns = columns
    this.references = references
    this.custom = custom
}

fun ForeignKey.toJs() = foreignKeyJs(
    columns = columns.toTypedArray(),
    references = references.toJs(),
    custom = custom.associateBy { _, value -> value.toJs() }
)

fun ForeignKeyJs.toClass() = ForeignKey(
    columns = columns.toMutableSet(),
    references = references.toClass(),
    custom = custom.associateBy { _, value -> value.toClass() }.toMutableMap()
)
