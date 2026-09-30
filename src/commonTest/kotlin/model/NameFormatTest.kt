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
package model

import model.type.ConstraintType
import model.type.IndexType
import kotlin.test.Test
import kotlin.test.assertEquals

class NameFormatTest {

    @Test
    fun `an underscore inside a token is doubled`() {
        assertEquals("album__id_album", NameFormat.indexName(listOf("album_id"), "album"))
    }

    @Test
    fun `a token holding an underscore does not collide with a property boundary`() {
        assertEquals(
            "pizza__sten_ugn_uniq",
            NameFormat.constraintName(listOf("pizza_sten"), "ugn", ConstraintType.UNIQUE)
        )
        assertEquals(
            "pizza_sten__ugn_uniq",
            NameFormat.constraintName(listOf("pizza"), "sten_ugn", ConstraintType.UNIQUE)
        )
    }

    @Test
    fun `every underscore in a token is doubled`() {
        assertEquals("a____b_L_key", NameFormat.constraintName(listOf("a__b"), "L", ConstraintType.KEY))
    }

    @Test
    fun `a space does not collide with an underscore`() {
        assertEquals("a b_Label", NameFormat.indexName(listOf("a b"), "Label"))
        assertEquals("a__b_Label", NameFormat.indexName(listOf("a_b"), "Label"))
    }

    @Test
    fun `spaces are kept and underscores are doubled`() {
        assertEquals(
            "first name_id_Movie Actor",
            NameFormat.indexName(listOf("first name", "id"), "Movie Actor")
        )
    }

    @Test
    fun `property order is preserved`() {
        assertEquals("b_a_Label", NameFormat.indexName(listOf("b", "a"), "Label"))
    }

    @Test
    fun `no properties produces no name`() {
        assertEquals("", NameFormat.indexName(emptyList(), "Label"))
        assertEquals("", NameFormat.constraintName(emptyList(), "Label", ConstraintType.KEY))
    }

    @Test
    fun `empty entity produces the properties alone`() {
        assertEquals("album__id", NameFormat.indexName(listOf("album_id"), ""))
        assertEquals("album__id_uniq", NameFormat.constraintName(listOf("album_id"), "", ConstraintType.UNIQUE))
    }

    @Test
    fun `a long name is capped keeping the suffix`() {
        val name = NameFormat.constraintName(listOf("p".repeat(1000)), "Label", ConstraintType.UNIQUE)

        assertEquals(800, name.length)
        assertEquals(true, name.endsWith("_uniq"))
    }

    @Test
    fun `a long index name is capped keeping the suffix`() {
        val name = NameFormat.indexName(listOf("p".repeat(1000)), "Label", IndexType.VECTOR)

        assertEquals(800, name.length)
        assertEquals(true, name.endsWith("_vector"))
    }

    @Test
    fun `each constraint type picks its suffix`() {
        assertEquals("id_Actor_uniq", NameFormat.constraintName(listOf("id"), "Actor", ConstraintType.UNIQUE))
        assertEquals("id_Actor_key", NameFormat.constraintName(listOf("id"), "Actor", ConstraintType.KEY))
        assertEquals(
            "id_Actor_propertyExistence",
            NameFormat.constraintName(listOf("id"), "Actor", ConstraintType.EXISTS)
        )
        assertEquals(
            "id_Actor_propertyType",
            NameFormat.constraintName(listOf("id"), "Actor", ConstraintType.PROPERTY_TYPE)
        )
    }

    @Test
    fun `each index type picks its suffix and none when unset`() {
        assertEquals("id_Actor", NameFormat.indexName(listOf("id"), "Actor"))
        assertEquals("id_Actor_range", NameFormat.indexName(listOf("id"), "Actor", IndexType.RANGE))
        assertEquals("id_Actor_text", NameFormat.indexName(listOf("id"), "Actor", IndexType.TEXT))
        assertEquals("id_Actor_fulltext", NameFormat.indexName(listOf("id"), "Actor", IndexType.FULLTEXT))
        assertEquals("id_Actor_point", NameFormat.indexName(listOf("id"), "Actor", IndexType.POINT))
        assertEquals("id_Actor_vector", NameFormat.indexName(listOf("id"), "Actor", IndexType.VECTOR))
        assertEquals("id_Actor_lookup", NameFormat.indexName(listOf("id"), "Actor", IndexType.LOOKUP))
    }
}
