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
package validate.constraint

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CompositenessTest {

    @Test
    fun `0 properties is neither draft nor composite - it is empty`() {
        // ARRANGE
        val properties = emptySet<String>()
        val name: String? = null

        // ACT & ASSERT
        assertFalse(isDraftCompositeConstraint(properties, name))
        assertFalse(isCompositeConstraint(properties))
    }

    @Test
    fun `1 property no name is draft composite not composite`() {
        // ARRANGE
        val properties = setOf("email")
        val name: String? = null

        // ACT & ASSERT
        assertTrue(isDraftCompositeConstraint(properties, name))
        assertFalse(isCompositeConstraint(properties))
    }

    @Test
    fun `1 property with name is neither draft nor composite`() {
        // ARRANGE
        val properties = setOf("email")
        val name = "uniq_email"

        // ACT & ASSERT
        assertFalse(isDraftCompositeConstraint(properties, name))
        assertFalse(isCompositeConstraint(properties))
    }

    @Test
    fun `2+ properties is composite not draft`() {
        // ARRANGE
        val properties = setOf("email", "name")
        val name: String? = null

        // ACT & ASSERT
        assertTrue(isCompositeConstraint(properties))
        assertFalse(isDraftCompositeConstraint(properties, name))
    }
}
