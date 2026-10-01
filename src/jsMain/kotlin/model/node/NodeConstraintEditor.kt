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
package model.node

import model.dropAt
import kotlin.collections.plus

@JsExport
class NodeConstraintEditor {
    companion object {
        @JsStatic
        fun setType(constraint: NodeConstraintJs, type: String) {
            constraint.type = type
        }

        @JsStatic
        fun setName(constraint: NodeConstraintJs, name: String?) {
            constraint.name = name
        }

        @JsStatic
        fun addProperty(constraint: NodeConstraintJs, property: String) {
            if (!constraint.properties.contains(property)) {
                constraint.properties += property
            }
        }

        @JsStatic
        fun removeProperty(constraint: NodeConstraintJs, property: String) {
            val index = constraint.properties.indexOf(property)
            if (index != -1) {
                constraint.properties = constraint.properties.dropAt(index)
            }
        }
    }
}
