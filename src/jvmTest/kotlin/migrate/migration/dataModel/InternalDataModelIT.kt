package migrate.migration.dataModel

import GraphSpec
import kotlinx.serialization.json.Json
import model.index.IndexOption
import model.index.VectorIndexOption
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class InternalDataModelIT {

    @TestFactory
    fun `Internal model round-trip`() = listOf(
        "adventureworks-sales.yaml",
        "chinook.yaml",
        "dvd-rental.yaml",
        "flights.yaml",
        "ldbc.yaml",
        "northwind.yaml",
        "pandc.yaml",
        "vector-embeddings.yaml"
    ).map { name ->
        dynamicTest(name.removeSuffix(".yaml")) {
            var initial = File(End2EndMigrationTest::class.java.getResource("prod-like/$name")!!.path).readText()
            val expected = File(End2EndMigrationTest::class.java.getResource("internal/$name")!!.path).readText()
            val model = GraphSpec.Yaml.decodeFromString(initial)

            model.internalise()
            val internal = GraphSpec.Yaml.encodeToString(model)

            assertEquals(expected, internal)

            model.prettify()
            val pretty = GraphSpec.Yaml.encodeToString(model)
            // FIXME our YAML library doesn't correctly handle negative doubles, isn't maintained and there's no alternative.
            initial = initial.replace("\"-100.123\"", "-100.123")
            assertEquals(initial, pretty)
        }
    }

}
