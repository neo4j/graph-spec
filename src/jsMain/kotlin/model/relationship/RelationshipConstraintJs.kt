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
package model.relationship

import model.node.ConstraintJs
import model.node.constraintJs

/**
 * The v1 model unified node and relationship constraints into the shared
 * [model.node.Constraint] class, so both domains use the one [ConstraintJs]
 * shape. This alias keeps the pre-unification exported name
 * source-compatible.
 */
typealias RelationshipConstraintJs = ConstraintJs

fun relationshipConstraintJs(
    type: String,
    name: String? = null,
    properties: Array<String> = emptyArray(),
): RelationshipConstraintJs = constraintJs(type, name, properties)
