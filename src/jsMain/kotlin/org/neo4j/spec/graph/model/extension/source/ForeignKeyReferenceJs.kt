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
package org.neo4j.spec.graph.model.extension.source

import js.objects.Record
import kotlinx.js.JsPlainObject
import org.neo4j.spec.graph.model.associateBy
import org.neo4j.spec.graph.model.emptyRecord
import org.neo4j.spec.graph.model.jso
import org.neo4j.spec.graph.model.value.ExtensionValueJs
import org.neo4j.spec.graph.model.value.toClass
import org.neo4j.spec.graph.model.value.toJs
import kotlin.String

@JsExport
@JsPlainObject
external interface ForeignKeyReferenceJs {
    var table: String
    var columns: Array<String>
    val custom: Record<String, ExtensionValueJs>
}

fun foreignKeyReferenceJs(
    table: String,
    columns: Array<String> = emptyArray(),
    custom: Record<String, ExtensionValueJs> = emptyRecord()
): ForeignKeyReferenceJs = jso {
    this.table = table
    this.columns = columns
    this.custom = custom
}

fun ForeignKeyReference.toJs() = foreignKeyReferenceJs(
    table = table,
    columns = columns.toTypedArray(),
    custom = custom.associateBy { _, value -> value.toJs() }
)

fun ForeignKeyReferenceJs.toClass() = ForeignKeyReference(
    table = table,
    columns = columns.toMutableSet(),
    custom = custom.associateBy { _, value -> value.toClass() }.toMutableMap()
)
