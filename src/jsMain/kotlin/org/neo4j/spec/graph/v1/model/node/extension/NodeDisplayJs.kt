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
package org.neo4j.spec.graph.v1.model.node.extension

import js.objects.Record
import js.objects.toRecord
import kotlinx.js.JsPlainObject
import org.neo4j.spec.graph.v1.model.associateBy
import org.neo4j.spec.graph.v1.model.emptyRecord
import org.neo4j.spec.graph.v1.model.jso
import org.neo4j.spec.graph.v1.model.node.toClass
import org.neo4j.spec.graph.v1.model.node.toJs
import org.neo4j.spec.graph.v1.model.value.ExtensionValueJs
import org.neo4j.spec.graph.v1.model.value.toClass
import org.neo4j.spec.graph.v1.model.value.toJs
import kotlin.collections.component1
import kotlin.collections.component2

@JsExport
@JsPlainObject
external interface NodeDisplayJs {
    var x: Double
    var y: Double
    var colour: String?
    val custom: Record<String, ExtensionValueJs>
}

fun nodeDisplayJs(
    x: Double,
    y: Double,
    colour: String? = null,
    custom: Record<String, ExtensionValueJs> = emptyRecord()
): NodeDisplayJs = jso {
    this.x = x
    this.y = y
    this.colour = colour
    this.custom = custom
}

fun NodeDisplay.toJs() = nodeDisplayJs(
    x = x,
    y = y,
    colour = colour,
    custom = custom.mapValues { (_, extension) -> extension.toJs() }.toRecord()
)

fun NodeDisplayJs.toClass(): NodeDisplay = NodeDisplay(
    x = x,
    y = y,
    colour = colour,
    custom = custom.associateBy { _, value -> value.toClass() }.toMutableMap()
)
