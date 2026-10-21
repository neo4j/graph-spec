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
package org.neo4j.spec.graph.model.extension.mapping

import js.objects.toRecord
import org.neo4j.spec.graph.model.extension.graphModelExtensionsJs
import org.neo4j.spec.graph.model.graphModelJs
import org.neo4j.spec.graph.model.relationship.relationshipJs
import org.neo4j.spec.graph.model.relationship.relationshipTargetJs
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class MappingKeyConversionEditorTest {

    private val person = nodeMappingJs(
        "person", "people", mapOf("id" to propertyMappingJs("person_id")).toRecord(), "MERGE", null,
        emptyArray(), emptyArray()
    )
    private val company = nodeMappingJs(
        "company", "companies", mapOf("code" to propertyMappingJs("company_code")).toRecord(), "MERGE", null,
        emptyArray(), arrayOf("code")
    )

    private fun worksAt(toColumn: String) = relationshipMappingJs(
        "worksAt", "jobs",
        targetMappingJs(nodeKeyColumn = "person_id", relationshipColumn = "pid"),
        targetMappingJs(nodeKeyColumn = toColumn, relationshipColumn = "cid"),
        mapOf("since" to propertyMappingJs("start_year")).toRecord(), "MERGE", null,
        arrayOf("start_year"), emptyArray()
    )

    private fun model(relationship: RelationshipMappingJs) = graphModelJs(
        version = "1",
        id = "test",
        relationships = mapOf(
            "worksAt" to relationshipJs(
                id = "worksAt",
                type = "WORKS_AT",
                from = relationshipTargetJs(node = "person"),
                to = relationshipTargetJs(node = "company")
            )
        ).toRecord(),
        extensions = graphModelExtensionsJs(
            mappings = mapOf("m0" to arrayOf<MappingJs>(person, company, relationship)).toRecord()
        )
    )

    @Test
    fun testUseKeyProperties_convertsMappingAndTargets() {
        val relationship = worksAt("company_code")
        MappingKeyConversionEditor.useKeyProperties(model(relationship), relationship)

        assertContentEquals(arrayOf("since"), relationship.keyProperties)
        assertEquals(0, relationship.keyColumns.size)
        assertEquals("id", relationship.fromNode.nodeKeyProperty)
        assertEquals("", relationship.fromNode.nodeKeyColumn)
        assertEquals("code", relationship.toNode.nodeKeyProperty)
    }

    @Test
    fun testUseAllKeyColumns_convertsEveryMapping() {
        val relationship = worksAt("company_code")
        val model = model(relationship)
        MappingKeyConversionEditor.useAllKeyProperties(model)
        MappingKeyConversionEditor.useAllKeyColumns(model)

        assertContentEquals(arrayOf("company_code"), company.keyColumns)
        assertEquals("person_id", relationship.fromNode.nodeKeyColumn)
        assertEquals("company_code", relationship.toNode.nodeKeyColumn)
    }

    @Test
    fun testFailedConversion_changesNothing() {
        val relationship = worksAt("unknown")

        assertFailsWith<IllegalArgumentException> {
            MappingKeyConversionEditor.useKeyProperties(model(relationship), relationship)
        }

        assertContentEquals(arrayOf("start_year"), relationship.keyColumns)
        assertEquals("person_id", relationship.fromNode.nodeKeyColumn)
    }
}
