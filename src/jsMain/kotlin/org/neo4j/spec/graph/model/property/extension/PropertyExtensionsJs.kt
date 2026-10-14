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
package org.neo4j.spec.graph.model.property.extension

import js.objects.Record
import kotlinx.js.JsPlainObject
import org.neo4j.spec.graph.model.emptyRecord
import org.neo4j.spec.graph.model.extension.custom.toCustomClass
import org.neo4j.spec.graph.model.extension.custom.toCustomJs
import org.neo4j.spec.graph.model.jso
@JsExport
@JsPlainObject
external interface PropertyExtensionsJs {
    val custom: Record<String, Any>
}

fun propertyExtensionsJs(custom: Record<String, Any> = emptyRecord()): PropertyExtensionsJs = jso {
    this.custom = custom
}

fun PropertyExtensions.toJs() = propertyExtensionsJs(
    custom = custom.toCustomJs()
)

fun PropertyExtensionsJs.toClass(): PropertyExtensions = PropertyExtensions(
    custom = custom.toCustomClass()
)
