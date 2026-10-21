package org.neo4j.spec.graph.model.extension.mapping

import js.objects.toRecord
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertEquals

class NodeMappingEditorTest {

    private fun mapping(columns: Array<String> = emptyArray(), keys: Array<String> = emptyArray()): NodeMappingJs {
        val properties = mapOf("id" to propertyMappingJs("person_id"), "name" to propertyMappingJs("person_name"))
        return nodeMappingJs("person", "people", properties.toRecord(), "MERGE", null, columns, keys)
    }

    @Test
    fun testUseKeyProperties_convertsColumns() {
        val mapping = mapping(columns = arrayOf("person_id"))
        NodeMappingEditor.useKeyProperties(mapping)

        assertContentEquals(arrayOf("id"), mapping.keyProperties)
        assertEquals(0, mapping.keyColumns.size)
    }

    @Test
    fun testUseKeyColumns_convertsProperties() {
        val mapping = mapping(keys = arrayOf("id", "name"))
        NodeMappingEditor.useKeyColumns(mapping)

        assertContentEquals(arrayOf("person_id", "person_name"), mapping.keyColumns)
        assertEquals(0, mapping.keyProperties.size)
    }

    @Test
    fun testUseKeyColumns_whenUnmapped_throwsAndLeavesMappingUnchanged() {
        val mapping = mapping(keys = arrayOf("id", "missing"))
        assertFailsWith<IllegalArgumentException> { NodeMappingEditor.useKeyColumns(mapping) }

        assertContentEquals(arrayOf("id", "missing"), mapping.keyProperties)
        assertEquals(0, mapping.keyColumns.size)
    }
}
