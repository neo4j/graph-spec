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
import model.property.IntegerType
import model.property.Property
import model.property.StringType
import model.property.neo4jTypeJs
import model.property.propertyJs
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
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
                        "p:n1:1" to propertyJs(neo4jTypeJs("LIST<STRING>"), name = "genres", id = "p:n1:1"),
                    ),
                    id = "n:1",
                )
            ),
            display = displayJs(recordOf("n:1" to nodeDisplayJs(0.0, 0.0)))
        )
        val model = GraphModelEditor.model(plainSpec)

        val encoded = GraphSpec.Json.encodeToString(model, "data_model", "3.0.0")

        val expected = """
            "type": {
                "type": "array",
                "items": {
                    "type": "string"
                }
            },
        """.trimIndent()
        assertTrue(encoded.replace(" ", "").contains(expected.replace(" ", "")))
    }

    @Test
    fun `encodeToString shouldn't drop fields`() {
        val graphSpec = GraphModel(
            version = "4.0.0",
            nodes = mutableMapOf(
                "PersonNode" to Node(
                    labels = Labels("Person"),
                    properties = mutableMapOf("born" to Property(IntegerType, mustExist = true)),
                    name = "Person",
                )
            ),
            pretty = true
        )
        graphSpec.internalise()
        val plain = GraphModelEditor.plain(graphSpec)
        val model = GraphModelEditor.model(plain)
        val encoded = GraphSpec.Json.encodeToString(model, Type.DATA_MODEL, Version.DATA_MODEL_V30)

        assertTrue(encoded.contains("\"token\": \"born\""))
        assertTrue(encoded.contains("\"nullable\": false"))

        val decoded = GraphSpec.Json.decodeFromString(encoded, Type.DATA_MODEL)
        assertEquals("Person", decoded.nodes["PersonNode"]?.name)
        assertEquals("born", decoded.nodes["PersonNode"]?.properties?.get("nodeProperty0")?.name)
    }

    @Test
    fun `encodeToString should be able to encode a graph spec that is using shorthand label syntax to data_model`() {
        val graphSpec = GraphModel(
            version = "4.0.0",
            nodes = mutableMapOf(
                "Person" to Node(
                    label = "Person",
                    properties = mutableMapOf("name" to Property(StringType)),
                )
            ),
            pretty = true
        )
        val encoded = GraphSpec.Json.encodeToString(graphSpec, Type.DATA_MODEL, Version.DATA_MODEL_V30)
        assertTrue(encoded.contains("\"token\": \"Person\""))
    }

    @Test
    fun `encodeToString shouldn't drop field names`() {
        val string = """
       {
          "version": "3.0.0",
          "dataModel": {
            "version": "3.0.0",
            "graphSchemaRepresentation": {
              "version": "1.0.0",
              "graphSchema": {
                "nodeLabels": [
                  {
                    "${'$'}id": "nl:1",
                    "token": "Test",
                    "properties": []
                  }
                ],
                "nodeObjectTypes": [
                  {
                    "${'$'}id": "n:1",
                    "labels": [
                      {
                        "${'$'}ref": "#nl:1"
                      }
                    ]
                  }
                ],
                "relationshipTypes": [],
                "relationshipObjectTypes": [],
                "indexes": [],
                "constraints": []
              }
            },
            "graphSchemaExtensionsRepresentation": {
              "nodeKeyProperties": [],
              "relationshipKeyProperties": []
            },
            "graphMappingRepresentation": {
              "dataSourceSchema": {
                "type": "local",
                "tableSchemas": [
                  {
                    "name": "categories.csv",
                    "expanded": true,
                    "fields": [
                      {
                        "name": "categoryID",
                        "sample": "1",
                        "recommendedType": {
                          "type": "integer"
                        }
                      }
                    ],
                    "primaryKeys": [],
                    "foreignKeys": []
                  }
                ]
              },
              "nodeMappings": [
              ],
              "relationshipMappings": []
            },
            "configurations": {
              "idsToIgnore": []
            }
          }
        }
        """.trimIndent()
        val dataModel = GraphSpec.Json.decodeFromString(string, Type.DATA_MODEL)
        val plain = GraphModelEditor.plain(dataModel)
        val filled = GraphModelEditor.model(plain)
        val encoded = GraphSpec.Json.encodeToString(filled, Type.DATA_MODEL, Version.DATA_MODEL_V30)
        val decoded = GraphSpec.Json.decodeFromString(encoded, Type.DATA_MODEL)
        assertNotNull(decoded)
    }

}

