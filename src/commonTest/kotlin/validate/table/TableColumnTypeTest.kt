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
package validate.table

import model.GraphModel
import model.property.Neo4jType
import model.source.Table
import model.source.TableColumn
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TableColumnTypeTest {

    private val validator = TableColumnType
    private val model = GraphModel("4.0.0")

    @Test
    fun `fail on cloud column with empty supported types`() {
        // UPX: supportedTypes?.length === 0
        val table = Table(
            source = "sql/postgres",
            columns = mutableMapOf(
                "f:1" to TableColumn(name = "email", suggested = Neo4jType.STRING, supported = emptySet())
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateTableColumn(model, "t:1", table, "f:1", table.columns["f:1"]!!, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_table_column_type", issues.first().code)
    }

    @Test
    fun `pass on cloud column with non-empty supported types`() {
        val table = Table(
            source = "sql/postgres",
            columns = mutableMapOf(
                "f:1" to TableColumn(
                    name = "email",
                    suggested = Neo4jType.STRING,
                    supported = setOf(Neo4jType.STRING)
                )
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateTableColumn(model, "t:1", table, "f:1", table.columns["f:1"]!!, issues)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun `pass on local column with empty supported types`() {
        // rule skipped for local tables - only cloud columns are checked
        val table = Table(
            source = "local",
            columns = mutableMapOf("f:1" to TableColumn(name = "email", supported = emptySet()))
        )
        val issues = mutableListOf<Issue>()

        validator.validateTableColumn(model, "t:1", table, "f:1", table.columns["f:1"]!!, issues)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun `fail on cloud column with blank name`() {
        // type check is independent of name check - both can fire
        val table = Table(
            source = "sql/postgres",
            columns = mutableMapOf("f:1" to TableColumn(name = "", supported = emptySet()))
        )
        val issues = mutableListOf<Issue>()

        validator.validateTableColumn(model, "t:1", table, "f:1", table.columns["f:1"]!!, issues)

        assertEquals(1, issues.size)
        assertEquals("missing_table_column_type", issues.first().code)
    }
}
