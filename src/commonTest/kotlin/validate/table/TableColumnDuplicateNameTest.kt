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
import model.source.Table
import model.source.TableColumn
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TableColumnDuplicateNameTest {

    private val validator = TableColumnDuplicateName
    private val model = GraphModel("4.0.0")

    @Test
    fun `fail on duplicate field name - all occurrences flagged`() {
        // UPX findArrayDuplicates + includes flags ALL fields with a duplicated name
        val table = Table(
            source = "local",
            columns = mutableMapOf(
                "f:1" to TableColumn(name = "name"),
                "f:2" to TableColumn(name = "email"),
                "f:3" to TableColumn(name = "name")
            )
        )
        val issuesFirst = mutableListOf<Issue>()
        val issuesThird = mutableListOf<Issue>()

        validator.validateTableColumn(model, "t:1", table, "f:1", table.columns["f:1"]!!, issuesFirst)
        validator.validateTableColumn(model, "t:1", table, "f:3", table.columns["f:3"]!!, issuesThird)

        assertEquals(1, issuesFirst.size, "First occurrence should also be flagged as duplicate")
        assertEquals("duplicate_table_column_name", issuesFirst.first().code)
        assertEquals(1, issuesThird.size)
        assertEquals("duplicate_table_column_name", issuesThird.first().code)
    }

    @Test
    fun `pass when all field names are unique`() {
        val table = Table(
            source = "local",
            columns = mutableMapOf(
                "f:1" to TableColumn(name = "name"),
                "f:2" to TableColumn(name = "email")
            )
        )
        val issues = mutableListOf<Issue>()

        validator.validateTableColumn(model, "t:1", table, "f:1", table.columns["f:1"]!!, issues)
        validator.validateTableColumn(model, "t:1", table, "f:2", table.columns["f:2"]!!, issues)

        assertTrue(issues.isEmpty())
    }

    @Test
    fun `pass when field name is blank`() {
        // blank names are the empty-name validator's concern, not this one
        val table = Table(
            source = "local",
            columns = mutableMapOf("f:1" to TableColumn(name = ""))
        )
        val issues = mutableListOf<Issue>()

        validator.validateTableColumn(model, "t:1", table, "f:1", table.columns["f:1"]!!, issues)

        assertTrue(issues.isEmpty())
    }
}
