package org.neo4j.spec.graph.model.extension.mapping

import org.neo4j.spec.graph.model.extension.mapping.JsMappingTest
import kotlin.test.assertEquals

class QueryMappingJsTest : JsMappingTest<QueryMapping, QueryMappingJs>() {

    override fun createClass() = QueryMapping(
        table = "table_name",
        query = "cypher query",
        dependsOn = "other",
        id = "mapping-id"
    )

    override fun toJs(k: QueryMapping): QueryMappingJs = k.toJs()

    override fun toClass(js: QueryMappingJs): QueryMapping = js.toClass()

    override fun verifyJsObject(jsObject: QueryMappingJs) {
        assertEquals("QUERY", jsObject.type)
        assertEquals("table_name", jsObject.table)
        assertEquals("cypher query", jsObject.query)
        assertEquals("other", jsObject.dependsOn)
        assertEquals("mapping-id", jsObject.id)
    }

}
