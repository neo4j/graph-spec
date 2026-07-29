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
package model.property

@JsExport
class PropertyEditor {
    companion object {

        @JsStatic
        fun setType(property: PropertyJs, type: Neo4jTypeJs) {
            property.type = type
        }

        @JsStatic
        fun setMustExist(property: PropertyJs, mustExist: Boolean) {
            property.mustExist = mustExist
            property.key = null
        }

        @JsStatic
        fun setUnique(property: PropertyJs, unique: Boolean) {
            property.unique = unique
            property.key = null
        }

        @JsStatic
        fun setKey(property: PropertyJs, key: Boolean) {
            property.key = key
            property.unique = null
            property.mustExist = null
        }

        @JsStatic
        fun setName(property: PropertyJs, name: String) {
            property.name = name
        }
    }
}
