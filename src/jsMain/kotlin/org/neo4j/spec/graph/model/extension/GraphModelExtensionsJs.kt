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
package org.neo4j.spec.graph.model.extension

import js.objects.Record
import js.objects.toRecord
import kotlinx.js.JsPlainObject
import org.neo4j.spec.graph.model.associateBy
import org.neo4j.spec.graph.model.emptyRecord
import org.neo4j.spec.graph.model.extension.custom.toCustomClass
import org.neo4j.spec.graph.model.extension.custom.toCustomJs
import org.neo4j.spec.graph.model.extension.mapping.MappingJs
import org.neo4j.spec.graph.model.extension.mapping.toClass
import org.neo4j.spec.graph.model.extension.mapping.toJs
import org.neo4j.spec.graph.model.extension.source.TableJs
import org.neo4j.spec.graph.model.extension.source.toClass
import org.neo4j.spec.graph.model.extension.source.toJs
import org.neo4j.spec.graph.model.jso

@JsExport
@JsPlainObject
external interface GraphModelExtensionsJs {
    val custom: Record<String, Any>
    val tables: Record<String, TableJs>
    var mappings: Array<MappingJs>
}

fun graphModelExtensionsJs(
    custom: Record<String, Any> = emptyRecord(),
    tables: Record<String, TableJs> = emptyRecord(),
    mappings: Array<MappingJs> = emptyArray()
): GraphModelExtensionsJs = jso {
    this.custom = custom
    this.tables = tables
    this.mappings = mappings
}

fun GraphModelExtensions.toJs() = graphModelExtensionsJs(
    custom = custom.toCustomJs(),
    tables = tables.mapValues { (_, table) -> table.toJs() }.toRecord(),
    mappings = mappings.map { mapping -> mapping.toJs() }.toTypedArray()
)

fun GraphModelExtensionsJs.toClass(): GraphModelExtensions = GraphModelExtensions(
    custom = custom.toCustomClass(),
    tables = tables.associateBy { _, js -> js.toClass() },
    mappings = mappings.map { it.toClass() }.toMutableList()
)
