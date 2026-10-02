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
import codec.format.Format
import codec.format.JsonFormat
import codec.format.YamlFormat
import codec.schema.SchemaMap
import migrate.MigrationPath
import migrate.migration.graphSpec.GraphSpecV4OntologyGraphSpecMigration
import model.GraphModel
import model.Type
import model.Version
import kotlin.js.JsExport

@JsExport
sealed class OntologyGraphSpec(val configuration: OntologyGraphSpecConfig) {
    private val path = MigrationPath(configuration.migrations)

    /*
        In v1, GraphModel.version is the ontology's own version (identity metadata), not the
        format version; the format version of every document this SDK reads and writes is
        Version.LATEST, so the MigrationPath is consulted with the format version, not the
        document's. The path holds exactly one migration: the one-way 4.0.0 -> 1.0.0
        converter (ADR-0004 track 3, ADR-0008).
     */
    fun encodeToString(
        model: GraphModel,
        targetType: String = Type.ONTOLOGY_GRAPH_SPEC,
        targetVersion: String = Version.LATEST,
    ): String {
        if (!path.requiresMigration(Version.LATEST, Type.ONTOLOGY_GRAPH_SPEC, targetVersion, targetType)) {
            return configuration.format.encodeModelToString(model)
        }
        val schema = configuration.format.encodeToSchema(model)
        var map = schema as? SchemaMap ?: error("Schema format expected")
        map = path.migrate(map, Type.ONTOLOGY_GRAPH_SPEC, targetVersion, targetType)
        return configuration.format.encodeToString(map)
    }

    fun decodeFromString(content: String, type: String = Type.ONTOLOGY_GRAPH_SPEC): GraphModel {
        if (type == Type.ONTOLOGY_GRAPH_SPEC) {
            val model = configuration.format.decodeModelFromString(content)
            if (!path.requiresMigration(Version.LATEST, type, Version.LATEST, Type.ONTOLOGY_GRAPH_SPEC)) {
                return model
            }
        }
        val schema = configuration.format.decodeFromString(content)
        var map = schema as? SchemaMap ?: error("Schema format expected")
        map = path.migrate(map, type, Version.LATEST, Type.ONTOLOGY_GRAPH_SPEC)
        return configuration.format.decodeFromSchema(map)
    }

    object Json : OntologyGraphSpec(defaultConfig(JsonFormat.default))

    object Yaml : OntologyGraphSpec(defaultConfig(YamlFormat.default))
}

fun defaultConfig(format: Format): OntologyGraphSpecConfig {
    // ADR-0008: the one-way 4.0.0 -> 1.0.0 converter is the single default migration, so
    // OntologyGraphSpec.Json.decodeFromString(doc, Type.GRAPH_SPEC) works with no API change.
    return OntologyGraphSpecConfig.Builder(format)
        .apply { migrate(GraphSpecV4OntologyGraphSpecMigration()) }
        .build()
}

private class OntologyGraphSpecImpl(configuration: OntologyGraphSpecConfig) : OntologyGraphSpec(configuration)

fun OntologyGraphSpec(
    from: OntologyGraphSpec = OntologyGraphSpec.Json,
    builderAction: OntologyGraphSpecConfig.Builder.() -> Unit,
): OntologyGraphSpec {
    val builder = OntologyGraphSpecConfig.Builder(from.configuration)
    builder.builderAction()
    val conf = builder.build()
    return OntologyGraphSpecImpl(conf)
}
