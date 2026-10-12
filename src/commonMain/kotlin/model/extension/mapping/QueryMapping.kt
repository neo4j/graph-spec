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
package model.extension.mapping

import kotlinx.schema.generator.json.SerialDescription
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
@SerialName(MappingType.QUERY)
@SerialDescription("Maps the results of a query run against a table.")
data class QueryMapping(
    @SerialDescription("Id of the source table.")
    var table: String,
    @SerialDescription("The query to run.")
    var query: String,
    @SerialName("depends_on")
    @SerialDescription("Id of the mapping which must be imported before this one. Empty when there is no dependency.")
    var dependsOn: String = "",
    @SerialDescription("Optional id of this mapping, so other mappings can reference it in depends_on.")
    var id: String = ""
) : Mapping
