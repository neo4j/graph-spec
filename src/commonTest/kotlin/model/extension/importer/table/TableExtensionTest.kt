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
package model.extension.importer.table

import model.GraphModel
import model.extension.ListValue
import model.extension.MapValue
import model.extension.StringValue
import model.extension.getNamed
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TableExtensionTest {
    private fun modelWithTable(payload: MutableMap<String, model.extension.ExtensionValue>) =
        GraphModel(schema = "s", id = "t", version = 1).apply {
            extensions[TableExtensionModule.key] = MapValue(payload)
        }

    @Test
    fun `decodes the minimal v1 payload`() {
        val model =
            modelWithTable(
                mutableMapOf(
                    "name" to StringValue("actors"),
                    "source" to StringValue("sql/postgres"),
                    "columns" to
                        MapValue(
                            mutableMapOf("actor_id" to MapValue(mutableMapOf("type" to StringValue("varchar"))))
                        )
                )
            )

        val table = model.getNamed(TableExtensionModule)!!

        assertEquals("actors", table.name)
        assertEquals("varchar", table.columns["actor_id"]?.type)
    }

    @Test
    fun `TableColumnType flags a typeless column on a non-local source`() {
        val model =
            modelWithTable(
                mutableMapOf(
                    "source" to StringValue("sql/postgres"),
                    "columns" to MapValue(mutableMapOf("c" to MapValue(mutableMapOf())))
                )
            )
        val issues = mutableListOf<Issue>()

        TableColumnType.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_table_column_type", issues[0].code)
    }

    @Test
    fun `TableColumnType keeps the local-source exemption`() {
        val model =
            modelWithTable(
                mutableMapOf(
                    "source" to StringValue("local"),
                    "columns" to MapValue(mutableMapOf("c" to MapValue(mutableMapOf())))
                )
            )
        val issues = mutableListOf<Issue>()

        TableColumnType.validate(model, issues)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun `TableColumnType fires on a converter-style list payload`() {
        val model =
            GraphModel(schema = "s", id = "t", version = 1).apply {
                extensions[TableExtensionModule.key] =
                    ListValue(
                        mutableListOf(
                            MapValue(
                                mutableMapOf(
                                    "name" to StringValue("actors"),
                                    "source" to StringValue("sql/postgres"),
                                    "columns" to MapValue(mutableMapOf("c" to MapValue(mutableMapOf())))
                                )
                            )
                        )
                    )
            }
        val issues = mutableListOf<Issue>()

        TableColumnType.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_table_column_type", issues[0].code)
        assertEquals("extensions[\"importer:table\"].columns.c.type", issues[0].path)
    }

    @Test
    fun `TableColumnName flags a blank column key`() {
        val model =
            modelWithTable(
                mutableMapOf("columns" to MapValue(mutableMapOf("" to MapValue(mutableMapOf()))))
            )
        val issues = mutableListOf<Issue>()

        TableColumnName.validate(model, issues)

        assertEquals(1, issues.size)
        assertEquals("empty_table_column_name", issues[0].code)
    }
}
