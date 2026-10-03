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
package validate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Pins the Validations group composition (ADR-0005/0006): the import call sites gate on
 * mapping/table/index correctness, so their groups include the named-extension rules;
 * the kg-builder and parse-integrity call sites stay core-only.
 */
class ValidationsGroupTest {
    @Test
    fun `importReady and bulkImportReady include the named-extension rules`() {
        assertTrue(Validations.importReady.containsAll(Validations.namedExtensions))
        assertTrue(Validations.bulkImportReady.containsAll(Validations.namedExtensions))
        assertTrue(Validations.importReady.containsAll(Validations.core))
        assertTrue(Validations.bulkImportReady.containsAll(Validations.core))
    }

    @Test
    fun `kgbuilderReady and importParseIntegrity stay core-only`() {
        assertEquals(Validations.core, Validations.kgbuilderReady)
        assertEquals(Validations.core, Validations.importParseIntegrity)
    }

    @Test
    fun `namedExtensions is exactly the four modules' rules plus the malformed-payload rule`() {
        // 3 index + 0 display + 2 table + 3 mapping + 1 cross-cutting (MalformedNamedExtensionPayload)
        assertEquals(9, Validations.namedExtensions.size)
        assertTrue(Validations.namedExtensions.contains(model.extension.MalformedNamedExtensionPayload))
    }

    @Test
    fun `core aggregates the tool-type modules' rules exactly once`() {
        // ADR-0011: the predefined tool types' rules reach core through
        // model.tool.toolTypeModules, never as direct entries.
        val moduleRules = model.tool.toolTypeModules.flatMap { it.validations }
        assertTrue(Validations.core.containsAll(moduleRules))
        moduleRules.forEach { rule ->
            assertEquals(1, Validations.core.count { it == rule })
        }
    }

    @Test
    fun `all is the distinct union of the groups`() {
        val union =
            (
                Validations.core + Validations.kgbuilderReady + Validations.importReady +
                    Validations.importParseIntegrity + Validations.bulkImportReady +
                    Validations.namedExtensions
                ).distinct()
        assertEquals(union, Validations.all)
    }
}
