package org.neo4j.spec.graph.migrate.migration.dataModel

import org.neo4j.spec.graph.GraphSpec
import kotlinx.serialization.json.Json
import org.neo4j.spec.graph.model.index.IndexOption
import org.neo4j.spec.graph.model.index.VectorIndexOption
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals

class InternalDataModelIT {

    @TestFactory
    fun `Longhand model round-trip`() = listOf(
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
            val initial = File(End2EndMigrationTest::class.java.getResource("prod-like/$name")!!.path).readText()
            val model = GraphSpec.Yaml.decodeFromString(initial)

            val output = GraphSpec.Yaml.encodeToString(model)

            assertEquals(initial, output)
        }
    }

}
