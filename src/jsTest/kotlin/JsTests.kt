import js.objects.recordOf
import model.GraphModelEditor
import model.display.displayJs
import model.display.nodeDisplayJs
import model.graphModelJs
import model.node.labelsJs
import model.node.nodeJs
import model.property.propertyJs
import kotlin.test.Test
import kotlin.test.assertNotNull

class JsTests {
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
}
