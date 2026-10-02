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
package model.property

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.extension.ExtensionValue
import model.extension.Extensions
import model.spec.SpecDef
import model.spec.SpecDoc
import model.spec.SpecFormat
import model.spec.SpecMinimum
import model.spec.SpecPattern
import kotlin.js.JsExport

/**
 * v1 property (ontology-graph-spec.schema.json `$defs/property`). [type] is a type token
 * (`STRING`, `LIST<STRING>`, `VECTOR<FLOAT>`, `ANY`, ...; `$defs/propertyType`);
 * [dimension] is the VECTOR companion. `mustExist`/`unique`/`key` are the shorthand
 * constraint flags; [oneOf] (`one_of`) and [pattern] constrain allowed values.
 */
@JsExport
@Serializable
@SerialName("Property")
@SpecDoc(
    "A property of a node or relationship type: a type token, optional value " +
        "constraints (one_of, pattern), the shorthand constraint flags, and metadata."
)
data class Property(
    @SpecDef("propertyType")
    @SpecPattern(
        "^(ANY|LIST<ANY>|(STRING|INTEGER|FLOAT|BOOLEAN|DATE|TIME|LOCALTIME|DATETIME|" +
            "LOCALDATETIME|DURATION|POINT|BYTES)|(LIST|VECTOR)<(STRING|INTEGER|FLOAT|BOOLEAN|" +
            "DATE|TIME|LOCALTIME|DATETIME|LOCALDATETIME|DURATION|POINT|BYTES)>)$"
    )
    @SpecDoc(
        "Type token: a Neo4j scalar, ANY, LIST<...> or VECTOR<...> (a VECTOR may carry a " +
            "companion dimension field on the property). Element types are always scalars: " +
            "no nested lists, no lists of vectors. No union types in v1: a property is a " +
            "single type or ANY."
    )
    var type: String? = null,
    @SpecMinimum(1)
    @SpecDoc("VECTOR companion: element count. Absent = unconstrained.")
    var dimension: Int? = null,
    @SpecDoc("Shorthand for a single-property mustExist constraint.")
    var mustExist: Boolean? = null,
    @SpecDoc("Shorthand for a single-property unique constraint.")
    var unique: Boolean? = null,
    @SpecDoc("Shorthand for a single-property key constraint (unique + mustExist).")
    var key: Boolean? = null,
    @SerialName("one_of")
    @SpecDoc("Allowed values. JSON Schema's word. Default value support.")
    val oneOf: MutableList<ExtensionValue> = mutableListOf(),
    @SpecFormat("regex")
    @SpecDoc("Regular expression the property value must match.")
    var pattern: String? = null,
    @SpecDoc("Alternative names for this property.")
    val aliases: MutableList<String> = mutableListOf(),
    @SpecDef("reference")
    @SpecFormat("uri")
    @SpecDoc(
        "A single informational URI pointing at an external definition of this element " +
            "(e.g. the original RDF resource)."
    )
    var reference: String? = null,
    @SpecDoc("Human-readable description of this property.")
    var description: String? = null,
    @SpecDoc("Named extensions map: first-party neo4j:* keys; custom envelopes under the reserved custom key.")
    override val extensions: MutableMap<String, ExtensionValue> = mutableMapOf()
) : Extensions
