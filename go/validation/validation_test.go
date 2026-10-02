package validation_test

import (
	"embed"
	"encoding/json"
	"testing"

	"github.com/neo4j/graph-spec/go/internal/bridge"
	"github.com/neo4j/graph-spec/go/model"
	"github.com/neo4j/graph-spec/go/validation"
	"github.com/stretchr/testify/require"
)

//go:embed testdata/*.json
var testdata embed.FS

// TestValidate: the fixture is a v1-shaped but invalid document — the ACTED_IN
// relationship's `to` endpoint references the node id "movie", which is not a key
// in the nodes map. The v1 validator (validate/relationship/EndpointNodeReferences.kt)
// reports exactly that with the missing_relation_to_node code.
func TestValidate(t *testing.T) {
	if err := bridge.Available(); err != nil {
		t.Skipf("native library unavailable: %v", err)
	}

	raw, err := testdata.ReadFile("testdata/invalid-graph-model.json")
	require.NoError(t, err)

	var graph model.GraphModel
	err = json.Unmarshal(raw, &graph)
	require.NoError(t, err)

	res, err := validation.Validate(graph)
	require.NoError(t, err)

	require.Len(t, res, 1)
	require.Equal(t, "missing_relation_to_node", res[0].Code)
	require.Equal(t, "relationships.ACTED_IN.to.node", res[0].Path)
}
