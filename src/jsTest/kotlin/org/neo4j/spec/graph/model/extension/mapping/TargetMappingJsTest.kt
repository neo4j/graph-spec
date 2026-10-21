package org.neo4j.spec.graph.model.extension.mapping

import kotlin.test.assertEquals

class TargetMappingJsTest : JsMappingTest<TargetMapping, TargetMappingJs>() {

    override fun createClass() = TargetMapping(
        nodeKeyColumn = "node_col",
        nodeKeyProperty = "node_prop",
        relationshipColumn = "rel_col"
    )

    override fun toJs(k: TargetMapping): TargetMappingJs = k.toJs()

    override fun toClass(js: TargetMappingJs): TargetMapping = js.toClass()

    override fun verifyJsObject(jsObject: TargetMappingJs) {
        assertEquals("node_col", jsObject.nodeKeyColumn)
        assertEquals("node_prop", jsObject.nodeKeyProperty)
        assertEquals("rel_col", jsObject.relationshipColumn)
    }

}
