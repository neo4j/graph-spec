package validate.node

import model.GraphModel
import model.node.Node
import model.node.NodeIndex
import validate.Issue

object NodeIndexOptions : NodeValidation {
    override fun validateIndex(
        model: GraphModel,
        nodeId: String,
        node: Node,
        indexId: String,
        index: NodeIndex,
        issues: MutableList<Issue>
    ) {
        if (index.options != null && index.options.type != index.type) {
            issues.add(
                Issue(
                    code = "node_index_type_options_mismatch",
                    message = "Cannot use options type '${index.options.type}' with node index '$indexId' type '${index.type}'",
                    path = "nodes.$nodeId.indexes.$indexId.options"
                )
            )
        }
    }
}
