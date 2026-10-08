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
package migrate.migration.dataModel

import codec.schema.SchemaLiteral
import codec.schema.SchemaMap
import codec.schema.SchemaNull
import codec.schema.schemaMapOf
import codec.schema.toNotEmpty
import migrate.Migration
import model.NameFormat
import model.Type
import model.Version
import model.type.ConstraintType
import model.type.IndexType
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator

class GraphSpecDataModelV3Migration(private val wrapped: Boolean = false) :
    Migration(
        fromType = Type.GRAPH_SPEC,
        from = Version.LATEST,
        toType = if (wrapped) {
            Type.DATA_MODEL_WRAPPED
        } else {
            Type.DATA_MODEL
        },
        to = Version.DATA_MODEL_V30
    ) {

    override fun migrate(schema: SchemaMap): SchemaMap {
        val constraints = mutableListOf<SchemaMap>()
        val indexes = mutableListOf<SchemaMap>()
        val nodeData = convertNodes(schema, constraints, indexes)
        val relData = convertRelationships(schema, constraints, indexes)
        val dataModel = schemaMapOf(
            "version" to "3.0.0",
            "graphSchemaRepresentation" to schemaMapOf(
                "version" to "1.0.0",
                "graphSchema" to schemaMapOf(
                    "nodeLabels" to (nodeData?.labelsMap ?: emptyList()),
                    "relationshipTypes" to (relData?.typesMap ?: emptyList()),
                    "nodeObjectTypes" to (nodeData?.objectTypes ?: emptyList()),
                    "relationshipObjectTypes" to (relData?.objectTypes ?: emptyList()),
                    "constraints" to constraints,
                    "indexes" to indexes
                )
            ),
            "graphSchemaExtensionsRepresentation" to convertExtensions(schema),
            "graphMappingRepresentation" to convertGraphMapping(schema),
            "configurations" to schemaMapOf("idsToIgnore" to emptyList<String>())
        )
        if (!wrapped) {
            return dataModel
        }
        return schemaMapOf(
            "version" to "3.0.0",
            "dataModel" to dataModel,
            "visualisation" toNotEmpty convertVisualisation(schema),
            "description" to schema.literalOrNull("description")
        )
    }

    private fun convertGraphMapping(schema: SchemaMap): SchemaMap {
        val relationships = schema.mapOfMapsOrNull("relationships").orEmpty()
        val mappings = schema.mapOrNull("extensions")?.listOfMapsOrNull("mappings").orEmpty()
        val (nodeMappings, relationshipMappings) = mappings.mapNotNull { mapping ->
            when {
                mapping.containsKey("node") -> "node" to schemaMapOf(
                    "node" to refOf(mapping.string("node")),
                    "tableName" to mapping.literal("table"),
                    "propertyMappings" to convertPropertyMappings(mapping.mapOfMapsOrNull("properties"))
                )
                mapping.containsKey("relationship") -> {
                    val relId = findRelationshipId(relationships, mapping) ?: return@mapNotNull null
                    "rel" to schemaMapOf(
                        "relationship" to refOf(relId),
                        "tableName" to mapping.literal("table"),
                        "fromMappings" toNotEmpty
                            convertEntityMap(mapping.map("from_node").mapOfMapsOrNull("properties")),
                        "toMappings" toNotEmpty convertEntityMap(mapping.map("to_node").mapOfMapsOrNull("properties")),
                        "propertyMappings" to convertPropertyMappings(mapping.mapOfMapsOrNull("properties"))
                    )
                }
                else -> error("Invalid mapping type, must be node or relationship.")
            }
        }.partition { it.first == "node" }
        return schemaMapOf(
            "dataSourceSchema" to convertSourceSchema(schema.mapOrNull("extensions")?.mapOfMapsOrNull("tables")),
            "nodeMappings" to nodeMappings.map { it.second },
            "relationshipMappings" to relationshipMappings.map { it.second }
        )
    }

    internal fun convertExtensions(schema: SchemaMap): SchemaMap = schemaMapOf(
        "nodeKeyProperties" to convertMappingKeyProperties(schema, "node"),
        "relationshipKeyProperties" toNotEmpty convertMappingKeyProperties(schema, "relationship")
    )

    internal fun convertMappingKeyProperties(schema: SchemaMap, singular: String): List<SchemaMap> {
        val keyProperties = mutableMapOf<String, MutableSet<String>>()
        for (mapping in schema.mapOrNull("extensions")?.listOfMapsOrNull("mappings").orEmpty()) {
            val entity = mapping.stringOrNull(singular) ?: continue
            val keys = mapping.listOrNull("key") ?: continue
            if (keys.isNotEmpty()) {
                val set = keyProperties.getOrPut(entity) { mutableSetOf() }
                set.addAll(keys.map { id -> (id as SchemaLiteral).string })
            }
        }
        return keyProperties.map { (entity, keys) ->
            schemaMapOf(
                singular to refOf(entity),
                "keyProperties" to keys.map { refOf(it) }
            )
        }
    }

    /**
     * Recovers the lost relationship ObjectType reference matching the unique combo of (relId, from, to)
     */
    private fun findRelationshipId(relationships: Map<String, SchemaMap>, mapping: SchemaMap): String? {
        val id = mapping.string("relationship")
        val fromNode = mapping.map("from_node").string("node")
        val toNode = mapping.map("to_node").string("node")
        return relationships.entries.firstOrNull { (key, rel) ->
            key == id &&
                rel.map("from").string("node") == fromNode &&
                rel.map("to").string("node") == toNode
        }?.key
    }

    private data class RelationshipData(
        val typesMap: List<SchemaMap>,
        val objectTypes: List<SchemaMap>,
        val constraints: List<SchemaMap>,
        val indexes: List<SchemaMap>
    )

    private fun convertRelationships(
        schema: SchemaMap,
        constraints: MutableList<SchemaMap>,
        indexes: MutableList<SchemaMap>
    ): RelationshipData? {
        val relationships = schema.mapOfMapsOrNull("relationships") ?: return null
        val relationTypes = mutableListOf<SchemaMap>()
        val relationshipObjectTypes = mutableListOf<SchemaMap>()
        for ((relId, rel) in relationships) {
            val typeToken = rel.string("type")
            // FIXME if we look-up existing tokens then all relationships get combined
            //      if do don't then joint relationships always get separated
            val typeId = "rt:${relationTypes.size}"
            relationTypes.add(
                schemaMapOf(
                    "\$id" to typeId,
                    "token" to typeToken,
                    "properties" to convertProperties(rel.mapOfMapsOrNull("properties"))
                )
            )

            relationshipObjectTypes.add(
                schemaMapOf(
                    "\$id" to relId,
                    "type" to refOf(typeId),
                    "from" to refOf(rel.map("from").string("node")),
                    "to" to refOf(rel.map("to").string("node")),
                    "description" to rel.literalOrNull("description")
                )
            )

            val propertyTokens = propertyTokens(rel.mapOfMapsOrNull("properties"))
            constraints.addAll(
                convertElements(
                    elements = rel.mapOfMapsOrNull("constraints"),
                    entityType = "relationship",
                    refId = typeId,
                    entityToken = typeToken,
                    propertyTokens = propertyTokens,
                    typeKey = "constraintType",
                    typeTransform = ::constraintType
                )
            )
            indexes.addAll(
                convertElements(
                    elements = rel.mapOrNull("extensions")?.mapOfMapsOrNull("indexes"),
                    entityType = "relationship",
                    refId = typeId,
                    entityToken = typeToken,
                    propertyTokens = propertyTokens,
                    typeKey = "indexType",
                    typeTransform = ::indexType
                )
            )
        }
        return RelationshipData(
            typesMap = relationTypes,
            objectTypes = relationshipObjectTypes,
            constraints = constraints,
            indexes = indexes
        )
    }

    private data class NodeData(
        val labelsMap: List<Map<String, Any?>>,
        val objectTypes: List<SchemaMap>,
        val constraints: List<SchemaMap>,
        val indexes: List<SchemaMap>
    )

    private fun convertNodes(
        schema: SchemaMap,
        constraints: MutableList<SchemaMap>,
        indexes: MutableList<SchemaMap>
    ): NodeData? {
        val nodes = schema.mapOfMapsOrNull("nodes") ?: return null
        val nodeLabelsMap = mutableMapOf<String, String>()
        val nodeLabels = mutableListOf<SchemaMap>()
        val nodeObjectTypes = mutableListOf<SchemaMap>()
        for ((nodeId, node) in nodes) {
            val labelsInfo =
                node.mapOrNull("labels")
                    ?: error(
                        "Missing required labels at ${node.path}.labels - Make sure you are using internal version."
                    )
            val primaryLabel = labelsInfo.string("identifier")
            val impliedLabels = labelsInfo.listOrNull("implied")?.map { it.toString() } ?: emptyList()
            val optionalLabels = labelsInfo.listOrNull("optional")?.map { it.toString() } ?: emptyList()
            val allLabels = listOf(primaryLabel) + impliedLabels + optionalLabels

            var primaryLabelId = "nl:null"
            val labelRefs = allLabels.map { label ->
                var labelId = nodeLabelsMap[label]
                if (labelId == null) {
                    labelId = "nl:${nodeLabels.size}"
                    nodeLabelsMap[label] = labelId
                    nodeLabels.add(
                        schemaMapOf(
                            "\$id" to labelId,
                            "token" to label,
                            "properties" to convertProperties(node.mapOfMapsOrNull("properties"))
                        )
                    )
                }
                if (label == primaryLabel) {
                    primaryLabelId = labelId
                }
                refOf(labelId)
            }
            nodeObjectTypes.add(
                schemaMapOf(
                    "\$id" to nodeId,
                    "labels" to labelRefs,
                    "description" to node.literalOrNull("description")
                )
            )
            val propertyTokens = propertyTokens(node.mapOfMapsOrNull("properties"))
            constraints.addAll(
                convertElements(
                    elements = node.mapOfMapsOrNull("constraints"),
                    entityType = "node",
                    refId = primaryLabelId,
                    entityToken = primaryLabel,
                    propertyTokens = propertyTokens,
                    typeKey = "constraintType",
                    typeTransform = ::constraintType
                )
            )
            indexes.addAll(
                convertElements(
                    elements = node.mapOrNull("extensions")?.mapOfMapsOrNull("indexes"),
                    entityType = "node",
                    refId = primaryLabelId,
                    entityToken = primaryLabel,
                    propertyTokens = propertyTokens,
                    typeKey = "indexType",
                    typeTransform = ::indexType
                )
            )
        }
        return NodeData(nodeLabels, nodeObjectTypes, constraints, indexes)
    }

    private fun convertSourceSchema(tables: Map<String, SchemaMap>?): SchemaMap {
        if (tables == null) {
            return schemaMapOf(
                "type" to SchemaNull(),
                "tableSchemas" to emptyList<SchemaMap>()
            )
        }
        val tableSchemas = mutableListOf<SchemaMap>()
        var sourceType: Any? = null
        for ((tableName, table) in tables) {
            if (sourceType == null) {
                sourceType = table.literalOrNull("source")
            }
            tableSchemas.add(
                schemaMapOf(
                    "name" to tableName,
                    "expanded" to true,
                    "fields" to convertFields(table.mapOfMapsOrNull("columns")),
                    "primaryKeys" to table.listOrNull("primaryKeys"),
                    "foreignKeys" to convertForeignKeys(table.mapOfMapsOrNull("foreignKeys"))
                )
            )
        }
        return schemaMapOf(
            "type" to (sourceType ?: SchemaNull()),
            "tableSchemas" to tableSchemas
        )
    }

    internal fun convertVisualisation(schema: SchemaMap): SchemaMap {
        val nodes = schema.mapOfMapsOrNull("nodes").orEmpty()
        return schemaMapOf(
            "nodes" toNotEmpty nodes.mapNotNull { (id, node) ->
                val pos = node.mapOrNull("extensions")?.mapOrNull("display") ?: return@mapNotNull null
                schemaMapOf(
                    "id" to id,
                    "position" to schemaMapOf(
                        "x" to pos.literal("x"),
                        "y" to pos.literal("y")
                    )
                )
            }
        )
    }

    private fun propertyTokens(properties: Map<String, SchemaMap>?): Map<String, String> =
        properties.orEmpty().mapValues { (id, property) -> property.stringOrNull("name") ?: id }

    internal fun convertProperties(properties: Map<String, SchemaMap>?): List<SchemaMap> {
        if (properties.isNullOrEmpty()) {
            return emptyList()
        }
        return properties.map { (propId, prop) ->
            val map = schemaMapOf(
                "\$id" to propId,
                "token" to (prop.stringOrNull("name") ?: propId),
                "type" to propertyType(prop.string("type"), prop.intOrNull("dimension")),
                "nullable" to false,
                "description" to prop.literalOrNull("description")
            )
            map
        }
    }

    internal fun convertElements(
        elements: Map<String, SchemaMap>?,
        entityType: String,
        refId: String,
        entityToken: String,
        propertyTokens: Map<String, String>,
        typeKey: String,
        typeTransform: (String) -> String
    ): List<SchemaMap> {
        if (elements.isNullOrEmpty()) {
            return emptyList()
        }
        return elements.map { (name, element) ->
            val propertyIds = element.listOrNull("properties")?.map { propId ->
                (propId as SchemaLiteral).string
            } ?: emptyList()
            val type = typeTransform(element.string("type"))
            schemaMapOf(
                "\$id" to name,
                "name" to element.resolvedName(propertyIds, propertyTokens, entityToken, typeKey),
                typeKey to type,
                "entityType" to entityType,
                "nodeLabel" to if (entityType == "node") refOf(refId) else SchemaNull(),
                "properties" to propertyIds.map { refOf(it) },
                "relationshipType" to if (entityType == "relationship") refOf(refId) else SchemaNull(),
                "options" toNotEmpty element.mapOrNull("options")
            )
        }
    }

    private fun SchemaMap.resolvedName(
        propertyIds: List<String>,
        propertyTokens: Map<String, String>,
        entityToken: String,
        typeKey: String
    ): String {
        stringOrNull("name")?.takeUnless { it.isBlank() }?.let { return it }
        val tokens = propertyIds.map { propertyTokens[it] ?: it }
        if (tokens.isEmpty()) {
            return ""
        }
        val typeName = string("type")
        return if (typeKey == "constraintType") {
            val type = ConstraintType.entries.find { it.name == typeName }
                ?: error("Unknown constraint type: '$typeName' at $path.type")
            NameFormat.constraintName(tokens, entityToken, type)
        } else {
            val type = IndexType.entries.find { it.name == typeName }
                ?: error("Unknown index type: '$typeName' at $path.type")
            NameFormat.indexName(tokens, entityToken, type)
        }
    }

    internal fun convertFields(fields: Map<String, SchemaMap>?): List<SchemaMap> {
        if (fields.isNullOrEmpty()) {
            return emptyList()
        }
        return fields.values.map { field ->
            schemaMapOf(
                "name" to field.literalOrNull("name"),
                "rawType" to field.literalOrNull("type"),
                "size" to field.literalOrNull("size"),
                "recommendedType" to field.literalOrNull("suggested")?.let {
                    propertyType(it.string, field.intOrNull("dimension"))
                },
                "supportedTypes" to field.listOrNull("supported")?.map {
                    propertyType((it as SchemaLiteral).string, field.intOrNull("dimension"))
                }
            )
        }
    }

    internal fun convertForeignKeys(foreignKeys: Map<String, SchemaMap>?): List<SchemaMap> {
        if (foreignKeys.isNullOrEmpty()) {
            return emptyList()
        }
        return foreignKeys.values.map { fk ->
            val columns = fk.list("columns").map { it.toString() }
            val references = fk.map("references")
            val referencedColumns = references.list("columns").map { it.toString() }

            val fieldMaps = columns.indices.map { i ->
                schemaMapOf(
                    "field" to columns[i],
                    "referencedField" to (referencedColumns.getOrNull(i) ?: referencedColumns.last())
                    // TODO set or list with duplicates?
                )
            }

            schemaMapOf(
                "referencedTable" to references.literal("table"),
                "fields" to fieldMaps
            )
        }
    }

    internal fun convertPropertyMappings(properties: Map<String, SchemaMap>?): List<SchemaMap> {
        if (properties.isNullOrEmpty()) {
            return emptyList()
        }
        return properties.map { (propId, propDef) ->
            schemaMapOf(
                "fieldName" to propDef.literal("column"),
                "property" to refOf(propId)
            )
        }
    }

    internal fun convertEntityMap(properties: Map<String, SchemaMap>?): Map<String, Any> {
        if (properties.isNullOrEmpty()) {
            return emptyMap()
        }
        return properties.entries.associate { (key, value) ->
            "#$key" to value.literal("column")
        }
    }

    companion object {
        private fun type(string: String?): String? = when (string?.uppercase()) {
            "LOCAL DATETIME" -> "localdatetime"
            "ZONED DATETIME" -> "datetime"
            "STRING" -> "string"
            "INTEGER" -> "integer"
            "FLOAT" -> "float"
            "DATE" -> "date"
            "BOOLEAN" -> "boolean"
            "ANY" -> null
            "LOCAL TIME" -> "localtime"
            "ZONED TIME" -> "time"
            "POINT" -> "point"
            else -> string?.lowercase()
        }

        private fun propertyType(propertyType: String?, dimension: Int? = null) = when {
            propertyType == "ANY" -> SchemaNull()
            propertyType != null && propertyType.startsWith("VECTOR") -> schemaMapOf(
                "type" to "vector",
                "items" to schemaMapOf("type" to type(propertyType.removePrefix("VECTOR<").removeSuffix(">"))),
                "dimension" to dimension
            )
            propertyType != null && propertyType.startsWith("LIST") -> schemaMapOf(
                "type" to "array",
                "items" to schemaMapOf("type" to type(propertyType.removePrefix("LIST<").removeSuffix(">")))
            )
            else -> schemaMapOf("type" to type(propertyType))
        }

        private fun constraintType(name: String): String = when (name) {
            "UNIQUE" -> "uniqueness"
            "EXISTS" -> "propertyExistence"
            "PROPERTY_TYPE" -> "propertyType"
            "KEY" -> "key"
            else -> name.lowercase()
        }

        private fun indexType(name: String): String = when (name) {
            "LOOKUP" -> "lookup"
            "RANGE" -> "range"
            "FULLTEXT" -> "fullText"
            "POINT" -> "point"
            "TEXT" -> "text"
            "VECTOR" -> "vector"
            else -> name.lowercase()
        }
    }
}
