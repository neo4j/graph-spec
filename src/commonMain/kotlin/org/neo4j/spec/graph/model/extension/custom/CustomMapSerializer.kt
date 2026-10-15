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
package org.neo4j.spec.graph.model.extension.custom

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import org.neo4j.spec.graph.model.extension.custom.AnyValueSerializer.checkDepth

/**
 * Serializes user-defined custom extension data as a plain map of implicitly typed values.
 * @see AnyValueSerializer
 */
object CustomMapSerializer : KSerializer<MutableMap<String, Any>> {
    private val delegate = MapSerializer(String.serializer(), AnyValueSerializer)

    override val descriptor: SerialDescriptor = delegate.descriptor

    override fun serialize(encoder: Encoder, value: MutableMap<String, Any>) {
        value.values.forEach { it.checkDepth() }
        delegate.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): MutableMap<String, Any> = delegate.deserialize(decoder).toMutableMap()
}
