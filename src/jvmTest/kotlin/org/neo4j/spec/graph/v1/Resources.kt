
package org.neo4j.spec.graph.v1

import java.nio.charset.Charset
import kotlin.reflect.KClass

internal fun KClass<*>.resourceAsString(name: String) =
    java.getResourceAsStream(name)!!.readBytes().toString(Charset.defaultCharset())
