package validate.node

import model.GraphModel
import model.index.FullTextIndexOption
import model.index.PointIndexOption
import model.index.VectorIndexOption
import model.node.Node
import model.node.NodeIndex
import model.property.Property
import model.type.IndexType
import validate.Issue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeIndexOptionsTest {

    private val validator = NodeIndexOptions
    private val model = GraphModel("4.0.0")

    @Test
    fun `no options set - no issues`() {
        val nodeId = "userNode"
        val indexId = "idx_user_email_status"

        val node = Node(
            properties = mutableMapOf(
                "email" to Property(),
                "status" to Property()
            )
        )
        val index = NodeIndex(
            type = IndexType.TEXT,
            labels = mutableSetOf(),
            properties = mutableSetOf("email", "status"),
            options = null
        )

        val issues = mutableListOf<Issue>()
        validator.validateIndex(model, nodeId, node, indexId, index, issues)

        assertTrue(issues.isEmpty(), "Expected no validation issues when options is null")
    }

    @Test
    fun `options type matches index type - no issues`() {
        val nodeId = "userNode"
        val indexId = "idx_user_email_status"

        val node = Node(
            properties = mutableMapOf(
                "email" to Property(),
                "status" to Property()
            )
        )
        val index = NodeIndex(
            type = IndexType.FULLTEXT,
            labels = mutableSetOf(),
            properties = mutableSetOf("email", "status"),
            options = FullTextIndexOption()
        )

        val issues = mutableListOf<Issue>()
        validator.validateIndex(model, nodeId, node, indexId, index, issues)

        assertTrue(issues.isEmpty(), "Expected no validation issues when options type matches index type")
    }

    @Test
    fun `options type does not match index type - issue reported`() {
        val nodeId = "userNode"
        val indexId = "idx_user_email_status"

        val node = Node(
            properties = mutableMapOf(
                "email" to Property(),
                "status" to Property()
            )
        )
        val index = NodeIndex(
            type = IndexType.TEXT,
            labels = mutableSetOf(),
            properties = mutableSetOf("email", "status"),
            options = PointIndexOption()
        )

        val issues = mutableListOf<Issue>()
        validator.validateIndex(model, nodeId, node, indexId, index, issues)

        assertEquals(1, issues.size, "Expected exactly one validation issue")

        val issue = issues.first()
        assertEquals("node_index_type_options_mismatch", issue.code)
        assertEquals(
            "Cannot use options type 'POINT' with node index 'idx_user_email_status' type 'TEXT'",
            issue.message
        )
        assertEquals("nodes.userNode.indexes.idx_user_email_status.options", issue.path)
    }

    @Test
    fun `any mismatched type combination reports an issue`() {
        for (indexType in IndexType.entries) {
            for (options in listOf(FullTextIndexOption(), PointIndexOption(), VectorIndexOption())) {
                if (options.type == indexType) {
                    continue
                }
                val nodeId = "userNode"
                val indexId = "idx_mismatch"

                val node = Node(properties = mutableMapOf())
                val index = NodeIndex(
                    type = indexType,
                    labels = mutableSetOf(),
                    properties = mutableSetOf(),
                    options = options
                )

                val issues = mutableListOf<Issue>()
                validator.validateIndex(model, nodeId, node, indexId, index, issues)

                assertEquals(1, issues.size, "Expected a mismatch issue for $indexType vs $options")
                assertEquals("node_index_type_options_mismatch", issues.first().code)
            }
        }
    }

    @Test
    fun `matching type for every IndexType produces no issues`() {
        for (indexType in IndexType.entries) {
            for (options in listOf(FullTextIndexOption(), PointIndexOption(), VectorIndexOption())) {
                if (options.type != indexType) {
                    continue
                }
                val nodeId = "userNode"
                val indexId = "idx_match"

                val node = Node(properties = mutableMapOf())
                val index = NodeIndex(
                    type = indexType,
                    labels = mutableSetOf(),
                    properties = mutableSetOf(),
                    options = options
                )

                val issues = mutableListOf<Issue>()
                validator.validateIndex(model, nodeId, node, indexId, index, issues)

                assertTrue(issues.isEmpty(), "Expected no issues when options type equals index type ($indexType)")
            }
        }
    }
}
