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
package org.neo4j.spec.graph.v1.model.extension

import js.objects.Record
import js.objects.toRecord
import kotlinx.js.JsPlainObject
import org.neo4j.spec.graph.v1.model.associateBy
import org.neo4j.spec.graph.v1.model.emptyRecord
import org.neo4j.spec.graph.v1.model.extension.mapping.MappingJs
import org.neo4j.spec.graph.v1.model.extension.mapping.toClass
import org.neo4j.spec.graph.v1.model.extension.mapping.toJs
import org.neo4j.spec.graph.v1.model.extension.source.TableJs
import org.neo4j.spec.graph.v1.model.extension.source.toClass
import org.neo4j.spec.graph.v1.model.extension.source.toJs
import org.neo4j.spec.graph.v1.model.jso
import org.neo4j.spec.graph.v1.model.value.ExtensionValueJs
import org.neo4j.spec.graph.v1.model.value.toClass
import org.neo4j.spec.graph.v1.model.value.toJs
@JsExport
@JsPlainObject
external interface GraphModelExtensionsJs {
    val custom: Record<String, ExtensionValueJs>
    val tables: Record<String, TableJs>
    var mappings: Array<MappingJs>
}

fun graphModelExtensionsJs(
    custom: Record<String, ExtensionValueJs> = emptyRecord(),
    tables: Record<String, TableJs> = emptyRecord(),
    mappings: Array<MappingJs> = emptyArray()
): GraphModelExtensionsJs = jso {
    this.custom = custom
    this.tables = tables
    this.mappings = mappings
}

fun GraphModelExtensions.toJs() = graphModelExtensionsJs(
    custom = custom.mapValues { (_, extension) -> extension.toJs() }.toRecord(),
    tables = tables.mapValues { (_, table) -> table.toJs() }.toRecord(),
    mappings = mappings.map { mapping -> mapping.toJs() }.toTypedArray()
)

fun GraphModelExtensionsJs.toClass(): GraphModelExtensions = GraphModelExtensions(
    custom = custom.associateBy { _, value -> value.toClass() }.toMutableMap(),
    tables = tables.associateBy { _, js -> js.toClass() },
    mappings = mappings.map { it.toClass() }.toMutableList()
)
