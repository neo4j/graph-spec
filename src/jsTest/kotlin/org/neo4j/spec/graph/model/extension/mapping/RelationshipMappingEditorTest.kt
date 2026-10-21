package org.neo4j.spec.graph.model.extension.mapping

import js.objects.toRecord
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

class RelationshipMappingEditorTest {

    private fun mapping(columns: Array<String> = emptyArray(), keys: Array<String> = emptyArray()): RelationshipMappingJs {
        val properties = mapOf("id" to propertyMappingJs("person_id"), "name" to propertyMappingJs("person_name"))
        return relationshipMappingJs("knows", "people", targetMappingJs(), targetMappingJs(), properties.toRecord(), "MERGE", null, columns, keys)
    }

    @Test
    fun testUseKeyProperties_convertsColumns() {
        val mapping = mapping(columns = arrayOf("person_id"))
        RelationshipMappingEditor.useKeyProperties(mapping)

        assertContentEquals(arrayOf("id"), mapping.keyProperties)
        assertEquals(0, mapping.keyColumns.size)
    }

    @Test
    fun testUseKeyColumns_convertsProperties() {
        val mapping = mapping(keys = arrayOf("id", "name"))
        RelationshipMappingEditor.useKeyColumns(mapping)

        assertContentEquals(arrayOf("person_id", "person_name"), mapping.keyColumns)
        assertEquals(0, mapping.keyProperties.size)
    }

    @Test
    fun testUseKeyColumns_whenUnmapped_throwsAndLeavesMappingUnchanged() {
        val mapping = mapping(keys = arrayOf("id", "missing"))
        assertFailsWith<IllegalArgumentException> { RelationshipMappingEditor.useKeyColumns(mapping) }

        assertContentEquals(arrayOf("id", "missing"), mapping.keyProperties)
        assertEquals(0, mapping.keyColumns.size)
    }
}
