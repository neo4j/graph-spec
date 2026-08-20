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

import codec.schema.schemaListOf
import codec.schema.schemaMapOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Review test: predictable constraint ids (type + properties, no entity) collide in the
 * data model's flat constraints array when two entities share a same-named property
 * with the same constraint type - the shape internalise produces for pretty shorthand.
 *
 * graph-schema-utils consumers (e.g. upx) index the flat constraints array by $id alone,
 * so duplicate $ids silently misattribute or drop constraints.
 *
 * Fails on the reviewed head. Goes green when predictableId includes the entity
 * (or the reverse migration scopes the emitted $id).
 */
class PredictableConstraintIdCollisionReviewTest {

    private val migration = GraphSpecDataModelV3Migration()

    @Test
    fun `two entities with same shorthand on same-named property get distinct constraint ids`() {
        // ARRANGE - internal format as Internal.internalise produces it:
        // Person.email unique:true and Company.email unique:true both became "unique_email"
        val input = schemaMapOf(
            "version" to "2.0",
            "nodes" to schemaMapOf(
                "person" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "Person"),
                    "properties" to schemaMapOf(
                        "email" to schemaMapOf("type" to "STRING")
                    ),
                    "constraints" to schemaMapOf(
                        "unique_email" to schemaMapOf(
                            "type" to "UNIQUE",
                            "properties" to schemaListOf("email")
                        )
                    )
                ),
                "company" to schemaMapOf(
                    "labels" to schemaMapOf("identifier" to "Company"),
                    "properties" to schemaMapOf(
                        "email" to schemaMapOf("type" to "STRING")
                    ),
                    "constraints" to schemaMapOf(
                        "unique_email" to schemaMapOf(
                            "type" to "UNIQUE",
                            "properties" to schemaListOf("email")
                        )
                    )
                )
            )
        )

        // ACT
        val result = migration.migrate(input)

        // ASSERT - both constraints survive in the flat array, with distinct $ids
        val constraints = result
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("constraints")
        assertEquals(2, constraints.size, "both entities' constraints must survive the conversion")
        val ids = constraints.map { it.string("\$id") }
        assertEquals(
            ids.size,
            ids.distinct().size,
            "constraint \$ids must be unique in the flat graphSchema.constraints array, got $ids"
        )
    }
}
