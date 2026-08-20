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

import codec.schema.SchemaElement
import codec.schema.SchemaLiteral
import codec.schema.SchemaMap
import codec.schema.schemaMapOf
import codec.schema.toNotEmpty
import migrate.Migration
import model.Type
import model.Version
import model.mapping.MappingType
import model.type.ConstraintType
import model.type.ConstraintType.EXISTS
import model.type.ConstraintType.KEY
import model.type.ConstraintType.PROPERTY_TYPE
import model.type.ConstraintType.UNIQUE
import model.type.IndexType
import model.type.IndexType.FULLTEXT
import model.type.IndexType.LOOKUP
import model.type.IndexType.POINT
import model.type.IndexType.RANGE
import model.type.IndexType.TEXT
import model.type.IndexType.VECTOR

/**
 * 3.0 -> Graph Spec 4.0
 */
class DataModelV3GraphSpecMigration :
    Migration(
        fromType = Type.DATA_MODEL,
        from = Version.DATA_MODEL_V30,
        toType = Type.GRAPH_SPEC,
        to = Version.LATEST
    ) {

    override fun migrate(schema: SchemaMap): SchemaMap {
        val schema = unwrap(schema)
        val extensions = schema.mapOrNull("graphSchemaExtensionsRepresentation")
        val nodeKeys = keyProperties(extensions, "node")
        val relKeys = keyProperties(extensions, "relationship")
        // Support source-schema-only input via migrating just the tables. Nodes and relationships
        // are emitted as empty maps to match the full migration path of them always being present
        val graphSchema = schema.mapOrNull("graphSchemaRepresentation")?.mapOrNull("graphSchema")
            ?: return schemaMapOf(
                "version" to schema.literal("version"),
                "nodes" to emptyMap<String, SchemaMap>(),
                "relationships" to emptyMap<String, SchemaMap>(),
                "tables" toNotEmpty migrateTables(schema)
            )
        val (nodeConstraints, relationshipConstraints) = gatherWithNames(graphSchema, "constraints")
        val (nodeIndexes, relationshipIndexes) = gatherWithNames(graphSchema, "indexes")
        val nodes = migrateNodes(graphSchema, nodeConstraints, nodeIndexes)
        return schemaMapOf(
            "version" to schema.literal("version"),
            "nodes" to nodes,
            "relationships" to migrateRelationships(graphSchema, relationshipConstraints, relationshipIndexes),
            "tables" toNotEmpty migrateTables(schema),
            "mappings" toNotEmpty nodeMappings(schema, nodeKeys) + relationshipMappings(schema, relKeys),
            "display" toNotEmpty visualisation(schema, nodes)
        )
    }

    private fun keyProperties(extensions: SchemaMap?, entity: String): Map<String, Set<String>> =
        extensions?.listOfMapsOrNull("${entity}KeyProperties")?.associate { kp ->
            kp.ref(entity) to kp.listOfMaps("keyProperties").map { it.ref() }.toSet()
        } ?: emptyMap()

    internal fun visualisation(schema: SchemaMap, nodes: MutableMap<String, SchemaMap>): SchemaMap? {
        val visualisation = schema.remove("visualisation") as? SchemaMap ?: return null
        val display = mutableMapOf<String, SchemaMap>()
        for (vis in visualisation.listOfMaps("nodes")) {
            val ref = vis.string("id").removePrefix("#")
            nodes[ref] ?: error("Unknown node $ref")
            val position = vis.map("position")
            display[ref] = schemaMapOf(
                "x" to position.literal("x"),
                "y" to position.literal("y")
            )
        }
        return schemaMapOf("nodes" to display)
    }

    internal fun gatherWithNames(
        schema: SchemaMap,
        key: String
    ): Pair<Map<String, List<SchemaMap>>, Map<String, List<SchemaMap>>> {
        val elements = schema.listOfMapsOrNull(key) ?: return Pair(emptyMap(), emptyMap())
        val (nodes, rels) = elements.partition { it.string("entityType") == "node" }
        // It's assumed that all nodes have nodeLabel fields and rels have relationshipType's
        val nodeElements = nodes.groupBy { it.ref("nodeLabel") }
        val relationshipElements = rels.groupBy { it.ref("relationshipType") }
        for (element in elements) {
            element["name"] = element.stringOrNull("name")
        }
        return Pair(nodeElements, relationshipElements)
    }

    internal fun migrateNodes(
        schema: SchemaMap,
        constraints: Map<String, List<SchemaMap>>,
        indexes: Map<String, List<SchemaMap>>
    ): MutableMap<String, SchemaMap> {
        val nodes = mutableMapOf<String, SchemaMap>()
        val nodeLabels = schema.listOfMapsOrNull("nodeLabels")?.associateBy { it.id() } ?: return nodes
        val nodeObjTypes = schema.listOfMapsOrNull("nodeObjectTypes") ?: return nodes
        for (nodeObject in nodeObjTypes) {
            val labelRefs = nodeObject.listOfMaps("labels").map { it.ref() }
            val labels = labelRefs.map { labelRef ->
                nodeLabels[labelRef] ?: error("Label $labelRef not found")
            }
            val tokens = labels.map { it.string("token") }
            val labelRef = labelRefs.firstOrNull() // TODO loop all
            val primaryLabel = tokens.first()
            val id = nodeObject.id()
            nodes[id] = schemaMapOf(
                "labels" to schemaMapOf(
                    "identifier" to primaryLabel,
                    "implied" toNotEmpty tokens.drop(1)
                    // TODO optional
                ),
                "constraints" toNotEmpty convertConstraints(constraints, labelRef, primaryLabel, "node"),
                "indexes" toNotEmpty convertIndexes(indexes, labelRef, primaryLabel, "node"),
                "properties" toNotEmpty convertProperties(labels),
                "name" to tokens.firstOrNull()
            )
        }
        return nodes
    }

    internal fun convertIndexes(
        indexes: Map<String, List<SchemaMap>>,
        labelRef: String?,
        label: String,
        type: String
    ): Map<String, SchemaMap>? {
        var count = 0
        return indexes[labelRef]?.associate { index ->
            count++
            val id = index.id()
            id to schemaMapOf(
                "type" to indexType(index).name,
                "labels" to listOf(label),
                "properties" to index.listOfMapsOrNull("properties")?.map { it.ref() },
                "name" to (index.stringOrNull("name") ?: "${type}Index${count - 1}")
            )
        }
    }

    private fun indexType(index: SchemaMap): IndexType {
        val type = index.string("indexType")
        return indexType(type) ?: error("Unknown index type: '$type' at ${index.path}.${index.string("name")}")
    }

    internal fun convertConstraints(
        constraints: Map<String, List<SchemaMap>>,
        labelRef: String?,
        label: String,
        type: String
    ): Map<String, SchemaMap>? {
        var index = 0
        return constraints[labelRef]?.associate { constraint ->
            index++
            val properties = constraint.listOfMapsOrNull("properties")
            val constraintType = constraintType(constraint)
            if (properties != null && properties.size > 1 && constraintType == PROPERTY_TYPE) {
                error("Type constraints not supported on multiple properties.")
            }
            val id = constraint.id()
            id to schemaMapOf(
                "type" to constraintType.name,
                "label" to label,
                "properties" toNotEmpty properties?.map { it.ref() },
                "name" to (constraint.stringOrNull("name") ?: "${type}Constraint${index - 1}")
            )
        }
    }

    private fun constraintType(constraint: SchemaMap): ConstraintType {
        val type = constraint.string("constraintType")
        return constraintType(type)
            ?: error("Unknown constraint type: $type at ${constraint.path}.${constraint.string("name")}")
    }

    internal fun migrateRelationships(
        schema: SchemaMap,
        constraints: Map<String, List<SchemaMap>>,
        indexes: Map<String, List<SchemaMap>>
    ): MutableMap<String, SchemaMap> {
        val uniqueNames = mutableSetOf<String>()
        val relationships = mutableMapOf<String, SchemaMap>()
        val relationshipTypes =
            schema.listOfMapsOrNull("relationshipTypes")?.associateBy { it.id() } ?: return relationships
        val relObjTypes = schema.listOfMapsOrNull("relationshipObjectTypes") ?: return relationships
        for (objectType in relObjTypes) {
            val typeRef = objectType.ref("type")
            val relationshipType = relationshipTypes[typeRef] ?: error("RelationshipType $typeRef not found")
            val token = relationshipType.string("token")
            val id = objectType.id()
            relationships[id] = schemaMapOf(
                "type" to token,
                "from" to mapOf("node" to objectType.ref("from")),
                "to" to mapOf("node" to objectType.ref("to")),
                "properties" to convertProperties(listOf(relationshipType)),
                "constraints" toNotEmpty convertConstraints(constraints, typeRef, token, "relationship"),
                "indexes" toNotEmpty convertIndexes(indexes, typeRef, token, "relationship"),
                "name" to uniqueRelationshipName(token, uniqueNames)
            )
        }
        return relationships
    }

    private fun uniqueRelationshipName(token: String, names: MutableSet<String>): String {
        if (names.add(token)) {
            return token
        }
        for (i in 2 until Int.MAX_VALUE) {
            val name = "${token}$i"
            if (names.add(name)) {
                return name
            }
        }
        throw IllegalArgumentException("Unable to find unique relationship name for $token")
    }

    internal fun convertProperties(labels: List<SchemaMap>): Map<String, SchemaMap> = labels
        .flatMap { label -> label.listOfMaps("properties") }
        .associate { property ->
            val typeObj = property.map("type")
            val map = schemaMapOf(
                "name" to property.literalOrNull("token"),
                "type" to neo4jType(typeObj),
                "dimension" to dimension(typeObj)
            )
            property.id() to map
        }

    internal fun relationshipMappings(schema: SchemaMap, relKeys: Map<String, Set<String>>): List<SchemaMap> {
        val graph = schema.map("graphSchemaRepresentation").map("graphSchema")
        val relInfo = graph.listOfMapsOrNull("relationshipObjectTypes")?.associateBy { it.id() } ?: return emptyList()
        val relationshipMappings = schema
            .map("graphMappingRepresentation")
            .listOfMapsOrNull("relationshipMappings") ?: return emptyList()
        val mappings = mutableListOf<SchemaMap>()
        for (mapping in relationshipMappings) {
            val ref = mapping.ref("relationship")
            val obj = relInfo[ref] ?: error("Relationship $ref not found")
            mappings += schemaMapOf(
                "type" to SchemaLiteral(MappingType.RELATIONSHIP), // needed for Kotlin/Native migrations
                "relationship" to ref,
                "from" to mapOf(
                    "node" to obj.ref("from"),
                    "properties" to mapping.entityMap("fromMappings")
                ),
                "to" to mapOf(
                    "node" to obj.ref("to"),
                    "properties" to mapping.entityMap("toMappings")
                ),
                "table" to mapping.literal("tableName"),
                "properties" toNotEmpty migratePropertyMappings(mapping),
                "key" toNotEmpty migrateKeyProperties(mapping, relKeys[ref] ?: emptySet())
            )
        }
        return mappings
    }

    internal fun SchemaMap.entityMap(key: String) = mapOrNull(key)?.map { (key, value) ->
        key.removePrefix("#") to schemaMapOf("field" to value)
    }?.toMap() ?: emptyMap()

    internal fun nodeMappings(schema: SchemaMap, nodeKeys: Map<String, Set<String>>): List<SchemaMap> {
        val nodeMappings = schema
            .map("graphMappingRepresentation")
            .listOfMapsOrNull("nodeMappings") ?: return emptyList()
        val mappings = mutableListOf<SchemaMap>()
        for (nodeMapping in nodeMappings) {
            val ref = nodeMapping.ref("node")
            mappings += schemaMapOf(
                "type" to SchemaLiteral(MappingType.NODE), // needed for Kotlin/Native migrations
                "node" to ref,
                "table" to nodeMapping.literal("tableName"),
                "properties" toNotEmpty migratePropertyMappings(nodeMapping),
                "key" toNotEmpty migrateKeyProperties(nodeMapping, nodeKeys[ref] ?: emptySet())
            )
        }
        return mappings
    }

    private fun migratePropertyMappings(entity: SchemaMap) = entity
        .listOfMaps("propertyMappings")
        .associate { mapping ->
            mapping.ref("property") to mapOf(
                "field" to mapping.literal("fieldName")
            )
        }

    private fun migrateKeyProperties(entity: SchemaMap, keys: Set<String>) = entity
        .listOfMaps("propertyMappings")
        .mapNotNull { mapping ->
            val ref = mapping.ref("property")
            if (keys.contains(ref)) ref else null
        }

    internal fun migrateTables(schema: SchemaMap): MutableMap<String, SchemaMap> {
        val tables = mutableMapOf<String, SchemaMap>()
        val sourceSchema = schema.map("graphMappingRepresentation").mapOrNull("dataSourceSchema") ?: return tables
        for (table in sourceSchema.listOfMaps("tableSchemas")) {
            val name = table.string("name")
            tables[name] = schemaMapOf(
                "source" to sourceSchema.literalOrNull("type"),
                "fields" to migrateFields(table),
                "primaryKeys" toNotEmpty table.listOrNull("primaryKeys"),
                "foreignKeys" toNotEmpty migrateForeignKeys(table)
            )
        }
        return tables
    }

    private fun migrateForeignKeys(table: SchemaMap): MutableMap<String, SchemaElement>? {
        val foreign = table.listOfMapsOrNull("foreignKeys") ?: return null
        val foreignKeys = mutableMapOf<String, SchemaElement>()
        for (foreignKey in foreign) {
            val fields = foreignKey.listOfMaps("fields").map { it.string("field") }
            val referencedFields = foreignKey.listOfMaps("fields").map { it.string("referencedField") }
            foreignKeys["foreignKey${foreignKeys.size + 1}"] = schemaMapOf(
                "fields" to fields,
                "references" to mapOf(
                    "table" to foreignKey.literal("referencedTable"),
                    "fields" to referencedFields
                )
            )
        }
        return foreignKeys
    }

    private fun migrateFields(table: SchemaMap): MutableMap<String, SchemaElement> {
        val fields = mutableMapOf<String, SchemaElement>()
        for (field in table.listOfMaps("fields")) {
            val name = field.string("name")
            fields[name] = schemaMapOf(
                "name" to field.literal("name"),
                "type" to field.literalOrNull("rawType"),
                "size" to field.literalOrNull("size"),
                "suggested" to neo4jType(field.mapOrNull("recommendedType")),
                "supported" to field.listOfMapsOrNull("supportedTypes")?.map {
                    neo4jType(it)
                },
                "dimension" to (
                    dimension(field.mapOrNull("recommendedType"))
                        ?: field.listOfMapsOrNull("supportedTypes")?.firstNotNullOfOrNull { dimension(it) }
                    )
            )
        }
        return fields
    }

    companion object {
        private fun neo4jType(type: SchemaMap?): String? {
            val base = type?.stringOrNull("type")?.lowercase() ?: return null
            return when (base) {
                "array" -> itemType(type)?.let { "LIST<$it>" }
                "vector" -> itemType(type)?.let { "VECTOR<$it>" }
                else -> scalarType(base)
            }
        }

        private fun dimension(type: SchemaMap?): Int? =
            if (type?.stringOrNull("type")?.lowercase() == "vector") type.intOrNull("dimension") else null

        private fun itemType(type: SchemaMap): String? = scalarType(type.mapOrNull("items")?.stringOrNull("type"))

        private fun scalarType(string: String?): String? = when (val lower = string?.lowercase()) {
            null -> null
            // spaced and renamed types are not a simple uppercasing
            "localdatetime" -> "LOCAL DATETIME"
            "datetime" -> "ZONED DATETIME"
            "localtime" -> "LOCAL TIME"
            "time" -> "ZONED TIME"
            // everything else (string, integer, float, boolean, point, date, duration
            // and the vector coordinate variants like float32) is just its uppercase name
            else -> lower.uppercase()
        }

        private fun indexType(name: String): IndexType? = when (name) {
            "lookup" -> LOOKUP
            "default", "range" -> RANGE // TODO is default always range or is it dynamic based on type?
            "fulltext" -> FULLTEXT
            "point" -> POINT
            "text" -> TEXT
            "vector" -> VECTOR
            else -> null
        }

        private fun constraintType(name: String): ConstraintType? = when (name) {
            "uniqueness" -> UNIQUE
            "propertyExistence" -> EXISTS
            "propertyType" -> PROPERTY_TYPE
            "key" -> KEY
            else -> null
        }
    }
}
