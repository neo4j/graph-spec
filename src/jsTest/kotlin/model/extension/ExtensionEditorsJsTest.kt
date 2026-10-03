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
package model.extension

import js.objects.Object
import model.GraphModelEditor
import model.extension.importer.mapping.MappingExtensionEditor
import model.extension.importer.mapping.MappingExtensionModule
import model.extension.importer.mapping.MappingKind
import model.extension.importer.mapping.MappingMode
import model.extension.importer.mapping.mappingExtensionJs
import model.extension.importer.table.ForeignKeyJs
import model.extension.importer.table.TableColumnEditor
import model.extension.importer.table.TableExtensionEditor
import model.extension.importer.table.TableExtensionModule
import model.extension.importer.table.foreignKeyJs
import model.extension.importer.table.foreignKeyReferenceJs
import model.extension.importer.table.tableColumnJs
import model.extension.importer.table.tableExtensionJs
import model.extension.neo4j.index.IndexExtensionEditor
import model.extension.neo4j.index.IndexExtensionModule
import model.extension.neo4j.index.indexExtensionJs
import model.extension.neo4j.index.toClass
import model.extension.ui.display.DisplayExtensionEditor
import model.extension.ui.display.displayExtensionJs
import model.graphModelJs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * JS-surface coverage of the named-extension twins and editors (ADR-0005): every editor
 * mutator is exercised against the *Js twins, and the GraphModelEditor root-extension
 * helpers are read back through the typed commonMain modules (getNamed).
 */
class ExtensionEditorsJsTest {
    @Test
    fun indexEditorMutators() {
        val index = indexExtensionJs()
        IndexExtensionEditor.setName(index, "actor_names")
        IndexExtensionEditor.setType(index, "range")
        IndexExtensionEditor.addProperty(index, "name")
        IndexExtensionEditor.addProperty(index, "name") // dedupe
        IndexExtensionEditor.addProperty(index, "born")
        IndexExtensionEditor.removeProperty(index, "born")

        assertEquals("actor_names", index.name)
        assertEquals("range", index.type)
        assertEquals(listOf("name"), index.properties.toList())
    }

    @Test
    fun displayEditorMutators() {
        val display = displayExtensionJs()
        DisplayExtensionEditor.setColor(display, "#e06209")
        DisplayExtensionEditor.setCaption(display, "name")
        DisplayExtensionEditor.setIcon(display, "person")
        DisplayExtensionEditor.setPosition(display, 10.0, 20.0)
        DisplayExtensionEditor.setPosition(display, null, null)

        assertEquals("#e06209", display.color)
        assertEquals("name", display.caption)
        assertEquals("person", display.icon)
        assertNull(display.x)
        assertNull(display.y)
    }

    @Test
    fun tableEditorMutators() {
        val table = tableExtensionJs()
        TableExtensionEditor.setName(table, "actors")
        TableExtensionEditor.setSource(table, "sql/postgres")

        val column = tableColumnJs()
        TableColumnEditor.setType(column, "varchar")
        TableColumnEditor.setSuggested(column, "STRING")
        TableColumnEditor.addSupported(column, "STRING")
        TableColumnEditor.addSupported(column, "STRING") // dedupe
        TableColumnEditor.addSupported(column, "INTEGER")
        TableColumnEditor.removeSupported(column, "INTEGER")
        TableColumnEditor.setDimension(column, 128)
        TableExtensionEditor.setColumn(table, "actor_id", column)

        TableExtensionEditor.addPrimaryKey(table, "actor_id")
        TableExtensionEditor.addPrimaryKey(table, "actor_id") // dedupe

        val fk: ForeignKeyJs = foreignKeyJs(arrayOf("movie_id"), foreignKeyReferenceJs("movies", arrayOf("id")))
        TableExtensionEditor.setForeignKey(table, "actor_movies", fk)

        assertEquals("actors", table.name)
        assertEquals("varchar", table.columns["actor_id"]!!.type)
        assertEquals("STRING", table.columns["actor_id"]!!.suggested)
        assertEquals(listOf("STRING"), table.columns["actor_id"]!!.supported.toList())
        assertEquals(128, table.columns["actor_id"]!!.dimension)
        assertEquals(listOf("actor_id"), table.primaryKeys.toList())
        assertEquals("movies", table.foreignKeys["actor_movies"]!!.references.table)

        TableExtensionEditor.removeForeignKey(table, "actor_movies")
        TableExtensionEditor.removePrimaryKey(table, "actor_id")
        TableExtensionEditor.removeColumn(table, "actor_id")

        assertFalse(Object.keys(table.foreignKeys).contains("actor_movies"))
        assertEquals(0, table.primaryKeys.size)
        assertFalse(Object.keys(table.columns).contains("actor_id"))
    }

    @Test
    fun mappingEditorMutators() {
        val mapping = mappingExtensionJs(kind = MappingKind.NODE)
        MappingExtensionEditor.setTable(mapping, "actors")
        MappingExtensionEditor.setMode(mapping, MappingMode.MERGE)
        MappingExtensionEditor.addKey(mapping, "actor_id")
        MappingExtensionEditor.addKey(mapping, "actor_id") // dedupe
        MappingExtensionEditor.setPropertyMapping(mapping, "name", "full_name")

        assertEquals("actors", mapping.table)
        assertEquals("merge", mapping.mode)
        assertEquals(listOf("actor_id"), mapping.key.toList())
        assertEquals("full_name", mapping.properties["name"]!!.column)

        MappingExtensionEditor.removePropertyMapping(mapping, "name")
        MappingExtensionEditor.removeKey(mapping, "actor_id")

        assertFalse(Object.keys(mapping.properties).contains("name"))
        assertEquals(0, mapping.key.size)
    }

    @Test
    fun graphModelEditorRootExtensionHelpers() {
        val model = graphModelJs(schema = "s", id = "t", version = 1)

        GraphModelEditor.setTableExtension(
            model,
            tableExtensionJs(name = "actors", source = "sql/postgres"),
        )
        GraphModelEditor.setMappingExtension(
            model,
            mappingExtensionJs(kind = MappingKind.NODE, node = "Actor", table = "actors", key = arrayOf("actor_id")),
        )

        // Read back through the typed commonMain modules
        val typed = GraphModelEditor.model(model)
        val table = typed.getNamed(TableExtensionModule)!!
        val mapping = typed.getNamed(MappingExtensionModule)!!

        assertEquals("actors", table.name)
        assertEquals("sql/postgres", table.source)
        assertEquals("Actor", mapping.node)
        assertEquals(setOf("actor_id"), mapping.key)

        GraphModelEditor.removeMappingExtension(model)
        assertNull(GraphModelEditor.model(model).getNamed(MappingExtensionModule))
        GraphModelEditor.removeTableExtension(model)
        assertNull(GraphModelEditor.model(model).getNamed(TableExtensionModule))
    }

    @Test
    fun indexTwinRoundTripsThroughToClass() {
        val index = indexExtensionJs(name = "ix", type = "vector", properties = arrayOf("embedding"))
        val back = index.toClass()

        assertEquals("ix", back.name)
        assertEquals("vector", back.type)
        assertEquals(listOf("embedding"), back.properties)
        assertEquals(IndexExtensionModule.key, IndexExtensionModule.key) // module reachable from jsTest
    }
}
