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
package org.neo4j.spec.graph.migrate.migration.dataModel

import org.neo4j.spec.graph.codec.schema.schemaMapOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class EmptyModelRoundTripTest {

    private val gsToGm3 = GraphSpecDataModelV3Migration()
    private val gm3ToGs = DataModelV3GraphSpecMigration()

    @Test
    fun `empty gm3 round-trips gm3 to gs to gm3 to gs without exception or null output`() {
        val gm3Input = schemaMapOf("version" to "3.0.0")
        val result = gm3Input.let(gm3ToGs::migrate).let(gsToGm3::migrate).let(gm3ToGs::migrate).let(gsToGm3::migrate)
        assertNotNull(result)
    }

    @Test
    fun `empty gs round-trips gs to gm3 to gs to gm3 without exception or null output`() {
        val gsInput = schemaMapOf("version" to "1.0.0")
        val gsResult = gsInput.let(gsToGm3::migrate).let(gm3ToGs::migrate).let(gsToGm3::migrate).let(gm3ToGs::migrate)
        assertNotNull(gsResult)
        assertEquals("1.0.0", gsResult.string("version"))
    }
}
