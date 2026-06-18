import js.objects.recordOf
import model.GraphModel
import model.GraphModelEditor
import model.Type
import model.Version
import model.display.displayJs
import model.display.nodeDisplayJs
import model.graphModelJs
import model.node.Labels
import model.node.Node
import model.node.labelsJs
import model.node.nodeJs
import model.property.Neo4jType
import model.property.Property
import model.property.propertyJs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JsEdgeCaseTests {
    @Test
    fun `encodeToString should handle LIST<STRING>`() {
        val plainSpec = graphModelJs(
            version = "4.0.0",
            nodes = recordOf(
                "n:1" to nodeJs(
                    name = "Movie",
                    labels = labelsJs("Movie"),
                    properties = recordOf(
                        "p:n1:1" to propertyJs("LIST<STRING>", name = "genres", id = "p:n1:1"),
                    ),
                    id = "n:1",
                )
            ),
            display = displayJs(recordOf("n:1" to nodeDisplayJs(0.0, 0.0)))
        )
        val model = GraphModelEditor.model(plainSpec)

        val encoded = GraphSpec.Json.encodeToString(model, "data_model", "3.0.0")
        assertNotNull(encoded)
    }

    @Test
    fun `encodeToString shouldn't drop fields`() {
        val graphSpec = GraphModel(
            version = "4.0.0",
            nodes = mutableMapOf(
                "n:1" to Node(
                    labels = Labels("Person"),
                    properties = mutableMapOf("p:1" to Property(Neo4jType.INTEGER, name = "born")),
                    name = "Person",
                )
            )
        )
        val plain = GraphModelEditor.plain(graphSpec)
        val model = GraphModelEditor.model(plain)
        val encoded = GraphSpec.Json.encodeToString(model, Type.DATA_MODEL, Version.DATA_MODEL_V30)

        assertTrue(encoded.contains("\"token\": \"born\""))
        assertTrue(encoded.contains("\"nullable\": false"))

        val decoded = GraphSpec.Json.decodeFromString(encoded, Type.DATA_MODEL)
        assertEquals("Person", decoded.nodes["n:1"]?.name)
        assertEquals("born", decoded.nodes["n:1"]?.properties?.get("p:1")?.name)
    }
}

