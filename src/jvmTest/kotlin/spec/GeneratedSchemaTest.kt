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
package spec

import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import schema.OntologyGraphSpecJsonSchemaGenerator
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Unit coverage of the schema generator (ADR-0003): the CI drift check only catches
 * an uncommitted regenerate, so these tests pin the generator's mechanics — required
 * marking, @SpecDef hoisting, the enumerated overrides, and the @SpecDoc enforcement
 * boundary (every descriptor-visible field documented; the custom-serialized defs are
 * the enumerated exception).
 */
class GeneratedSchemaTest {
    private val schema = OntologyGraphSpecJsonSchemaGenerator.generate()

    private fun def(name: String): JsonObject = (schema["\$defs"] as JsonObject).jsonObject(name)
    private fun props(name: String): JsonObject = def(name)["properties"] as JsonObject
    private fun required(name: String): List<String> =
        (def(name)["required"] as JsonArray).map { (it as JsonPrimitive).content }

    private fun JsonObject.jsonObject(key: String) = this[key] as JsonObject

    @Test
    fun `the defs set is exactly the modelled surface`() {
        assertEquals(
            setOf(
                "propertyType", "labels", "constraint", "property", "endpoint",
                "reference", "tool", "extension", "extensionsMap", "node", "relationshipEntry",
            ),
            (schema["\$defs"] as JsonObject).keys,
        )
    }

    @Test
    fun `root shape`() {
        assertEquals(
            listOf("\$schema", "id", "version"),
            (schema["required"] as JsonArray).map { (it as JsonPrimitive).content },
        )
        assertEquals(JsonPrimitive(false), schema["additionalProperties"])
        assertEquals(
            "uri",
            ((schema["properties"] as JsonObject).jsonObject("\$schema")["format"] as JsonPrimitive).content,
        )
    }

    @Test
    fun `required marking - no-default fields plus SpecRequired`() {
        assertEquals(listOf("identifier"), required("labels")) // nullable in Kotlin, @SpecRequired
        assertEquals(listOf("constraint_type", "properties"), required("constraint"))
        assertEquals(listOf("node"), required("endpoint"))
        assertEquals(listOf("type", "from", "to"), required("relationshipEntry"))
        assertEquals(listOf("type"), required("tool"))
        assertEquals(listOf("type"), required("extension"))
        assertFalse(def("node").containsKey("required"))
        assertFalse(def("property").containsKey("required"))
    }

    @Test
    fun `closed and open surfaces`() {
        for (closed in listOf("labels", "constraint", "property", "endpoint", "node", "relationshipEntry")) {
            assertEquals(JsonPrimitive(false), def(closed)["additionalProperties"], closed)
        }
        assertEquals(JsonPrimitive(true), def("tool")["additionalProperties"])
        assertEquals(JsonPrimitive(true), def("extensionsMap")["additionalProperties"])
        assertFalse(def("extension").containsKey("additionalProperties")) // JSON Schema default
    }

    @Test
    fun `enumerated overrides`() {
        // Free-form custom-extension payload: the boolean schema `true`
        assertEquals(JsonPrimitive(true), props("extension")["definition"])
        // one_of: array of any JSON value (no items constraint)
        val oneOf = props("property").jsonObject("one_of")
        assertEquals(JsonPrimitive("array"), oneOf["type"])
        assertFalse(oneOf.containsKey("items"))
        // extensionsMap's pinned custom envelope
        assertEquals(
            "#/\$defs/extension",
            (def("extensionsMap").jsonObject("properties").jsonObject("custom").jsonObject("items")["\$ref"] as JsonPrimitive).content,
        )
    }

    @Test
    fun `SpecDef hoisting and shared refs`() {
        assertEquals("#/\$defs/propertyType", (props("property")["type"] as JsonObject)["\$ref"]?.let { (it as JsonPrimitive).content })
        assertNotNull(def("propertyType")["pattern"])
        assertEquals("uri", (def("reference")["format"] as JsonPrimitive).content)
        for (defName in listOf("node", "relationshipEntry", "property")) {
            assertEquals(
                "#/\$defs/reference",
                (props(defName)["reference"] as JsonObject)["\$ref"]?.let { (it as JsonPrimitive).content },
                defName,
            )
        }
    }

    @Test
    fun `annotation-carried constraints`() {
        assertEquals(
            listOf("key", "unique", "mustExist"),
            (props("constraint").jsonObject("constraint_type")["enum"] as JsonArray).map { (it as JsonPrimitive).content },
        )
        assertEquals(JsonPrimitive(1), props("constraint").jsonObject("properties")["minItems"])
        assertEquals(JsonPrimitive(1), props("property").jsonObject("dimension")["minimum"])
        assertEquals(JsonPrimitive(0), props("endpoint").jsonObject("count")["minimum"])
        assertEquals(JsonPrimitive(1), props("endpoint").jsonObject("max_count")["minimum"])
        assertEquals("regex", (props("property").jsonObject("pattern")["format"] as JsonPrimitive).content)
        for (defName in listOf("node", "relationshipEntry")) {
            assertEquals(
                JsonPrimitive(1),
                props(defName).jsonObject("properties").jsonObject("propertyNames")["minLength"],
                defName,
            )
        }
    }

    @Test
    fun `every descriptor-visible field is documented - the custom-serialized defs are the only exception`() {
        val undocumented = mutableListOf<String>()
        fun check(container: JsonObject, path: String) {
            container.forEach { (name, field) ->
                // A bare $ref is documented by its target def; anything else needs its own description.
                if (field is JsonObject && !field.containsKey("description") && !field.containsKey("\$ref")) {
                    undocumented.add("$path/$name")
                }
            }
        }
        (schema["\$defs"] as JsonObject).forEach { (defName, defSchema) ->
            val defObject = defSchema as JsonObject
            if (!defObject.containsKey("description")) undocumented.add("\$defs/$defName")
            (defObject["properties"] as? JsonObject)?.let { check(it, "\$defs/$defName/properties") }
        }
        check(schema["properties"] as JsonObject, "properties")

        // tool/extension fields are documented on the model but their hand-built
        // serializer descriptors don't carry annotations (enumerated override, ADR-0003)
        assertEquals(
            listOf(
                "\$defs/extension/properties/\$schema",
                "\$defs/extension/properties/name",
                "\$defs/extension/properties/type",
                "\$defs/tool/properties/description",
                "\$defs/tool/properties/name",
                "\$defs/tool/properties/type",
            ),
            undocumented.sorted(),
        )
    }
}
