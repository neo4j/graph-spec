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
package migrate.migration.dataModel

import model.type.ConstraintType
import model.type.IndexType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class DataModelUtilsTest {

    @Test
    fun `data model constraint type words map to the graph spec type`() {
        assertEquals(ConstraintType.UNIQUE, dataModelConstraintType("uniqueness"))
        assertEquals(ConstraintType.KEY, dataModelConstraintType("key"))
        assertEquals(ConstraintType.EXISTS, dataModelConstraintType("propertyExistence"))
        assertEquals(ConstraintType.PROPERTY_TYPE, dataModelConstraintType("propertyType"))
        assertNull(dataModelConstraintType("something else"))
    }

    @Test
    fun `data model index type words map to the graph spec type`() {
        assertEquals(IndexType.RANGE, dataModelIndexType("default"))
        assertEquals(IndexType.RANGE, dataModelIndexType("range"))
        assertEquals(IndexType.TEXT, dataModelIndexType("text"))
        assertEquals(IndexType.FULLTEXT, dataModelIndexType("fullText"))
        assertEquals(IndexType.POINT, dataModelIndexType("point"))
        assertEquals(IndexType.VECTOR, dataModelIndexType("vector"))
        assertEquals(IndexType.LOOKUP, dataModelIndexType("lookup"))
        assertNull(dataModelIndexType("something else"))
    }
}
