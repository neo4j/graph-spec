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
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import script.TypeScriptTypes
import script.TypeScriptUnions
import java.io.File

/**
 * Given a generated .d.mts file replaces hard-coded enum classes
 * with string union types.
 * Note: this is a hack for union types until they are properly supported by kotlin plain js objects library
 * https://youtrack.jetbrains.com/issue/KT-55101/
 */
abstract class TypeScriptModifierTask : DefaultTask() {
    @get:InputFile
    abstract var typescriptFile: File

    @TaskAction
    fun run() {
        val unions = TypeScriptUnions()
        val types = TypeScriptTypes()

        // ConstraintType
        unions.create("ConstraintType", "ConstraintTypeJs")
        types.replace("NodeConstraintJs", "type", "string", "ConstraintTypeJs")
        types.replace("RelationshipConstraintJs", "type", "string", "ConstraintTypeJs")

        // IndexType
        unions.create("IndexType", "IndexTypeJs")
        types.replace("NodeIndexJs", "type", "string", "IndexTypeJs")
        types.replace("RelationshipIndexJs", "type", "string", "IndexTypeJs")

        // MappingMode
        unions.create("MappingMode", "MappingModeJs")
        types.replace("NodeMappingJs", "mode", "string", "MappingModeJs")

        val input = typescriptFile.readText()
        var result = unions.run(input)
        result = types.run(result)
        typescriptFile.writeText(result)
    }
}
