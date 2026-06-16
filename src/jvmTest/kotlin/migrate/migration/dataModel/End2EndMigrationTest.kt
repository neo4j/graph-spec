package migrate.migration.dataModel

import GraphSpec
import model.Type
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import resourceAsString
import kotlin.test.assertEquals

class End2EndMigrationTest {

    @TestFactory
    fun `Round trip examples`() = listOf(
        "adventureworks-sales.json",
        "chinook.json",
        "dvd-rental.json",
        "flights.json",
        "ldbc.json",
        "northwind.json",
        "pandc.json"
    ).map { name ->
        dynamicTest(name.removeSuffix(".json")) {
            val input = End2EndMigrationTest::class.resourceAsString("prod-like/$name")
            val expected = End2EndMigrationTest::class.resourceAsString("prod-like/${name.replace(".json", ".yaml")}")

            val model = GraphSpec.Json.decodeFromString(input, Type.DATA_MODEL)

            model.prettify()
            val output = GraphSpec.Yaml.encodeToString(model)

            assertEquals(expected, output)
        }
    }

}
