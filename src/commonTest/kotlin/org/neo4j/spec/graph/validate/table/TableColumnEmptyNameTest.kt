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
package org.neo4j.spec.graph.validate.table

import org.neo4j.spec.graph.model.GraphModel
import org.neo4j.spec.graph.model.extension.source.Table
import org.neo4j.spec.graph.model.extension.source.TableColumn
import org.neo4j.spec.graph.validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TableColumnEmptyNameTest {

    private val validator = TableColumnEmptyName
    private val model =
        GraphModel(schema = "https://neo4j.com/docs/ontology-graph-spec/4.0.0/schema.json", version = "1.0.0", id = "test")

    @Test
    fun `fail when column name is blank`() {
        val table = Table(
            source = "local",
            columns = mutableListOf(TableColumn(name = ""))
        )
        val issues = mutableListOf<Issue>()

        validator.validateTableColumn(model, "t:1", table, 0, table.columns[0], issues)

        assertEquals(1, issues.size)
        assertEquals("missing_table_column_name", issues.first().code)
    }

    @Test
    fun `pass when column has a valid name`() {
        val table = Table(
            source = "local",
            columns = mutableListOf(TableColumn(name = "email"))
        )
        val issues = mutableListOf<Issue>()

        validator.validateTableColumn(model, "t:1", table, 0, table.columns[0], issues)

        assertTrue(issues.isEmpty())
    }
}
