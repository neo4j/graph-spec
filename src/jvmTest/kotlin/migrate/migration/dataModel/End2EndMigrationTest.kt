package migrate.migration.dataModel

import GraphSpec
import codec.format.YamlFormat
import model.Type
import resourceAsString
import java.io.File
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals

class End2EndMigrationTest {
    @Test
    fun `Round trip prod-like examples`() {
        val list = listOf(
//            "adventureworks-sales.json",
//            "chinook.json",
//            "dvd-rental.json",
            "flights.json",
//            "ldbc.json",
//            "northwind.json",
//            "pandc.json"
        )
        for (name in list) {
            val input = End2EndMigrationTest::class.resourceAsString("prod-like/$name")
            val expected = End2EndMigrationTest::class.resourceAsString("prod-like/${name.replace(".json", ".yaml")}")

            val model = GraphSpec.Json.decodeFromString(input, Type.DATA_MODEL)

            model.prettify()
            val output = GraphSpec.Yaml.encodeToString(model)

//            assertEquals(expected, output)
            File("./${name.replace(".json", ".yaml")}").writeText(output)
        }
    }

}
