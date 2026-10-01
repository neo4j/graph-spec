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
        assertEquals(ConstraintType.UNIQUE, dataModelConstraintTypeFrom("uniqueness"))
        assertEquals(ConstraintType.KEY, dataModelConstraintTypeFrom("key"))
        assertEquals(ConstraintType.EXISTS, dataModelConstraintTypeFrom("propertyExistence"))
        assertEquals(ConstraintType.PROPERTY_TYPE, dataModelConstraintTypeFrom("propertyType"))
        assertNull(dataModelConstraintTypeFrom("something else"))
    }

    @Test
    fun `data model index type words map to the graph spec type`() {
        assertEquals(IndexType.RANGE, dataModelIndexTypeFrom("default"))
        assertEquals(IndexType.RANGE, dataModelIndexTypeFrom("range"))
        assertEquals(IndexType.TEXT, dataModelIndexTypeFrom("text"))
        assertEquals(IndexType.FULLTEXT, dataModelIndexTypeFrom("fullText"))
        assertEquals(IndexType.POINT, dataModelIndexTypeFrom("point"))
        assertEquals(IndexType.VECTOR, dataModelIndexTypeFrom("vector"))
        assertEquals(IndexType.LOOKUP, dataModelIndexTypeFrom("lookup"))
        assertNull(dataModelIndexTypeFrom("something else"))
        assertNull(dataModelIndexTypeFrom("fulltext"))
        assertNull(dataModelIndexTypeFrom("FullText"))
    }
}
