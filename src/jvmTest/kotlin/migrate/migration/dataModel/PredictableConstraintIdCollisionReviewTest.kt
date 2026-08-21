package migrate.migration.dataModel

import GraphSpec
import codec.schema.SchemaMap
import model.Type
import model.Version
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Review test: predictable constraint ids (type + properties, no entity) collide in the
 * data model's flat constraints array when two entities share a same-named property
 * with the same constraint type.
 *
 * Input is the real pretty format a user writes or downloads: unique: true shorthand
 * on Person.email and Company.email. Internalise folds both into "unique_email"
 * constraints and the migration emits both into the flat array with that key as $id.
 *
 * graph-schema-utils consumers (e.g. upx) index the flat constraints array by $id alone,
 * so duplicate $ids silently misattribute or drop constraints downstream.
 *
 * Fails on the reviewed head. Goes green when predictableId includes the entity
 * (or the reverse migration scopes the emitted $id).
 */
class PredictableConstraintIdCollisionReviewTest {

    @Test
    fun `two entities with unique shorthand on same-named property get distinct constraint ids`() {
        // ARRANGE - the real user path: pretty yaml with shorthand on two entities
        val input = """
            version: "4.0.0"
            nodes:
              Person:
                label: "Person"
                properties:
                  email: { type: "STRING", unique: true }
              Company:
                label: "Company"
                properties:
                  email: { type: "STRING", unique: true }
        """.trimIndent()

        // ACT - full pipeline: parse pretty yaml, convert to data model v3
        val graphModel = GraphSpec.Yaml.decodeFromString(input)
        val graphSpec = GraphSpec { json { encodeDefaults = true } }
        val output = graphSpec.encodeToString(graphModel, Type.DATA_MODEL, Version.DATA_MODEL_V30)
        val result = graphSpec.configuration.format.decodeFromString(output) as SchemaMap

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
