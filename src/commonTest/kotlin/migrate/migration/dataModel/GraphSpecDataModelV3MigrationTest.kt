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
import codec.schema.schemaListOf
import codec.schema.schemaMapOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class GraphSpecDataModelV3MigrationTest {

    private val migration = GraphSpecDataModelV3Migration()

    @Test
    fun `migrate converts node labels and properties correctly`() {
        val input = schemaMapOf(
            "version" to "2.0",
            "nodes" to schemaMapOf(
                "node1" to schemaMapOf(
                    "labels" to schemaMapOf(
                        "identifier" to "Person",
                        "implied" to listOf("Entity")
                    ),
                    "properties" to schemaMapOf(
                        "p1" to schemaMapOf("name" to "name", "type" to "STRING", "mustExist" to true)
                    )
                )
            )
        )

        val result = migration.migrate(input)

        // Verify Node Labels
        val nodeLabels = result
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("nodeLabels")
        assertEquals(2, nodeLabels.size)
        val personLabel = nodeLabels.first { it.string("token") == "Person" }
        assertEquals("nl:0", personLabel.string("\$id"))

        // Verify Properties moved to Label
        val props = personLabel.listOfMaps("properties")
        assertEquals("p1", props[0].string("\$id"))
        assertEquals("string", props[0].map("type").string("type"))

        // Verify Object Type
        val nodeObjectTypes = result
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("nodeObjectTypes")
        assertEquals("node1", nodeObjectTypes[0].string("\$id"))
        val labelRefs = nodeObjectTypes[0].listOfMaps("labels")
        assertEquals("#nl:0", labelRefs[0].string("\$ref"))
    }

    @Test
    fun `migrate deduplicates properties when multiple nodes share the same primary label`() {
        val input = schemaMapOf(
            "nodes" to schemaMapOf(
                "n1" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "Person"),
                    "properties" to schemaMapOf("p1" to schemaMapOf("name" to "name", "type" to "STRING"))
                ),
                "n2" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "Person"),
                    "properties" to schemaMapOf("p1" to schemaMapOf("name" to "name", "type" to "STRING"))
                )
            )
        )

        val result = migration.migrate(input)
        val nodeLabels = result
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("nodeLabels")

        // The logic creates a new ID nl:0, nl:1 for every label found in the loop.
        // However, the test verifies if the properties list for a specific label ID remains unique.
        val personLabel = nodeLabels.first { it.string("token") == "Person" }
        val props = personLabel.listOfMaps("properties")
        assertEquals(1, props.size, "Property p1 should not be duplicated in the label definition")
    }

    @Test
    fun `migrate converts relationships and handles ObjectType references`() {
        val input = schemaMapOf(
            "nodes" to schemaMapOf("n1" to schemaMapOf("labels" to schemaMapOf("identifier" to "A"))),
            "relationships" to schemaMapOf(
                "rel1" to schemaMapOf(
                    "type" to "WORKS_AT",
                    "from" to schemaMapOf("node" to "n1"),
                    "to" to schemaMapOf("node" to "n2"),
                    "properties" to schemaMapOf(
                        "since" to schemaMapOf("name" to "since", "type" to "INTEGER")
                    )
                )
            )
        )

        val result = migration.migrate(input)
        val schema = result.map("graphSchemaRepresentation").map("graphSchema")

        // Verify Relationship Type
        val relTypes = schema.listOfMaps("relationshipTypes")
        assertEquals("rt:0", relTypes[0].string("\$id"))

        // Verify Relationship Object Type (the link between nodes)
        val relObjectTypes = schema.listOfMaps("relationshipObjectTypes")
        assertEquals("rel1", relObjectTypes[0].string("\$id"))
        assertEquals("#rt:0", relObjectTypes[0].map("type").string("\$ref"))
        assertEquals("#n1", relObjectTypes[0].map("from").string("\$ref"))
    }

    @Test
    fun `migrate handles relationship constraints and sets relationshipType ref`() {
        val input = schemaMapOf(
            "relationships" to schemaMapOf(
                "r:1" to schemaMapOf(
                    "type" to "LIVES_IN",
                    "from" to schemaMapOf("node" to "n1"),
                    "to" to schemaMapOf("node" to "n2"),
                    "constraints" to schemaMapOf(
                        "rel_uniq" to schemaMapOf("type" to "UNIQUE", "properties" to listOf("p1"))
                    )
                )
            )
        )

        val result = migration.migrate(input)
        val constraints = result.map("graphSchemaRepresentation")
            .map("graphSchema").listOfMaps("constraints")

        val relConstraint = constraints[0]
        assertEquals("rel_uniq", relConstraint.string("\$id"))
        assertEquals("relationship", relConstraint.string("entityType"))
        assertEquals("#rt:0", relConstraint.map("relationshipType").string("\$ref"))
        // Ensure nodeLabel is specifically SchemaNull
        assertTrue(relConstraint["nodeLabel"] is SchemaNull)
    }

    @Test
    fun `migrate handles constraints and indexes with type transformation`() {
        val input = schemaMapOf(
            "nodes" to schemaMapOf(
                "n1" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "User"),
                    "constraints" to schemaMapOf(
                        "uniq_id" to schemaMapOf("type" to "UNIQUE", "properties" to listOf("p1"))
                    ),
                    "indexes" to schemaMapOf(
                        "idx_name" to schemaMapOf("type" to "RANGE", "properties" to listOf("p2"))
                    )
                )
            )
        )

        val result = migration.migrate(input)
        val graphSchema = result.map("graphSchemaRepresentation").map("graphSchema")

        val constraint = graphSchema.listOfMaps("constraints")[0]
        assertEquals("uniqueness", constraint.string("constraintType"))
        assertEquals("#nl:0", constraint.map("nodeLabel").string("\$ref"))

        val index = graphSchema.listOfMaps("indexes")[0]
        assertEquals("range", index.string("indexType"))
    }

    @Test
    fun `convertGraphMapping correctly recovers relationship IDs via findRelationshipId`() {
        val input = schemaMapOf(
            "relationships" to schemaMapOf(
                "actual_rel_id" to schemaMapOf(
                    "type" to "FOLLOWS",
                    "from" to schemaMapOf("node" to "User"),
                    "to" to schemaMapOf("node" to "User")
                )
            ),
            "mappings" to listOf(
                schemaMapOf(
                    "relationship" to "actual_rel_id",
                    "table" to "user_follows",
                    "from" to
                        schemaMapOf(
                            "node" to "User",
                            "properties" to schemaMapOf("uid" to schemaMapOf("field" to "from_id"))
                        ),
                    "to" to
                        schemaMapOf(
                            "node" to "User",
                            "properties" to schemaMapOf("uid" to schemaMapOf("field" to "to_id"))
                        )
                )
            )
        )

        val result = migration.migrate(input)
        val relMappings = result
            .map("graphMappingRepresentation")
            .listOfMaps("relationshipMappings")

        assertEquals(1, relMappings.size)
        // Verify that findRelationshipId matched "FOLLOWS" + "User" -> "User" to "actual_rel_id"
        assertEquals("#actual_rel_id", relMappings[0].map("relationship").string("\$ref"))
        assertEquals("user_follows", relMappings[0].string("tableName"))
    }

    @Test
    fun `convertGraphMapping ignores relationship mappings that cannot be resolved`() {
        val input = schemaMapOf(
            "relationships" to schemaMapOf(), // Empty
            "mappings" to listOf(
                schemaMapOf(
                    "relationship" to "NON_EXISTENT",
                    "from" to schemaMapOf("node" to "A"),
                    "to" to schemaMapOf("node" to "B")
                )
            )
        )

        val result = migration.migrate(input)
        val relMappings = result
            .map("graphMappingRepresentation")
            .listOfMapsOrNull("relationshipMappings")
        assertEquals(emptyList(), relMappings)
    }

    @Test
    fun `convertExtensions identifies key properties from mappings`() {
        val input = schemaMapOf(
            "mappings" to schemaListOf(
                schemaMapOf(
                    "node" to "node1",
                    "table" to "table1",
                    "properties" to schemaMapOf(
                        "p1" to schemaMapOf("field" to "field1"),
                        "p2" to schemaMapOf("field" to "field2"),
                        "p3" to schemaMapOf("field" to "field3")
                    ),
                    "keys" to schemaListOf("p1")
                ),
                schemaMapOf(
                    "relationship" to "relationship1",
                    "table" to "table2",
                    "properties" to schemaMapOf(
                        "p1" to schemaMapOf("field" to "field1"),
                        "p2" to schemaMapOf("field" to "field2"),
                        "p3" to schemaMapOf("field" to "field3")
                    ),
                    "keys" to schemaListOf("p2", "p3")
                )
            )
        )

        val result = migration.convertExtensions(input)
        assertNotNull(result)

        val nodeKeyProps = result.listOfMaps("nodeKeyProperties")
        assertEquals(1, nodeKeyProps.size)
        assertEquals("#node1", nodeKeyProps[0].map("node").string("\$ref"))
        val nodeKeys = nodeKeyProps[0].listOfMaps("keyProperties")
        assertEquals(1, nodeKeys.size)
        assertEquals("#p1", nodeKeys[0].string("\$ref"))

        val relationshipKeyProps = result.listOfMaps("relationshipKeyProperties")
        assertEquals(1, relationshipKeyProps.size)
        assertEquals("#relationship1", relationshipKeyProps[0].map("relationship").string("\$ref"))
        val relKeys = relationshipKeyProps[0].listOfMaps("keyProperties")
        assertEquals(2, relKeys.size)
        assertEquals("#p2", relKeys[0].string("\$ref"))
    }

    @Test
    fun `convertSourceSchema handles complex foreign keys correctly`() {
        val input = schemaMapOf(
            "tables" to schemaMapOf(
                "Orders" to schemaMapOf(
                    "source" to "SQL",
                    "foreignKeys" to schemaMapOf(
                        "fk_customer" to schemaMapOf(
                            "fields" to listOf("cust_id", "region_id"),
                            "references" to schemaMapOf(
                                "table" to "Customers",
                                "fields" to listOf("id", "reg_id")
                            )
                        )
                    )
                )
            )
        )

        val result = migration.migrate(input)
        val tableSchemas = result
            .map("graphMappingRepresentation")
            .map("dataSourceSchema")
            .listOfMaps("tableSchemas")

        val fk = tableSchemas[0].listOfMaps("foreignKeys")[0]
        assertEquals("Customers", fk.string("referencedTable"))

        val fields = fk.listOfMaps("fields")
        assertEquals(2, fields.size)
        assertEquals("cust_id", fields[0].string("field"))
        assertEquals("id", fields[0].string("referencedField"))
        assertEquals("region_id", fields[1].string("field"))
        assertEquals("reg_id", fields[1].string("referencedField"))
    }

    @Test
    fun `convertFields transforms raw types to camelCase recommended types`() {
        val fieldsInput = mapOf(
            "f1" to schemaMapOf(
                "name" to "first_name",
                "suggested" to "STRING",
                "supported" to listOf(SchemaLiteral("STRING"), SchemaLiteral("INTEGER"))
            )
        )

        val fields = migration.convertFields(fieldsInput)

        assertEquals("first_name", fields[0].string("name"))
        assertEquals("string", fields[0].map("recommendedType").string("type"))
        val supported = fields[0].listOfMaps("supportedTypes")
        assertEquals("string", supported[0].string("type"))
        assertEquals("integer", supported[1].string("type"))
    }

    @Test
    fun `convertVisualisation transforms coordinates correctly`() {
        val display = schemaMapOf(
            "display" to schemaMapOf(
                "nodes" to schemaMapOf(
                    "node1" to schemaMapOf("x" to 100.23, "y" to 200.12)
                )
            )
        )

        val result = migration.convertVisualisation(display)

        assertNotNull(result)
        val nodes = result.listOfMaps("nodes")
        assertEquals("node1", nodes[0].string("id"))
        assertEquals(100.23, nodes[0].map("position").string("x").toDouble())
        assertEquals(200.12, nodes[0].map("position").string("y").toDouble())
    }

    @Test
    fun `convertExtensions emits nodeKeyProperties as empty list when no node has key properties`() {
        val input = schemaMapOf(
            "nodes" to schemaMapOf(
                "n1" to schemaMapOf(
                    "properties" to schemaMapOf(
                        "p1" to schemaMapOf("mustExist" to false, "unique" to true), // mustExist — not a key
                        "p2" to schemaMapOf("mustExist" to true, "unique" to false) // not unique — not a key
                    )
                )
            ),
            "relationships" to schemaMapOf(
                "r1" to schemaMapOf(
                    "properties" to schemaMapOf(
                        "p1" to schemaMapOf("nullable" to true, "unique" to true), // nullable — not a key
                        "p2" to schemaMapOf("nullable" to false, "unique" to false) // not unique — not a key
                    )
                )
            )
        )

        val result = migration.convertExtensions(input)

        assertNotNull(result)
        assertTrue(result.containsKey("nodeKeyProperties"), "nodeKeyProperties key must always be present")
        assertEquals(emptyList(), result.listOfMapsOrNull("nodeKeyProperties"))
        assertNull(result.listOfMapsOrNull("relationshipKeyProperties"))
    }

    @Test
    fun `migrate graphMappingRepresentation is complete when tables key is absent`() {
        val input = schemaMapOf(
            "nodes" to schemaMapOf(
                "n1" to schemaMapOf("labels" to schemaMapOf("identifier" to "Person"))
            )
        )

        val result = migration.migrate(input)
        val mapping = result.map("graphMappingRepresentation")

        assertTrue(mapping.containsKey("dataSourceSchema"), "dataSourceSchema must always be present")
        val dataSourceSchema = mapping.map("dataSourceSchema")
        assertTrue(dataSourceSchema["type"] is SchemaNull, "type should be null when no tables")
        assertEquals(emptyList(), dataSourceSchema.listOfMapsOrNull("tableSchemas"))

        assertTrue(mapping.containsKey("nodeMappings"), "nodeMappings must always be present")
        assertEquals(emptyList(), mapping.listOfMapsOrNull("nodeMappings"))

        assertTrue(mapping.containsKey("relationshipMappings"), "relationshipMappings must always be present")
        assertEquals(emptyList(), mapping.listOfMapsOrNull("relationshipMappings"))
    }

    @Test
    fun `migrate graphMappingRepresentation is complete when tables key is present but empty`() {
        val input = schemaMapOf(
            "nodes" to schemaMapOf(
                "n1" to schemaMapOf("labels" to schemaMapOf("identifier" to "Person"))
            ),
            "tables" to schemaMapOf() // key present, but no table entries
        )

        val result = migration.migrate(input)
        val mapping = result.map("graphMappingRepresentation")

        assertTrue(mapping.containsKey("dataSourceSchema"), "dataSourceSchema must be present even for empty tables")
        val dataSourceSchema = mapping.map("dataSourceSchema")
        assertTrue(dataSourceSchema.containsKey("tableSchemas"), "tableSchemas must be present even when empty")
        assertEquals(emptyList(), dataSourceSchema.listOfMapsOrNull("tableSchemas"))

        assertEquals(emptyList(), mapping.listOfMapsOrNull("nodeMappings"))
        assertEquals(emptyList(), mapping.listOfMapsOrNull("relationshipMappings"))
    }

    @Test
    fun `migrate always includes configurations with idsToIgnore`() {
        val input = schemaMapOf(
            "nodes" to schemaMapOf(
                "n1" to schemaMapOf("labels" to schemaMapOf("identifier" to "Person"))
            )
        )

        val result = migration.migrate(input)
        val dataModel = result

        assertTrue(dataModel.containsKey("configurations"), "configurations must always be present")
        val configurations = dataModel.map("configurations")
        assertTrue(configurations.containsKey("idsToIgnore"), "idsToIgnore must always be present")
        assertEquals(0, configurations.listOrNull("idsToIgnore")?.content?.size)
    }

    @Test
    fun `migrate emits expanded true for each converted table schema`() {
        val input = schemaMapOf(
            "tables" to schemaMapOf(
                "users" to schemaMapOf(
                    "source" to "local",
                    "fields" to schemaMapOf(
                        "id" to schemaMapOf("name" to "id", "type" to "INTEGER")
                    )
                )
            )
        )

        val result = migration.migrate(input)
        val tableSchemas = result
            .map("graphMappingRepresentation")
            .map("dataSourceSchema")
            .listOfMaps("tableSchemas")

        assertEquals(1, tableSchemas.size)
        assertTrue(tableSchemas[0].containsKey("expanded"), "expanded must be present on each table schema")
        assertTrue(tableSchemas[0].bool("expanded"), "expanded should default to true")
    }

    @Test
    fun `migrate handles completely empty schema without crashing`() {
        val emptyInput = schemaMapOf("version" to "2.0")

        val result = migration.migrate(emptyInput)

        val graphSchema = result
            .map("graphSchemaRepresentation")
            .map("graphSchema")

        assertEquals(0, graphSchema.listOfMaps("nodeLabels").size)
        assertEquals(0, graphSchema.listOfMaps("relationshipTypes").size)
        assertEquals(0, graphSchema.listOfMaps("nodeObjectTypes").size)
        assertEquals(0, graphSchema.listOfMaps("relationshipObjectTypes").size)
        assertEquals(0, graphSchema.listOfMaps("constraints").size)
        assertEquals(0, graphSchema.listOfMaps("indexes").size)
    }

    @Test
    fun `migrate emits the flat data model by default and the wrapped model file when wrapped`() {
        val input = schemaMapOf(
            "nodes" to schemaMapOf(
                "n1" to schemaMapOf("labels" to schemaMapOf("identifier" to "Person"))
            ),
            "display" to schemaMapOf(
                "nodes" to schemaMapOf(
                    "node1" to schemaMapOf("x" to 100.23, "y" to 200.12)
                )
            )
        )

        // Default DATA_MODEL is flat with no visualisation
        val flat = migration.migrate(input)
        assertFalse(flat.containsKey("dataModel"), "flat output must not be wrapped")
        assertTrue(flat.containsKey("graphSchemaRepresentation"))
        assertTrue(flat.containsKey("graphMappingRepresentation"))
        assertFalse(flat.containsKey("visualisation"))

        // DATA_MODEL_WRAPPED wraps the same data model under a top-level dataModel key and includes visualisation
        val wrapped = GraphSpecDataModelV3Migration(wrapped = true).migrate(input)
        assertFalse(wrapped.containsKey("graphSchemaRepresentation"))
        assertTrue(wrapped.containsKey("visualisation"))
        val dataModel = wrapped.map("dataModel")
        assertTrue(dataModel.containsKey("graphSchemaRepresentation"))
        assertTrue(dataModel.containsKey("graphMappingRepresentation"))
        assertTrue(dataModel.containsKey("configurations"))
    }

    @Test
    fun `long-form constraint id and name are preserved through a round-trip`() {
        // ARRANGE - Graph Spec with a long-form UNIQUE constraint (custom id and name)
        val input = schemaMapOf(
            "version" to "2.0",
            "nodes" to schemaMapOf(
                "user" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "User"),
                    "properties" to schemaMapOf(
                        "email" to schemaMapOf("name" to "email", "type" to "STRING")
                    ),
                    "constraints" to schemaMapOf(
                        "emailUnique" to schemaMapOf(
                            "type" to "UNIQUE",
                            "label" to "User",
                            "properties" to listOf("email"),
                            "name" to "email_unique_constraint"
                        )
                    )
                )
            )
        )

        // ACT - Round-trip: Graph Spec → Data Model → Graph Spec
        val dataModel = migration.migrate(input)
        val result = DataModelV3GraphSpecMigration().migrate(dataModel)

        // ASSERT - Custom constraint ID and name are preserved
        val userNode = result.map("nodes").map("user")
        val constraints = userNode.mapOrNull("constraints")
        assertNotNull(constraints, "Constraints should be preserved")
        val emailConstraint = constraints.mapOrNull("emailUnique")
        assertNotNull(emailConstraint, "Custom constraint ID 'emailUnique' should be preserved")
        assertEquals("email_unique_constraint", emailConstraint.string("name"))
        assertEquals("UNIQUE", emailConstraint.string("type"))
    }

    @Test
    fun `shorthand input is still parsed into a Data Model constraint`() {
        // ARRANGE - Graph Spec input using property shorthand (public format must keep working)
        val input = schemaMapOf(
            "version" to "2.0",
            "nodes" to schemaMapOf(
                "user" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "User"),
                    "properties" to schemaMapOf(
                        "email" to schemaMapOf("name" to "email", "type" to "STRING", "unique" to true)
                    )
                )
            )
        )

        // ACT - GS -> DM
        val dataModel = migration.migrate(input)

        // ASSERT - the shorthand produced a uniqueness constraint on email in the Data Model
        val constraints = dataModel
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMapsOrNull("constraints")
        val hasUniqueness = constraints?.any { c ->
            c.stringOrNull("constraintType") == "uniqueness" &&
                c.listOrNull("properties")?.any { (it as? SchemaMap)?.ref() == "email" } == true
        } == true
        assertEquals(true, hasUniqueness, "shorthand input must still create a Data Model constraint")
    }

    @Test
    fun `shorthand constraint input is emitted as long-form through a round-trip`() {
        // ARRANGE - Graph Spec expressing the constraint as property shorthand (unique: true)
        val input = schemaMapOf(
            "version" to "2.0",
            "nodes" to schemaMapOf(
                "user" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "User"),
                    "properties" to schemaMapOf(
                        "email" to schemaMapOf("name" to "email", "type" to "STRING", "unique" to true)
                    )
                )
            )
        )

        // ACT - Round-trip: Graph Spec -> Data Model -> Graph Spec
        val dataModel = migration.migrate(input)
        val result = DataModelV3GraphSpecMigration().migrate(dataModel)
        val userNode = result.map("nodes").map("user")

        // ASSERT - long-form only (no shorthand), one UNIQUE constraint on email, UPX-style name
        assertNull(userNode.map("properties").map("email").boolOrNull("unique"), "shorthand must not be emitted")
        val constraint = userNode.mapOfMapsOrNull("constraints")?.values?.single { c ->
            c.stringOrNull("type") == "UNIQUE" &&
                c.listOrNull("properties")?.any { (it as? SchemaLiteral)?.string == "email" } == true
        }
        assertNotNull(constraint, "constraint must be emitted as long-form")
        assertEquals("email_User_uniq", constraint.string("name"), "name must follow UPX auto-naming")
    }

    @Test
    fun `migrate preserves uniqueness constraints on same-named property across different node labels`() {
        val input = schemaMapOf(
            "version" to "2.0",
            "nodes" to schemaMapOf(
                "user" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "User"),
                    "properties" to schemaMapOf(
                        "email" to schemaMapOf("name" to "email", "type" to "STRING", "unique" to true)
                    )
                ),
                "company" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "Company"),
                    "properties" to schemaMapOf(
                        "email" to schemaMapOf("name" to "email", "type" to "STRING", "unique" to true)
                    )
                )
            )
        )

        val result = migration.migrate(input)
        val constraints = result
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("constraints")
            .filter { it.stringOrNull("constraintType") == "uniqueness" }

        assertEquals(2, constraints.size, "expected uniqueness constraints for both User and Company")
        val nodeLabelRefs = constraints.mapNotNull { it.mapOrNull("nodeLabel")?.ref() }
        assertEquals(2, nodeLabelRefs.toSet().size, "constraints must belong to distinct node labels: $nodeLabelRefs")
    }

    @Test
    fun `shorthand constraints on the same property get unique ids`() {
        // ARRANGE - one property that is both mandatory and unique produces TWO constraints
        // (a property existence constraint and a uniqueness constraint)
        val input = schemaMapOf(
            "version" to "2.0",
            "nodes" to schemaMapOf(
                "user" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "User"),
                    "properties" to schemaMapOf(
                        "email" to schemaMapOf(
                            "name" to "email",
                            "type" to "STRING",
                            "mustExist" to true,
                            "unique" to true
                        )
                    )
                )
            )
        )

        // ACT - GS -> DM
        val dataModel = migration.migrate(input)
        val emailConstraints = dataModel
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("constraints")
            .filter { c -> c.listOfMapsOrNull("properties")?.any { it.ref() == "email" } == true }

        // ASSERT - both constraints exist, and each has a UNIQUE \$id
        assertEquals(2, emailConstraints.size, "expected an existence and a uniqueness constraint")
        val ids = emailConstraints.map { it.string("\$id") }
        assertEquals(
            ids.size,
            ids.toSet().size,
            "constraint \$ids must be unique, but they collide: $ids"
        )
    }
}
