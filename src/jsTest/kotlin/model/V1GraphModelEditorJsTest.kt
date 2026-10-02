/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package model

import js.objects.recordOf
import model.extension.ExtensionType
import model.extension.ExtensionValueJs
import model.extension.ExtensionsEditor
import model.extension.StringValueJs
import model.extension.stringValueJs
import model.node.NodeConstraintEditor
import model.node.NodeConstraintJs
import model.node.NodeEditor
import model.relationship.RelationshipConstraintEditor
import model.relationship.RelationshipConstraintJs
import model.relationship.RelationshipEditor
import model.relationship.RelationshipTargetEditor
import model.tool.toolJs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Real coverage of the v1 JS surface (ADR-0004 track 4): a v1 model built through the
 * editors (id-keyed nodes/relationships, String type tokens, endpoint cardinality,
 * the unified constraint aliases, tools, extensions), read back through the *Js twins
 * and round-tripped toJs/toClass with data-class equality.
 */
class V1GraphModelEditorJsTest {

    private fun buildV1ModelJs(): GraphModelJs {
        val model = graphModelJs(
            schema = "https://neo4j.com/ontology-spec/1.0.0/schema.json",
            id = "editor-built",
            version = 1,
            name = "editor-built-model",
        )

        // Id-keyed node records created by the editor
        val personId = GraphModelEditor.addNode(model, "Person")
        val movieId = GraphModelEditor.addNode(model, "Movie")
        NodeEditor.setDescription(model, movieId, "A movie.")

        // Properties with v1 String type tokens
        val nameProp = NodeEditor.addProperty(model, personId)
        NodeEditor.setPropertyType(model, personId, nameProp, "STRING")
        NodeEditor.setPropertyMustExist(model, personId, nameProp, true)

        val tagsProp = NodeEditor.addProperty(model, personId)
        NodeEditor.setPropertyType(model, personId, tagsProp, "LIST<STRING>")

        val embeddingProp = NodeEditor.addProperty(model, personId)
        NodeEditor.setPropertyType(model, personId, embeddingProp, "VECTOR<FLOAT>")
        NodeEditor.setPropertyDimension(model, personId, embeddingProp, 1536)

        // Node constraint via the NodeConstraintJs typealias over the unified ConstraintJs
        val nodeConstraintIndex = NodeEditor.addConstraint(
            model = model,
            nodeId = personId,
            type = "unique",
            name = "person_name_unique",
            properties = arrayOf(nameProp),
        )
        NodeEditor.addConstraintProperty(model, personId, nodeConstraintIndex, tagsProp)
        NodeEditor.removeConstraintProperty(model, personId, nodeConstraintIndex, tagsProp)
        val nodeConstraint: NodeConstraintJs = model.nodes[personId]!!.constraints[nodeConstraintIndex]
        NodeConstraintEditor.setName(nodeConstraint, "person_name_unique_v2")

        // A canonicalQuery tool with a cypher extra, via the ToolJs shape. `tools` is a
        // read-only array on the twins and there is no ToolEditor, so attach in place.
        val person = model.nodes[personId]!!
        person.tools.asDynamic().push(
            toolJs(
                type = "canonicalQuery",
                name = "findPeople",
                description = "All people",
                extra = recordOf<String, ExtensionValueJs>(
                    "cypher" to stringValueJs("MATCH (p:Person) RETURN p"),
                ),
            ),
        )

        // Id-keyed relationship record with endpoint cardinality
        val relId = GraphModelEditor.addRelationship(model, "ACTED_IN")
        val rel = model.relationships[relId]!!
        RelationshipEditor.setSourceNode(rel, personId)
        RelationshipEditor.setTargetNode(rel, movieId)
        RelationshipTargetEditor.setCount(rel.from, 1)
        RelationshipTargetEditor.setMinCount(rel.to, 1)
        RelationshipTargetEditor.setMaxCount(rel.to, 5)

        val rolesProp = RelationshipEditor.addProperty(model, relId)
        RelationshipEditor.setPropertyType(model, relId, rolesProp, "LIST<STRING>")

        // Relationship constraint via the RelationshipConstraintJs typealias over the
        // same unified ConstraintJs implementation
        val relConstraintIndex = RelationshipEditor.addConstraint(
            model = model,
            relationshipId = relId,
            type = "unique",
            name = "acted_in_roles_unique",
            properties = arrayOf(rolesProp),
        )
        val relConstraint: RelationshipConstraintJs = model.relationships[relId]!!.constraints[relConstraintIndex]
        RelationshipConstraintEditor.setName(relConstraint, "acted_in_roles_unique_v2")
        RelationshipConstraintEditor.addProperty(relConstraint, "property1")
        RelationshipConstraintEditor.removeProperty(relConstraint, "property1")

        // A model-level extension so the round trip covers the extensions map too
        ExtensionsEditor.set(model.extensions, "acme:owner", "data-platform")

        return model
    }

    @Test
    fun testBuildV1ModelThroughEditors() {
        val model = buildV1ModelJs()

        // Id-keyed records: the editor returns the key, the entry carries it as id
        val personId = "node0"
        val movieId = "node1"
        val person = model.nodes[personId]
        val movie = model.nodes[movieId]
        assertNotNull(person)
        assertNotNull(movie)
        assertEquals(personId, person.id)
        assertEquals(movieId, movie.id)
        assertEquals("Person", person.label)
        assertEquals("Movie", movie.label)
        assertEquals("A movie.", movie.description)

        // String type tokens, incl. LIST<STRING> and VECTOR<FLOAT> + dimension
        val name = person.properties["property0"]
        val tags = person.properties["property1"]
        val embedding = person.properties["property2"]
        assertNotNull(name)
        assertNotNull(tags)
        assertNotNull(embedding)
        assertEquals("STRING", name.type)
        assertEquals(true, name.mustExist)
        assertEquals("LIST<STRING>", tags.type)
        assertEquals("VECTOR<FLOAT>", embedding.type)
        assertEquals(1536, embedding.dimension)

        // The node constraint through the NodeConstraintJs alias
        assertEquals(1, person.constraints.size)
        val nodeConstraint: NodeConstraintJs = person.constraints[0]
        assertEquals("unique", nodeConstraint.type)
        assertEquals("person_name_unique_v2", nodeConstraint.name)
        assertEquals(listOf("property0"), nodeConstraint.properties.toList())

        // The canonicalQuery tool with its cypher extra
        assertEquals(1, person.tools.size)
        val tool = person.tools[0]
        assertEquals("canonicalQuery", tool.type)
        assertEquals("findPeople", tool.name)
        assertEquals("All people", tool.description)
        val cypher = tool.extra["cypher"]
        assertEquals(ExtensionType.STRING, cypher?.type)
        assertEquals("MATCH (p:Person) RETURN p", (cypher as StringValueJs).value)

        // Relationship: id-keyed, type, endpoints with count/min_count/max_count
        val relId = "relationship0"
        val rel = model.relationships[relId]
        assertNotNull(rel)
        assertEquals(relId, rel.id)
        assertEquals("ACTED_IN", rel.type)
        assertEquals(personId, rel.from.node)
        assertEquals(1, rel.from.count)
        assertEquals(movieId, rel.to.node)
        assertEquals(1, rel.to.minCount)
        assertEquals(5, rel.to.maxCount)

        // Relationship property and constraint (RelationshipConstraintJs alias)
        val roles = rel.properties["property0"]
        assertNotNull(roles)
        assertEquals("LIST<STRING>", roles.type)
        assertEquals(1, rel.constraints.size)
        val relConstraint: RelationshipConstraintJs = rel.constraints[0]
        assertEquals("unique", relConstraint.type)
        assertEquals("acted_in_roles_unique_v2", relConstraint.name)
        assertEquals(listOf("property0"), relConstraint.properties.toList())

        // Model-level extension
        val owner = model.extensions["acme:owner"]
        assertEquals(ExtensionType.STRING, owner?.type)
        assertEquals("data-platform", (owner as StringValueJs).value)
    }

    @Test
    fun testRoundTrip() {
        val jsModel = buildV1ModelJs()

        val model = GraphModelEditor.model(jsModel)

        // Spot-check the class side of the v1 shape before the round trip
        assertEquals("https://neo4j.com/ontology-spec/1.0.0/schema.json", model.schema)
        assertEquals("editor-built", model.id)
        assertEquals(setOf("node0", "node1"), model.nodes.keys)
        assertEquals(setOf("relationship0"), model.relationships.keys)
        val person = model.nodes.getValue("node0")
        assertEquals("Person", person.label)
        assertEquals("VECTOR<FLOAT>", person.properties.getValue("property2").type)
        assertEquals(1536, person.properties.getValue("property2").dimension)
        assertEquals("unique", person.constraints[0].type)
        assertEquals("person_name_unique_v2", person.constraints[0].name)
        assertEquals("canonicalQuery", person.tools[0].type)
        assertEquals("MATCH (p:Person) RETURN p", person.tools[0].extra.getValue("cypher").asString)
        val rel = model.relationships.getValue("relationship0")
        assertEquals("ACTED_IN", rel.type)
        assertEquals("node0", rel.from.node)
        assertEquals(1, rel.from.count)
        assertEquals("node1", rel.to.node)
        assertEquals(1, rel.to.minCount)
        assertEquals(5, rel.to.maxCount)
        assertEquals("acted_in_roles_unique_v2", rel.constraints[0].name)
        assertEquals("data-platform", model.extensions.getValue("acme:owner").asString)

        // model -> toJs -> toClass keeps the editor-built model (data-class equality)
        val roundTripped = GraphModelEditor.model(GraphModelEditor.plain(model))
        assertEquals(model, roundTripped, "The toJs/toClass round trip changed the editor-built model.")
    }

    @Test
    fun testExtensionsEditor() {
        // ADR-0006: ExtensionsEditor stays string-only set/remove on the extensions record
        val model = graphModelJs(id = "m", version = 1)

        ExtensionsEditor.set(model.extensions, "acme:owner", "data-platform")
        val value = model.extensions["acme:owner"]
        assertEquals(ExtensionType.STRING, value?.type)
        assertEquals("data-platform", (value as StringValueJs).value)

        ExtensionsEditor.set(model.extensions, "acme:owner", "replaced")
        assertEquals("replaced", (model.extensions["acme:owner"] as StringValueJs).value)

        ExtensionsEditor.remove(model.extensions, "acme:owner")
        assertNull(model.extensions["acme:owner"])
    }
}
