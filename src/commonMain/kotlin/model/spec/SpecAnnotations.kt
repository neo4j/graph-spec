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
package model.spec

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialInfo

/*
    Spec annotations: the channel through which the Kotlin model carries the JSON Schema
    surface (ADR-0003). The generator (jvmMain schema/GenerateOntologyGraphSpecJsonSchema.kt)
    reads them off the kotlinx-serialization descriptors; @SpecDoc is mandatory on every
    structural field, so the emitted schema always documents itself.
 */

/** JSON Schema `description`. Mandatory on every structural field and on every class that becomes a `$defs` entry. */
@SerialInfo
@Target(AnnotationTarget.PROPERTY, AnnotationTarget.CLASS)
annotation class SpecDoc(val value: String)

/** JSON Schema `pattern` on a string field. */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class SpecPattern(val value: String)

/** JSON Schema `format` on a string field (`uri`, `regex`). */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class SpecFormat(val value: String)

/** JSON Schema `minimum` on an integer field. */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class SpecMinimum(val value: Long)

/** JSON Schema `minItems` on an array field. */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class SpecMinItems(val value: Int)

/** JSON Schema `enum` on a string token field. */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class SpecEnum(val values: Array<String>)

/** Marks a field required in the schema even though the Kotlin model keeps it nullable/defaulted for leniency. */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class SpecRequired

/** Emits the field as `$ref: #/$defs/<name>` and hoists its annotations into a named, shared `$defs` entry. */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class SpecDef(val name: String)

/** JSON Schema `propertyNames` on a map field (ADR-0010). */
@SerialInfo
@Target(AnnotationTarget.PROPERTY)
annotation class SpecPropertyNames(val minLength: Int, val description: String)
