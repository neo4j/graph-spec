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
package spec

import java.io.File
import kotlin.test.assertTrue

/** Classloader anchor for resolving the `/ontology` classpath resources. */
private object SpecExamplesAnchor

/**
 * The directory behind the `/ontology` classpath resource
 * (`src/jvmTest/resources/ontology/`, ADR-0007). Examples are classpath
 * resources, so the tests also survive IDE runs with a non-project working
 * directory.
 */
internal fun ontologyExamplesDir(): File =
    File(SpecExamplesAnchor.javaClass.getResource("/ontology")!!.path)

/**
 * The JSON ontology examples under `/ontology`, sorted by file name. The
 * resource glob (json filter + sortedBy + orEmpty) and the non-empty guard
 * live here, so every caller gets a guaranteed non-empty sorted list or an
 * assertion failure.
 */
internal fun ontologyExamples(): List<File> {
    val dir = ontologyExamplesDir()
    val examples =
        dir
            .listFiles { file -> file.isFile && file.extension == "json" }
            ?.sortedBy { it.name }
            .orEmpty()
    assertTrue(examples.isNotEmpty(), "no ontology examples found in ${dir.path}")
    return examples
}
