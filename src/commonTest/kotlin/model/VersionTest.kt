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

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class VersionTest {

    @Test
    fun `schema url round trips`() {
        assertEquals("https://neo4j.io/ontology-graph-spec/4.0.0/", Version.schemaUrl("4.0.0"))
        assertEquals("4.0.0", Version.parseSchemaVersion(Version.schemaUrl("4.0.0")))
    }

    @Test
    fun `version is the last path segment regardless of host or trailing slash`() {
        assertEquals("4.1.0", Version.parseSchemaVersion("https://example.com/a/b/4.1.0"))
        assertEquals("4.1.0", Version.parseSchemaVersion("  https://example.com/a/b/4.1.0/  "))
        assertEquals("4.1.0-beta", Version.parseSchemaVersion("https://example.com/4.1.0-beta/"))
    }

    @Test
    fun `blank schema is rejected`() {
        assertFailsWith<IllegalArgumentException> { Version.parseSchemaVersion("") }
        assertFailsWith<IllegalArgumentException> { Version.parseSchemaVersion("/") }
    }
}
