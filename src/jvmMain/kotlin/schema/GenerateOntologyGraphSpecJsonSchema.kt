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
package schema

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.StructureKind
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import model.GraphModel
import model.spec.SpecDef
import model.spec.SpecDoc
import model.spec.SpecEnum
import model.spec.SpecFormat
import model.spec.SpecMinItems
import model.spec.SpecMinimum
import model.spec.SpecPattern
import model.spec.SpecPropertyNames
import model.spec.SpecRequired
import java.io.File

/*
    The v1 schema generator (ADR-0003): walks GraphModel.serializer().descriptor and emits
    ontology-graph-spec.schema.json. The annotated Kotlin model is the source of truth; every
    structural property must carry @SpecDoc (enforced here — an undocumented field fails the
    build, which is AGENTS.md non-negotiable 2 made mechanical).

    Deliberate, enumerated overrides (schema shape a serializer descriptor cannot express):
    - the `extensionsMap` $def is a constant: the extensions map is Map<String, ExtensionValue>
      in the model, but the spec pins its `custom` envelope and additionalProperties:true
    - `Tool` and the extensions map are open surfaces: additionalProperties:true (InlineExtras)
    - `CustomExtension.definition` is free-form: the boolean schema `true`
    - `one_of` lists ExtensionValue payloads: an array with no items constraint
    - `ExtensionValue` itself is untagged raw JSON: emitted as `true` where referenced directly
 */
@OptIn(ExperimentalSerializationApi::class)
object OntologyGraphSpecJsonSchemaGenerator {
    private val DEFS_BY_SERIAL_NAME =
        mapOf(
            "Node" to "node",
            "Labels" to "labels",
            "Constraint" to "constraint",
            "Property" to "property",
            "RelationshipTarget" to "endpoint",
            "Relationship" to "relationshipEntry",
            "Tool" to "tool",
            "CustomExtension" to "extension",
        )

    /** $defs entries that stay open (InlineExtras catch-alls); everything else is closed. */
    private val OPEN_DEFS = setOf("tool")

    /** $defs entries with no additionalProperties key at all (JSON Schema default: open). */
    private val UNGUARDED_DEFS = setOf("extension")

    /**
     * Custom-serialized classes (ToolSerializer, CustomExtensionSerializer) hand-build
     * their descriptors, so property-level annotations are invisible to the generator;
     * their fields are documented on the model but not required in the schema output.
     */
    private val CUSTOM_SERIALIZED_DEFS = setOf("tool", "extension")

    /** Field-level schema facts the hand-built serializer descriptors can't carry. */
    private val FIELD_OVERRIDES =
        mapOf(
            // CustomExtension.`$schema`: format: uri (the @SpecFormat sits on the class
            // field, invisible through CustomExtensionSerializer's descriptor)
            "extension" to mapOf("\$schema" to mapOf("format" to JsonPrimitive("uri"))),
        )

    /**
     * Class-level docs for the custom-serialized classes: their hand-built serializer
     * descriptors don't carry the class annotations, so the generator reads the consts
     * declared next to the classes (single source, see Tool.kt / CustomExtension.kt).
     */
    private val CLASS_DOC_OVERRIDES =
        mapOf(
            "tool" to model.tool.TOOL_SPEC_DOC,
            "extension" to model.extension.CUSTOM_EXTENSION_SPEC_DOC,
        )

    private const val EXTENSIONS_MAP_DESCRIPTION =
        "Named extensions: first-party extensions as keys (neo4j:*), each shape defined by its " +
            "owner, available in the ontology graph spec SDK, not validated by this spec. Custom " +
            "extensions ride the fixed envelope under the reserved custom key."

    fun generate(): JsonObject {
        val defs = linkedMapOf<String, JsonElement>()
        defs["extensionsMap"] = extensionsMapDef()
        for ((serialName, defName) in DEFS_BY_SERIAL_NAME) {
            val descriptor = descriptorBySerialName(serialName)
            defs[defName] = objectSchema(descriptor, defName, defs)
        }
        val root = GraphModel.serializer().descriptor
        return orderedObject(
            "\$schema" to JsonPrimitive("https://json-schema.org/draft/2020-12/schema"),
            "\$id" to JsonPrimitive("https://neo4j.com/ontology-graph-spec/1.0.0/schema.json"),
            "title" to JsonPrimitive("Neo4j Ontology Graph Specification v1"),
            "description" to JsonPrimitive(docOf(root.annotations, "GraphModel")),
            "type" to JsonPrimitive("object"),
            "required" to requiredArray(root, null),
            "additionalProperties" to JsonPrimitive(false),
            "properties" to propertiesObject(root, null, defs),
            "\$defs" to JsonObject(defs),
        )
    }

    private fun descriptorBySerialName(serialName: String): SerialDescriptor {
        // CustomExtension is not reachable from GraphModel's descriptor graph (it is
        // decoded dynamically from the extensions map's `custom` key) — seed it.
        val roots =
            listOf(
                GraphModel.serializer().descriptor,
                model.extension.CustomExtension.serializer().descriptor,
            )
        val seen = mutableListOf<SerialDescriptor>()
        fun visit(d: SerialDescriptor) {
            if (d in seen) return
            seen.add(d)
            for (i in 0 until d.elementsCount) {
                visit(d.getElementDescriptor(i))
                if (d.getElementDescriptor(i).kind == StructureKind.MAP) {
                    visit(d.getElementDescriptor(i).getElementDescriptor(1))
                }
                if (d.getElementDescriptor(i).kind == StructureKind.LIST) {
                    visit(d.getElementDescriptor(i).getElementDescriptor(0))
                }
            }
        }
        roots.forEach(::visit)
        return seen.firstOrNull { it.serialName.removeSuffix("?") == serialName }
            ?: error("Generator: no descriptor reachable from GraphModel for $serialName")
    }

    private fun objectSchema(
        descriptor: SerialDescriptor,
        defName: String,
        defs: MutableMap<String, JsonElement>,
    ): JsonObject {
        val entries = linkedMapOf<String, JsonElement>()
        val classDoc = docOrNull(descriptor.annotations) ?: CLASS_DOC_OVERRIDES[defName]
        classDoc?.let { entries["description"] = JsonPrimitive(it) }
            ?: error("Generator: ${descriptor.serialName} is missing a class-level @SpecDoc")
        entries["type"] = JsonPrimitive("object")
        val required = requiredArray(descriptor, defName)
        if (required.size > 0) entries["required"] = required
        when (defName) {
            in OPEN_DEFS -> entries["additionalProperties"] = JsonPrimitive(true)
            in UNGUARDED_DEFS -> Unit
            else -> entries["additionalProperties"] = JsonPrimitive(false)
        }
        entries["properties"] = propertiesObject(descriptor, defName, defs)
        return orderedObject(entries)
    }

    private fun requiredArray(descriptor: SerialDescriptor, defName: String?): JsonArray {
        val required = mutableListOf<JsonPrimitive>()
        for (i in 0 until descriptor.elementsCount) {
            if (skip(descriptor, i)) continue
            val annotations = descriptor.getElementAnnotations(i)
            if (descriptor.isElementOptional(i) && annotations.none { it is SpecRequired }) continue
            required.add(JsonPrimitive(descriptor.getElementName(i)))
        }
        return JsonArray(required)
    }

    private fun propertiesObject(
        descriptor: SerialDescriptor,
        defName: String?,
        defs: MutableMap<String, JsonElement>,
    ): JsonObject {
        val properties = linkedMapOf<String, JsonElement>()
        for (i in 0 until descriptor.elementsCount) {
            if (skip(descriptor, i)) continue
            val name = descriptor.getElementName(i)
            properties[name] = propertySchema(descriptor, i, defName, defs)
        }
        return JsonObject(properties)
    }

    /** Inline extras catch-alls are inlined on the wire by the custom serializers, not a wire field. */
    private fun skip(descriptor: SerialDescriptor, index: Int): Boolean =
        descriptor.getElementName(index) == "extra"

    private fun propertySchema(
        parent: SerialDescriptor,
        index: Int,
        defName: String?,
        defs: MutableMap<String, JsonElement>,
    ): JsonElement {
        val name = parent.getElementName(index)
        val annotations = parent.getElementAnnotations(index)
        val doc = docOrNull(annotations)
        if (doc == null && defName !in CUSTOM_SERIALIZED_DEFS) {
            error("Generator: ${parent.serialName}.$name is missing @SpecDoc")
        }
        val element = parent.getElementDescriptor(index)

        // Shared, hoisted $defs ($ref: propertyType, reference).
        annotations.filterIsInstance<SpecDef>().firstOrNull()?.let { specDef ->
            val defEntries = linkedMapOf<String, JsonElement>(
                "description" to JsonPrimitive(doc!!),
                "type" to JsonPrimitive("string"),
            )
            annotations.filterIsInstance<SpecPattern>().firstOrNull()?.let {
                defEntries["pattern"] = JsonPrimitive(it.value)
            }
            annotations.filterIsInstance<SpecFormat>().firstOrNull()?.let {
                defEntries["format"] = JsonPrimitive(it.value)
            }
            val existing = defs[specDef.name]
            val hoisted = orderedObject(defEntries)
            if (existing != null && existing != hoisted) {
                error("Generator: conflicting @SpecDef('${specDef.name}') annotations")
            }
            defs[specDef.name] = hoisted
            return ref(specDef.name)
        }

        // The named extensions map.
        if (name == "extensions") return ref("extensionsMap")

        // Free-form ExtensionValue payload (CustomExtension.definition).
        if (element.serialName == "ExtensionValue") return JsonPrimitive(true)

        val entries = linkedMapOf<String, JsonElement>()
        doc?.let { entries["description"] = JsonPrimitive(it) }
        when (element.kind) {
            StructureKind.MAP -> {
                entries["type"] = JsonPrimitive("object")
                annotations.filterIsInstance<SpecPropertyNames>().firstOrNull()?.let {
                    entries["propertyNames"] =
                        orderedObject(
                            "minLength" to JsonPrimitive(it.minLength),
                            "description" to JsonPrimitive(it.description),
                        )
                }
                val valueDef =
                    DEFS_BY_SERIAL_NAME[element.getElementDescriptor(1).serialName.removeSuffix("?")]
                        ?: error("Generator: map value type of ${parent.serialName}.$name has no \$def")
                entries["additionalProperties"] = ref(valueDef)
            }
            StructureKind.LIST -> {
                entries["type"] = JsonPrimitive("array")
                val itemDescriptor = element.getElementDescriptor(0)
                val itemDef = DEFS_BY_SERIAL_NAME[itemDescriptor.serialName]
                when {
                    itemDef != null -> entries["items"] = ref(itemDef)
                    itemDescriptor.serialName == "ExtensionValue" -> Unit // one_of: any JSON value
                    else -> entries["items"] = orderedObject("type" to JsonPrimitive(scalarType(itemDescriptor)))
                }
                annotations.filterIsInstance<SpecMinItems>().firstOrNull()?.let {
                    entries["minItems"] = JsonPrimitive(it.value)
                }
            }
            StructureKind.CLASS -> {
                val targetDef =
                    DEFS_BY_SERIAL_NAME[element.serialName.removeSuffix("?")]
                        ?: error("Generator: ${parent.serialName}.$name references ${element.serialName}, which has no \$def")
                return withDescription(ref(targetDef), doc, entries)
            }
            else -> {
                entries["type"] = JsonPrimitive(scalarType(element))
                annotations.filterIsInstance<SpecEnum>().firstOrNull()?.let {
                    entries["enum"] = JsonArray(it.values.map(::JsonPrimitive))
                }
                annotations.filterIsInstance<SpecPattern>().firstOrNull()?.let {
                    entries["pattern"] = JsonPrimitive(it.value)
                }
                annotations.filterIsInstance<SpecFormat>().firstOrNull()?.let {
                    entries["format"] = JsonPrimitive(it.value)
                }
                annotations.filterIsInstance<SpecMinimum>().firstOrNull()?.let {
                    entries["minimum"] = JsonPrimitive(it.value)
                }
                entries.putAll(FIELD_OVERRIDES[defName]?.get(name).orEmpty())
            }
        }
        return orderedObject(entries)
    }

    /** A $ref sibling description (2020-12 allows annotations next to $ref). */
    private fun withDescription(ref: JsonObject, doc: String?, entries: Map<String, JsonElement>): JsonObject =
        if (doc == null) {
            ref
        } else {
            orderedObject(linkedMapOf("description" to JsonPrimitive(doc)) + ref)
        }

    private fun scalarType(descriptor: SerialDescriptor): String =
        when (descriptor.serialName.removeSuffix("?")) {
            "kotlin.String" -> "string"
            "kotlin.Int", "kotlin.Long" -> "integer"
            "kotlin.Double", "kotlin.Float" -> "number"
            "kotlin.Boolean" -> "boolean"
            else -> error("Generator: unsupported scalar ${descriptor.serialName}")
        }

    private fun extensionsMapDef(): JsonObject =
        orderedObject(
            "description" to JsonPrimitive(EXTENSIONS_MAP_DESCRIPTION),
            "type" to JsonPrimitive("object"),
            "properties" to
                JsonObject(
                    mapOf(
                        "custom" to
                            orderedObject(
                                "description" to
                                    JsonPrimitive("Custom extensions, each in the fixed envelope."),
                                "type" to JsonPrimitive("array"),
                                "items" to ref("extension"),
                            ),
                    ),
                ),
            "additionalProperties" to JsonPrimitive(true),
        )

    private fun ref(name: String): JsonObject = JsonObject(mapOf("\$ref" to JsonPrimitive("#/\$defs/$name")))

    private fun docOrNull(annotations: List<Annotation>): String? =
        annotations.filterIsInstance<SpecDoc>().firstOrNull()?.value

    private fun docOf(annotations: List<Annotation>, what: String): String =
        docOrNull(annotations) ?: error("Generator: $what is missing @SpecDoc")

    private fun orderedObject(vararg pairs: Pair<String, JsonElement>): JsonObject =
        JsonObject(linkedMapOf(*pairs))

    private fun orderedObject(entries: Map<String, JsonElement>): JsonObject =
        JsonObject(LinkedHashMap(entries))
}

@OptIn(ExperimentalSerializationApi::class)
fun main(args: Array<String>) {
    val out = File(args.firstOrNull()?.takeIf { it.isNotEmpty() } ?: "ontology-graph-spec.schema.json").absoluteFile
    out.parentFile?.mkdirs()
    val json = Json { prettyPrint = true }
    out.writeText(json.encodeToString(JsonObject.serializer(), OntologyGraphSpecJsonSchemaGenerator.generate()) + "\n")
    println("Generated ${out.name}")
}
