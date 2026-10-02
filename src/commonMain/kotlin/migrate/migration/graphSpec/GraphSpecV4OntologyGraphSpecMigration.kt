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
package migrate.migration.graphSpec

import codec.schema.SchemaElement
import codec.schema.SchemaList
import codec.schema.SchemaLiteral
import codec.schema.SchemaMap
import codec.schema.SchemaNull
import codec.schema.schemaMapOf
import migrate.Migration
import model.Type
import model.Version
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/**
 * The one-way graph spec 4.0.0 -> ontology graph spec 1.0.0 converter (ADR-0008), the single
 * `Migration` on the kept `MigrationPath` machinery (ADR-0004 track 3, ADR-0006). Every
 * rule is a map -> map transform over the codec `SchemaMap` tree; no 4.0.0 model class
 * is referenced. Decode (JSON or YAML, pretty or internal form) happens in the codec
 * layer before this runs; encode as v1 happens through the v1 model after it.
 *
 * Pre-4.0.0 inputs (data model 2.3/2.4/3.0, import_spec 1.0.0) are out of scope and
 * rejected (ADR-0008 §10): holders run the graph-spec 4.x tooling first.
 */
class GraphSpecV4OntologyGraphSpecMigration :
    Migration(
        fromType = Type.GRAPH_SPEC,
        from = Version.GRAPH_SPEC_V4,
        toType = Type.ONTOLOGY_GRAPH_SPEC,
        to = Version.LATEST
    ) {

    @OptIn(ExperimentalUuidApi::class)
    override fun migrate(schema: SchemaMap): SchemaMap {
        // ADR-0008 §0: graph_spec 4.0.x only; anything else belongs to the 4.x tooling.
        // The truncation mirrors MigrationPath.version(), which routes any 4.0.x patch.
        val version = schema.stringOrNull("version")
            ?: error("Unsupported input (missing version); migrate to graph spec 4.0.0 first")
        var semver = version.substringBefore('-').substringBefore('+')
        if (semver.count { it == '.' } > 1) {
            semver = "${semver.substringBeforeLast('.')}.0"
        }
        require(semver == Version.GRAPH_SPEC_V4) {
            "Unsupported input (graph spec $version); migrate to graph spec 4.0.0 first"
        }

        val ids = GraphSpecV4Normalizer(schema)

        // ADR-0008 §1: the 4.0.0 format version becomes the $schema link; the ontology
        // gets a fresh identity (random UUID, own version 1).
        val out = schemaMapOf(
            "\$schema" to SCHEMA_ID,
            "id" to Uuid.random().toString(),
            "version" to 1,
            "name" to schema.stringOrNull("name"),
            "description" to schema.stringOrNull("description")?.takeIf { it.isNotEmpty() }
        )
        convertElements(schema.mapOfMapsOrNull("nodes"), ids, ::convertNode)?.let { out["nodes"] = it }
        convertElements(schema.mapOfMapsOrNull("relationships"), ids, ::convertRelationship)?.let {
            out["relationships"] = it
        }
        rootExtensions(schema, ids)?.let { out["extensions"] = it }
        return out
    }

    // ADR-0008 §4/§5: nodes and relationships share the element shape (properties,
    // constraints, indexes, extensions, description); only identity fields differ.
    private fun convertElements(
        elements: Map<String, SchemaMap>?,
        ids: GraphSpecV4Normalizer,
        convert: (String, SchemaMap, GraphSpecV4Normalizer) -> SchemaMap
    ): SchemaMap? {
        if (elements.isNullOrEmpty()) return null
        val out = SchemaMap()
        for ((key, element) in elements) {
            val id = GraphSpecV4Normalizer.idOf(key, element)
            out[id] = convert(key, element, ids)
        }
        return out
    }

    private fun convertNode(key: String, node: SchemaMap, ids: GraphSpecV4Normalizer): SchemaMap {
        val id = GraphSpecV4Normalizer.idOf(key, node)
        val out = SchemaMap()
        // ADR-0008 §4: labels.identifier ?: label ?: the resolved id; shorthand `label`
        // when no implied/optional labels exist.
        val labels = node.mapOrNull("labels")
        val identifier = labels?.stringOrNull("identifier") ?: node.stringOrNull("label") ?: id
        val implied = labels?.listOrNull("implied")?.map { it.toString() }.orEmpty()
        val optional = labels?.listOrNull("optional")?.map { it.toString() }.orEmpty()
        if (implied.isEmpty() && optional.isEmpty()) {
            out["label"] = identifier
        } else {
            out["labels"] = schemaMapOf(
                "identifier" to identifier,
                "implied" to implied.takeIf { it.isNotEmpty() },
                "optional" to optional.takeIf { it.isNotEmpty() }
            )
        }
        // ADR-0008 §9: `labels` is a closed shape in v1; its extensions hoist up.
        val hoisted = untagExtensions(labels?.mapOrNull("extensions"))
        convertElementBody(node, id, ids.nodePropertyIds[key].orEmpty(), ids, out, hoisted)
        return out
    }

    private fun convertRelationship(key: String, relationship: SchemaMap, ids: GraphSpecV4Normalizer): SchemaMap {
        val id = GraphSpecV4Normalizer.idOf(key, relationship)
        val out = schemaMapOf("type" to relationship.stringOrNull("type"))
        // ADR-0008 §5: endpoint `node` references resolve through the id map; `label`
        // is dropped (reachable through the referenced node); 4.0.0 has no cardinality,
        // so endpoints are unconstrained (0..*) and nothing is synthesised.
        for (endpoint in listOf("from", "to")) {
            relationship.mapOrNull(endpoint)?.let { target ->
                out[endpoint] = schemaMapOf("node" to ids.nodeId(target.string("node")))
            }
        }
        convertElementBody(relationship, id, ids.relationshipPropertyIds[key].orEmpty(), ids, out, null)
        return out
    }

    /** The shared §4/§5 body: description, properties, constraints, indexes, extensions. */
    private fun convertElementBody(
        element: SchemaMap,
        id: String,
        propertyIds: Map<String, String>,
        ids: GraphSpecV4Normalizer,
        out: SchemaMap,
        hoisted: SchemaMap?
    ) {
        element.stringOrNull("description")?.takeIf { it.isNotEmpty() }?.let { out["description"] = it }
        convertProperties(element.mapOfMapsOrNull("properties"))?.let { out["properties"] = it }
        val extensions = elementExtensions(element, propertyIds, ids)
        mergeExtensions(extensions, convertConstraints(element.mapOfMapsOrNull("constraints"), id, propertyIds, out))
        mergeExtensions(extensions, hoisted)
        element.mapOrNull("extensions")?.let { mergeExtensions(extensions, untagExtensions(it)) }
        if (!extensions.isEmpty()) {
            out["extensions"] = extensions
        }
    }

    // ADR-0008 §7: type per §3, dimension carried, flags per §6, description when
    // non-empty, `name` consumed by §2. 4.0.0 has no one_of/pattern/aliases/reference.
    private fun convertProperties(properties: Map<String, SchemaMap>?): SchemaMap? {
        if (properties.isNullOrEmpty()) return null
        val out = SchemaMap()
        for ((key, property) in properties) {
            val id = GraphSpecV4Normalizer.idOf(key, property)
            val converted = schemaMapOf(
                "type" to property.stringOrNull("type")?.let(Neo4jTypeTokens::map),
                "dimension" to property.literalOrNull("dimension")?.let(::copyOf),
                "mustExist" to property.literalOrNull("mustExist")?.let(::copyOf),
                "unique" to property.literalOrNull("unique")?.let(::copyOf),
                "key" to property.literalOrNull("key")?.let(::copyOf),
                "description" to property.stringOrNull("description")?.takeIf { it.isNotEmpty() }
            )
            property.mapOrNull("extensions")?.let { ext ->
                untagExtensions(ext)?.let { converted["extensions"] = it }
            }
            out[id] = converted
        }
        return out
    }

    /**
     * ADR-0008 §6: constraints become property shorthand flags if and only if they
     * constrain exactly one property, are unnamed or carry the 4.0.0 machine-generated
     * deterministic id, and the referenced property exists on the owning element.
     * Everything else is a constraint object, input order preserved. PROPERTY_TYPE is
     * dropped (redundant with the property's `type`), NodeConstraint's `label` is
     * dropped, and constraint/label extensions hoist to the enclosing element (§9).
     * The constraint name resolves name-or-key like every 4.0.0 identity (Pretty.kt
     * moved human names into the map key); a machine-generated name is treated as
     * absent, a human-given one is carried.
     *
     * Returns the hoisted constraint extensions (§9), or null when none.
     */
    private fun convertConstraints(
        constraints: Map<String, SchemaMap>?,
        ownerId: String,
        propertyIds: Map<String, String>,
        out: SchemaMap
    ): SchemaMap? {
        if (constraints.isNullOrEmpty()) return null
        val objects = mutableListOf<SchemaMap>()
        val hoisted = SchemaMap()
        for ((key, constraint) in constraints) {
            constraint.mapOrNull("extensions")?.let { mergeExtensions(hoisted, untagExtensions(it)) }
            val type = constraint.string("type")
            if (type == "PROPERTY_TYPE") continue // redundant with the property's type
            val constraintType = when (type) {
                "EXISTS" -> "mustExist"
                "KEY" -> "key"
                "UNIQUE" -> "unique"
                else -> error("Unsupported 4.0.0 constraint type '$type' at ${constraint.path}")
            }
            val properties = constraint.listOrNull("properties")?.map { it.toString() }.orEmpty()
            val resolved = properties.map { propertyIds[it] ?: it }
            // Name-or-key like every 4.0.0 identity: Pretty.kt moved human names into
            // the map key, internalisation pushed the pretty key into `name` — both
            // forms resolve to the same name, so both converge on the same v1 shape.
            val name = constraint.stringOrNull("name") ?: key
            val machineNamed = name == deterministicId(type, ownerId, resolved)
            if (resolved.size == 1 && machineNamed && propertyIds.containsKey(properties.first())) {
                // The §6 flag rule; the property is guaranteed on the output element.
                out.map("properties").map(resolved.first())[constraintType] = true
                continue
            }
            objects.add(
                schemaMapOf(
                    "constraint_type" to constraintType,
                    "name" to name.takeIf { !machineNamed },
                    "properties" to resolved
                )
            )
        }
        if (objects.isNotEmpty()) {
            out["constraints"] = objects
        }
        return hoisted.takeIf { !it.isEmpty() }
    }

    /**
     * 4.0.0's `Internal.deterministicId`: `<type-lowercase>_<owner>_<sorted distinct
     * properties>`, recomputed on resolved (pretty) ids — e.g. `unique_categories_categoryid`.
     */
    private fun deterministicId(type: String, ownerId: String, properties: List<String>): String =
        "${type.lowercase()}_${ownerId}_${properties.distinct().sorted().joinToString("_")}"

    // ADR-0008 §8: indexes become `neo4j:index` payload lists on the owning element's
    // extensions. The index name resolves name-or-key (internal form carries the pretty
    // key in `name`); `labels` and `properties` rewrite through the §2 id maps.
    private fun elementExtensions(
        element: SchemaMap,
        propertyIds: Map<String, String>,
        ids: GraphSpecV4Normalizer
    ): SchemaMap {
        val extensions = SchemaMap()
        val indexes = element.mapOfMapsOrNull("indexes").orEmpty()
        if (indexes.isNotEmpty()) {
            val payloads = indexes.map { (indexKey, index) ->
                val payload = schemaMapOf(
                    "type" to index.stringOrNull("type"),
                    "name" to (index.stringOrNull("name") ?: indexKey),
                    "labels" to index.listOrNull("labels")?.map { ids.nodeId(it.toString()) },
                    "properties" to index.listOrNull("properties")?.map { propertyIds[it.toString()] ?: it.toString() },
                    "options" to index.mapOrNull("options")?.let { untagValues(it) }
                )
                index.mapOrNull("extensions")?.let { ext ->
                    untagExtensions(ext)?.let { payload["extensions"] = it }
                }
                payload
            }
            extensions["neo4j:index"] = payloads
        }
        return extensions
    }

    // ADR-0008 §8: tables, mappings and display describe several elements or none, so
    // they live at the top level as named extension payloads (always lists for the
    // multi-instance ones — one wire form, design rule 8).
    private fun rootExtensions(schema: SchemaMap, ids: GraphSpecV4Normalizer): SchemaMap? {
        val extensions = SchemaMap()
        schema.mapOfMapsOrNull("tables")?.takeIf { it.isNotEmpty() }?.let { tables ->
            extensions["neo4j-importer:table"] = tables.map { (name, table) -> convertTable(name, table) }
        }
        schema.listOfMapsOrNull("mappings")?.takeIf { it.isNotEmpty() }?.let { mappings ->
            extensions["neo4j-importer:mapping"] = mappings.map { convertMapping(it, ids) }
        }
        schema.mapOrNull("display")?.let { display ->
            val nodes = SchemaMap()
            for ((key, position) in display.mapOfMapsOrNull("nodes").orEmpty()) {
                val entry = schemaMapOf(
                    "x" to position.literalOrNull("x")?.let(::copyOf),
                    "y" to position.literalOrNull("y")?.let(::copyOf)
                )
                position.mapOrNull("extensions")?.let { ext ->
                    untagExtensions(ext)?.let { entry["extensions"] = it }
                }
                nodes[ids.nodeId(key)] = entry
            }
            extensions["neo4j:display"] = schemaMapOf("nodes" to nodes)
        }
        return extensions.takeIf { !it.isEmpty() }
    }

    // ADR-0008 §8: `{name, source, columns, primaryKeys?, foreignKeys?}`; fields carried
    // verbatim except columns.*.suggested/.supported (remapped per §3) and nested tagged
    // extensions (untagged per §9). A v1 document never carries 4.0.0-era type tokens.
    private fun convertTable(name: String, table: SchemaMap): SchemaMap {
        val payload = schemaMapOf("name" to name)
        for ((key, value) in table) {
            when (key) {
                "columns" -> payload["columns"] = convertColumns(table.mapOfMaps("columns"))
                "extensions" -> untagExtensions(table.map("extensions"))?.let { payload["extensions"] = it }
                "foreignKeys" -> payload["foreignKeys"] = convertForeignKeys(table.mapOfMaps("foreignKeys"))
                else -> payload[key] = copyOf(value)
            }
        }
        return payload
    }

    private fun convertColumns(columns: Map<String, SchemaMap>): SchemaMap {
        val out = SchemaMap()
        for ((name, column) in columns) {
            val converted = SchemaMap()
            for ((key, value) in column) {
                when (key) {
                    "suggested" -> column.stringOrNull("suggested")?.let {
                        converted["suggested"] =
                            Neo4jTypeTokens.map(it)
                    }
                    "supported" -> converted["supported"] =
                        column.listOrNull("supported")?.map { Neo4jTypeTokens.map(it.toString()) }.orEmpty()
                    "extensions" -> untagExtensions(column.map("extensions"))?.let { converted["extensions"] = it }
                    else -> converted[key] = copyOf(value)
                }
            }
            out[name] = converted
        }
        return out
    }

    private fun convertForeignKeys(foreignKeys: Map<String, SchemaMap>): SchemaMap {
        val out = SchemaMap()
        for ((name, foreignKey) in foreignKeys) {
            val converted = SchemaMap()
            for ((key, value) in foreignKey) {
                when (key) {
                    "extensions" -> untagExtensions(foreignKey.map("extensions"))?.let { converted["extensions"] = it }
                    "references" -> converted["references"] = convertReferences(foreignKey.map("references"))
                    else -> converted[key] = copyOf(value)
                }
            }
            out[name] = converted
        }
        return out
    }

    private fun convertReferences(references: SchemaMap): SchemaMap {
        val out = SchemaMap()
        for ((key, value) in references) {
            when (key) {
                "extensions" -> untagExtensions(references.map("extensions"))?.let { out["extensions"] = it }
                else -> out[key] = copyOf(value)
            }
        }
        return out
    }

    // ADR-0008 §8: the kind is detected structurally (the 4.0.0 wire does not reliably
    // carry the `type` discriminator); every node/relationship/property reference
    // rewrites through the §2 id maps; `TargetMapping.label` is source-matching info
    // and carries verbatim.
    private fun convertMapping(mapping: SchemaMap, ids: GraphSpecV4Normalizer): SchemaMap = when {
        mapping.containsKey("node") -> {
            val nodeKey = mapping.string("node")
            val propertyIds = ids.nodePropertyIds[nodeKey].orEmpty()
            schemaMapOf(
                "kind" to "node",
                "node" to ids.nodeId(nodeKey),
                "table" to mapping.stringOrNull("table"),
                "properties" to convertPropertyMappings(mapping.mapOfMapsOrNull("properties"), propertyIds),
                "mode" to mapping.stringOrNull("mode"),
                "matchLabel" to mapping.stringOrNull("matchLabel"),
                "key" to mapping.listOrNull("key")?.map { propertyIds[it.toString()] ?: it.toString() }
            )
        }
        mapping.containsKey("relationship") -> {
            val relationshipKey = mapping.string("relationship")
            val propertyIds = ids.relationshipPropertyIds[relationshipKey].orEmpty()
            // 4.0.0 internalised a relationship mapping's `key` through the endpoint
            // node property namespaces (Pretty.kt renamed them with parent from.node /
            // to.node) while `properties` internalised through the relationship's own —
            // so `key` resolves relationship-first, then from-node, then to-node, and
            // both serialisations converge (e.g. the ldbc corpus pair).
            val fromKey = (mapping.mapOrNull("start_node") ?: mapping.mapOrNull("from"))?.stringOrNull("node")
            val toKey = (mapping.mapOrNull("end_node") ?: mapping.mapOrNull("to"))?.stringOrNull("node")
            val resolveKey: (String) -> String = { ref ->
                propertyIds[ref]
                    ?: fromKey?.let { ids.nodePropertyIds[it]?.get(ref) }
                    ?: toKey?.let { ids.nodePropertyIds[it]?.get(ref) }
                    ?: ref
            }
            schemaMapOf(
                "kind" to "relationship",
                "relationship" to ids.relationshipId(relationshipKey),
                "table" to mapping.stringOrNull("table"),
                // 4.0.0's final wire shape renamed the mapping target fields
                // from/to → start_node/end_node (graph-spec #103); accept both.
                "from" to convertTargetMapping(mapping.mapOrNull("start_node") ?: mapping.map("from"), ids),
                "to" to convertTargetMapping(mapping.mapOrNull("end_node") ?: mapping.map("to"), ids),
                "properties" to convertPropertyMappings(mapping.mapOfMapsOrNull("properties"), propertyIds),
                "mode" to mapping.stringOrNull("mode"),
                "matchLabel" to mapping.stringOrNull("matchLabel"),
                "key" to mapping.listOrNull("key")?.map { resolveKey(it.toString()) }
            )
        }
        mapping.containsKey("query") -> schemaMapOf(
            "kind" to "query",
            "table" to mapping.stringOrNull("table"),
            "query" to mapping.stringOrNull("query")
        )
        else -> error("Invalid 4.0.0 mapping at ${mapping.path}: must be node, relationship or query")
    }

    private fun convertTargetMapping(target: SchemaMap, ids: GraphSpecV4Normalizer): SchemaMap {
        val nodeKey = target.stringOrNull("node")
        val propertyIds = nodeKey?.let { ids.nodePropertyIds[it].orEmpty() }.orEmpty()
        return schemaMapOf(
            "node" to nodeKey?.let(ids::nodeId),
            "label" to target.stringOrNull("label"),
            "properties" to convertPropertyMappings(target.mapOfMapsOrNull("properties"), propertyIds)
        )
    }

    private fun convertPropertyMappings(
        properties: Map<String, SchemaMap>?,
        propertyIds: Map<String, String>
    ): SchemaMap? {
        if (properties.isNullOrEmpty()) return null
        val out = SchemaMap()
        for ((key, propertyMapping) in properties) {
            out[propertyIds[key] ?: key] = copyOf(propertyMapping)
        }
        return out
    }

    companion object {
        /** The v1 spec link (the schema's `$id`), as used by every v1 example. */
        const val SCHEMA_ID = "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json"

        /** First-party extension owner prefixes (ADR-0005/ADR-0008 §9). */
        private val NAMED_PREFIXES = listOf("neo4j:", "neo4j-importer:")

        /**
         * ADR-0008 §9: 4.0.0's tagged extension tree (`{"String": {"value": X}}`, …)
         * untags recursively to raw JSON; a value that does not match the tagged shape
         * is carried untouched, never rejected.
         */
        internal fun untag(element: SchemaElement): SchemaElement {
            if (element !is SchemaMap || element.size != 1) return copyOf(element)
            val (kind, payload) = element.entries.first()
            val value = (payload as? SchemaMap)?.takeIf { it.size == 1 }?.get("value") ?: return copyOf(element)
            return when (kind) {
                "String", "Boolean", "Long", "Double" -> value as? SchemaLiteral ?: copyOf(element)
                "List" -> (value as? SchemaList)?.let { list ->
                    SchemaList(list.map { untag(it) }.toMutableList())
                } ?: copyOf(element)
                "Map" -> (value as? SchemaMap)?.let { map ->
                    SchemaMap(map.content.mapValuesTo(mutableMapOf()) { untag(it.value) })
                } ?: copyOf(element)
                else -> copyOf(element)
            }
        }

        /** Untags every value of a plain tagged-value map (e.g. index `options`). */
        internal fun untagValues(map: SchemaMap): SchemaMap =
            SchemaMap(map.content.mapValuesTo(mutableMapOf()) { untag(it.value) })

        /**
         * ADR-0008 §9 named-vs-custom: keys under a declared owner prefix ride as named
         * keys with the untagged payload untouched; every other key wraps in the custom
         * envelope (`type` = original key, `definition` = untagged payload), input order
         * preserved. Returns null for absent/empty input ("absent means none").
         */
        internal fun untagExtensions(extensions: SchemaMap?): SchemaMap? {
            if (extensions == null || extensions.isEmpty()) return null
            val out = SchemaMap()
            val custom = mutableListOf<SchemaMap>()
            for ((key, value) in extensions) {
                if (NAMED_PREFIXES.any(key::startsWith)) {
                    out[key] = untag(value)
                } else {
                    custom.add(schemaMapOf("type" to key, "definition" to untag(value)))
                }
            }
            if (custom.isNotEmpty()) {
                out["custom"] = custom
            }
            return out
        }

        /**
         * Merges [extra] into [target]: `custom` lists append (input order preserved);
         * a named key that already holds a list (e.g. a §8 `neo4j:index` payload list
         * meeting a hand-written 4.0.0 extension of the same key) concatenates — content
         * is carried, never rejected; anything else is set.
         */
        internal fun mergeExtensions(target: SchemaMap, extra: SchemaMap?) {
            if (extra == null) return
            for ((key, value) in extra) {
                val existing = target[key]
                target[key] = when {
                    existing is SchemaList && value is SchemaList ->
                        SchemaList((existing.toList() + value.toList()).toMutableList())
                    else -> value
                }
            }
        }

        internal fun copyOf(element: SchemaElement): SchemaElement = when (element) {
            is SchemaMap -> SchemaMap(element.content.mapValuesTo(mutableMapOf()) { copyOf(it.value) })
            is SchemaList -> SchemaList(element.content.mapTo(mutableListOf()) { copyOf(it) })
            is SchemaLiteral -> SchemaLiteral(element.string, isString = element.isString)
            is SchemaNull -> SchemaNull()
        }
    }
}
