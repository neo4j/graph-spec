package migrate.migration.dataModel

import GraphSpec
import model.Type
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import resourceAsString
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class End2EndMigrationTest {

    private val fixtures = listOf(
        "adventureworks-sales.json",
        "chinook.json",
        "dvd-rental.json",
        "flights.json",
        "ldbc.json",
        "northwind.json",
        "pandc.json",
        "vector-embeddings.json"
    )

    @TestFactory
    fun `round trip examples`() = fixtures.map { name ->
        dynamicTest(name.removeSuffix(".json")) {
            val input = End2EndMigrationTest::class.resourceAsString("prod-like/$name")
            val expected = End2EndMigrationTest::class.resourceAsString("prod-like/${name.replace(".json", ".yaml")}")

            val model = GraphSpec.Json.decodeFromString(input, Type.DATA_MODEL)

            model.prettify()
            val output = GraphSpec.Yaml.encodeToString(model)

            assertEquals(expected, output)
        }
    }


    @TestFactory
    fun `graph-spec round trip examples`() = fixtures.map { name ->
        dynamicTest(name.removeSuffix(".json")) {
            val input = End2EndMigrationTest::class.resourceAsString("prod-like/$name")
            val json = GraphSpec.Json.encodeToString(GraphSpec.Json.decodeFromString(input, Type.DATA_MODEL))

            val model = GraphSpec.Json.decodeFromString(json, Type.GRAPH_SPEC)
            val output = GraphSpec.Json.encodeToString(model)

            assertEquals(json, output)
        }
    }

    @TestFactory
    fun `decoding a graph-spec model with an unsupported version fails`() = mapOf(
        GraphSpec.Json to """{"version":"3.0.0"}""",
        GraphSpec.Yaml to """version: "3.0.0"""",
    ).map { (graphSpec, input) ->
        dynamicTest(graphSpec.configuration.format.javaClass.toString()) {
            val failure = assertFailsWith<IllegalStateException> {
                graphSpec.decodeFromString(input, Type.GRAPH_SPEC)
            }

            assertTrue(
                failure.message!!.contains("Unsupported migration from graph_spec:3.0.0 to graph_spec:4.0.0"),
                "unexpected message: ${failure.message}"
            )
        }
    }

}
