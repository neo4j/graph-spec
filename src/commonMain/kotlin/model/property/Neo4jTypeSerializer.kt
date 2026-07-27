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

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Selects the concrete [Neo4jType] to decode, dispatching on the object's `type` discriminator:
 * `VECTOR` -> [VectorType], `LIST` -> [ListType], anything else -> [ScalarType]. Serialization
 * is delegated to each subtype's own serializer, so the object form is always emitted.
 */
object Neo4jTypeSerializer : JsonContentPolymorphicSerializer<Neo4jType>(Neo4jType::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Neo4jType> =
        when (element.jsonObject["type"]?.jsonPrimitive?.contentOrNull) {
            Neo4jTypeKind.VECTOR -> VectorType.serializer()
            Neo4jTypeKind.LIST -> ListType.serializer()
            else -> ScalarType.serializer()
        }
}
