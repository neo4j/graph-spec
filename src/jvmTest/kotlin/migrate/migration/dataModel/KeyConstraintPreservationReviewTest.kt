package migrate.migration.dataModel

import GraphSpec
import codec.schema.SchemaMap
import model.Type
import model.Version
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Review test: a KEY constraint in graph spec must survive conversion to the data model.
 * Users set key constraints independently of IDs (constraints UI, IMP-903); the data
 * model represents them fine (constraintType key). Skipping ALL KEY constraints on
 * conversion silently deletes user-declared schema rules.
 *
 * Skipping may be right for constraints that back a mapping key (the UI re-derives
 * those from the ID), but a KEY constraint backing nothing is user state, not derived state.
 *
 * Fails on the reviewed head (convertElements drops every KEY constraint).
 * Goes green when non-backing KEY constraints are preserved.
 */
class KeyConstraintPreservationReviewTest {

    @Test
    fun `user KEY constraint survives conversion to data model`() {
        // ARRANGE - pretty yaml with a long-form KEY constraint backing no mapping key
        val input = """
            version: "4.0.0"
            nodes:
              Customer:
                label: "Customer"
                properties:
                  customerId: { type: "STRING" }
                  email: { type: "STRING" }
                constraints:
                  emailKey:
                    type: "KEY"
                    label: "Customer"
                    properties: ["email"]
        """.trimIndent()

        // ACT - full pipeline: parse pretty yaml, convert to data model v3
        val graphModel = GraphSpec.Yaml.decodeFromString(input)
        val graphSpec = GraphSpec { json { encodeDefaults = true } }
        val output = graphSpec.encodeToString(graphModel, Type.DATA_MODEL, Version.DATA_MODEL_V30)
        val result = graphSpec.configuration.format.decodeFromString(output) as SchemaMap

        // ASSERT - the user's constraint is still there
        val constraints = result
            .map("graphSchemaRepresentation")
            .map("graphSchema")
            .listOfMaps("constraints")
        assertEquals(1, constraints.size, "a user-declared KEY constraint must not be silently dropped")
        assertEquals("key", constraints[0].string("constraintType"))
    }
}
