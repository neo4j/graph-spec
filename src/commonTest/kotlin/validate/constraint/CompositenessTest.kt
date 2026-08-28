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
    fun `0 properties is not composite`() {
        assertFalse(isCompositeConstraint(emptySet()))
    }

    @Test
    fun `1 property is not composite`() {
        assertFalse(isCompositeConstraint(setOf("email")))
    }

    @Test
    fun `2+ properties is composite`() {
        assertTrue(isCompositeConstraint(setOf("email", "name")))
    }
}
