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
import kotlin.js.JsExport
import kotlin.js.JsStatic

/**
 * Builds constraint and index names in the importer's expected form, `<property>_<label>_<suffix>`.
 */
@JsExport
class NameFormat {
    companion object {
        private const val MAX_LENGTH = 800

        @JsStatic
        fun constraintName(properties: List<String>, entity: String, type: ConstraintType): String =
            build(properties, entity, suffixFrom(type))

        @JsStatic
        fun indexName(properties: List<String>, entity: String, type: IndexType? = null): String =
            build(properties, entity, suffixFrom(type))

        private fun build(properties: List<String>, entity: String, suffix: String?): String {
            val propertyPart = properties.joinToString("_") { escape(it) }
            if (propertyPart.isEmpty()) {
                return ""
            }
            val base = if (entity.isEmpty()) propertyPart else "${propertyPart}_${escape(entity)}"
            val suffixPart = if (suffix == null) "" else "_$suffix"
            return base.take((MAX_LENGTH - suffixPart.length).coerceAtLeast(0)) + suffixPart
        }

        private fun suffixFrom(type: ConstraintType): String = when (type) {
            ConstraintType.UNIQUE -> "uniq"
            ConstraintType.KEY -> "key"
            ConstraintType.EXISTS -> "propertyExistence"
            ConstraintType.PROPERTY_TYPE -> "propertyType"
        }

        private fun suffixFrom(type: IndexType?): String? = type?.let { it.name.lowercase() }

        /**
         * An underscore inside a token is doubled, so the parts stay recoverable. Without it `pizza_sten` on
         * label `ugn` collides with `pizza` on `sten_ugn`. A token that starts or ends with an underscore is
         * still ambiguous.
         */
        private fun escape(token: String): String = token.replace("_", "__")
    }
}
