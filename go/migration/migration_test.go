package migration_test

import (
	"embed"
	"testing"

	"github.com/neo4j/graph-spec/go/internal/bridge"
	"github.com/neo4j/graph-spec/go/migration"
	"github.com/stretchr/testify/require"
)

//go:embed testdata/*.json
var testdata embed.FS

// requireBridge skips the test when the native library cannot be loaded on this
// host (ontologygraphspec_noembed builds, or a platform whose bundled library is not
// checked in — see go/internal/bridge/lib/macos-arm64/README.md).
func requireBridge(t *testing.T) {
	t.Helper()
	if err := bridge.Available(); err != nil {
		t.Skipf("native library unavailable: %v", err)
	}
}

// TestSpecV4ToOntologyGraphSpecMigration drives the one supported bridge path (ADR-0008):
// a graph spec 4.0.0 document converts to the v1 ontology graph spec shape.
func TestSpecV4ToOntologyGraphSpecMigration(t *testing.T) {
	requireBridge(t)

	raw, err := testdata.ReadFile("testdata/graph-spec-example.json")
	require.NoError(t, err)

	result, err := migration.ToOntologyGraphSpec(string(raw), migration.ModelTypeSpecV4)
	require.NoError(t, err)

	// The v1 root shape: the spec link, a fresh ontology identity, version 1 as a
	// number (a string "version" would fail the model.GraphModel unmarshal above).
	require.Equal(t, "https://neo4j.com/ontology-graph-spec/1.0.0/schema.json", result.Schema)
	require.Equal(t, 1, result.Version)
	require.NotEmpty(t, result.ID, "the converter mints a fresh ontology id")
	require.NotNil(t, result.Name)
	require.Equal(t, "movies", *result.Name)

	// Nodes are keyed by local id; the label lives in label / labels.identifier.
	movie, ok := result.Nodes["movie"]
	require.True(t, ok, "nodes keyed by local id")
	require.NotNil(t, movie.Label)
	require.Equal(t, "Movie", *movie.Label)
	require.Nil(t, movie.Labels, "no implied/optional labels: shorthand label form")

	person, ok := result.Nodes["person"]
	require.True(t, ok)
	require.Nil(t, person.Label, "implied/optional labels: full labels object form")
	require.NotNil(t, person.Labels)
	require.Equal(t, "Person", person.Labels.Identifier)
	require.Equal(t, []string{"Actor"}, person.Labels.Implied)
	require.Equal(t, []string{"Director"}, person.Labels.Optional)

	// 4.0.0 type tokens map to v1 tokens (ADR-0008 §3): UUID -> STRING,
	// ZONED DATETIME -> DATETIME, VECTOR<FLOAT32> -> VECTOR<FLOAT> (dimension carried).
	require.Equal(t, "STRING", *movie.Properties["id"].Type)
	require.Equal(t, "DATETIME", *movie.Properties["released"].Type)
	require.Equal(t, "VECTOR<FLOAT>", *movie.Properties["embedding"].Type)
	require.Equal(t, 1536, *movie.Properties["embedding"].Dimension)

	// Property flags carry; the single-property machine-named EXISTS constraint
	// becomes the mustExist shorthand flag (ADR-0008 §6).
	require.True(t, *movie.Properties["id"].Unique)
	require.True(t, *movie.Properties["title"].MustExist)
	require.Empty(t, movie.Constraints)

	// Relationship endpoints are {node} references into the nodes map; the 4.0.0
	// endpoint label is dropped (ADR-0008 §5).
	actedIn, ok := result.Relationships["ACTED_IN"]
	require.True(t, ok)
	require.Equal(t, "ACTED_IN", actedIn.Type)
	require.Equal(t, "person", actedIn.From.Node)
	require.Equal(t, "movie", actedIn.To.Node)
	require.Equal(t, "LIST<STRING>", *actedIn.Properties["roles"].Type)
}

// TestUnsupportedPreV4InputRejected: the legacy data_model chain is deleted
// (ADR-0008 §10) — a 3.0.0 document is rejected with the unsupported-migration error.
func TestUnsupportedPreV4InputRejected(t *testing.T) {
	requireBridge(t)

	raw, err := testdata.ReadFile("testdata/northwind.json") // a data_model 3.0.0 document
	require.NoError(t, err)

	_, err = migration.ToOntologyGraphSpec(string(raw), migration.ModelTypeSpecV4)
	require.Error(t, err)
	require.ErrorContains(t, err, "Unsupported migration")
}
