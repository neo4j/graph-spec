package validation_test

import (
	"embed"
	"encoding/json"
	"fmt"
	"testing"

	"github.com/neo4j/graph-spec/go/v4/model"
	"github.com/neo4j/graph-spec/go/v4/validation"
	"github.com/stretchr/testify/require"
)

//go:embed testdata/*.json
var testdata embed.FS

func TestValidate(t *testing.T) {
	raw, err := testdata.ReadFile("testdata/invalid-graph-model.json")
	require.NoError(t, err)

	var graph model.GraphModel
	err = json.Unmarshal(raw, &graph)
	require.NoError(t, err)

	res, err := validation.Validate(graph)
	require.NoError(t, err)

	t.Log(fmt.Sprintf("Validated graph: %v", res))
	require.Len(t, res, 3)

	codes := make([]string, len(res))
	for i := range res {
		codes[i] = res[i].Code
	}
	require.ElementsMatch(t, []string{
		"invalid_node_type_constraint_property_count",
		"invalid_node_exist_constraint_property_count",
		"missing_node_properties",
	}, codes)
}
