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
package validate.property

import model.property.Neo4jType

// UPX: MIN_DIMENSION/MAX_DIMENSION (details-panel/constants.ts#L46-L47).
const val MIN_DIMENSION = 1
const val MAX_DIMENSION = 4096

fun isVectorType(type: Neo4jType): Boolean = when (type) {
    Neo4jType.VECTOR_FLOAT,
    Neo4jType.VECTOR_FLOAT32,
    Neo4jType.VECTOR_INTEGER,
    Neo4jType.VECTOR_INTEGER32,
    Neo4jType.VECTOR_INTEGER16,
    Neo4jType.VECTOR_INTEGER8 -> true
    Neo4jType.ANY,
    Neo4jType.BOOLEAN,
    Neo4jType.LIST_BOOLEAN,
    Neo4jType.DATE,
    Neo4jType.LIST_DATE,
    Neo4jType.DURATION,
    Neo4jType.LIST_DURATION,
    Neo4jType.FLOAT32,
    Neo4jType.LIST_FLOAT32,
    Neo4jType.FLOAT,
    Neo4jType.LIST_FLOAT,
    Neo4jType.INTEGER8,
    Neo4jType.LIST_INTEGER8,
    Neo4jType.INTEGER16,
    Neo4jType.LIST_INTEGER16,
    Neo4jType.INTEGER32,
    Neo4jType.LIST_INTEGER32,
    Neo4jType.INTEGER,
    Neo4jType.LIST_INTEGER,
    Neo4jType.LOCAL_DATETIME,
    Neo4jType.LIST_LOCAL_DATETIME,
    Neo4jType.LOCAL_TIME,
    Neo4jType.LIST_LOCAL_TIME,
    Neo4jType.POINT,
    Neo4jType.LIST_POINT,
    Neo4jType.STRING,
    Neo4jType.LIST_STRING,
    Neo4jType.ZONED_DATETIME,
    Neo4jType.LIST_ZONED_DATETIME,
    Neo4jType.ZONED_TIME,
    Neo4jType.LIST_ZONED_TIME,
    Neo4jType.UUID -> false
}
