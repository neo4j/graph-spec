package validate.relationship

import model.GraphModel
import model.node.Node
import model.node.NodeIndex
import model.relationship.Relationship
import model.relationship.RelationshipIndex
import validate.Issue
import validate.node.NodeValidation

object RelationshipIndexOptions : RelationshipValidation {
    override fun validateIndex(
        model: GraphModel,
        relationshipId: String,
        relationship: Relationship,
        indexId: String,
        index: RelationshipIndex,
        issues: MutableList<Issue>
    ) {
        if (index.options != null && index.options.type != index.type) {
            issues.add(
                Issue(
                    code = "node_index_type_options_mismatch",
                    message = "Cannot use options type '${index.options.type}' with node index '$indexId' type '${index.type}'",
                    path = "relationships.$relationshipId.indexes.$indexId.options"
                )
            )
        }
    }
}
