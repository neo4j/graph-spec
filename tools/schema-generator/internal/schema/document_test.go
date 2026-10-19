package schema_test

import (
	"testing"

	"github.com/neo4j/ontology-graph-spec/tools/schema-generator/internal/schema"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

func TestResolve(t *testing.T) {
	doc, err := schema.Load([]byte(`{"$defs": {"A": {"type": "string"}}}`))
	require.NoError(t, err)

	def, err := doc.Resolve(&schema.Element{Ref: "#/$defs/A"})
	require.NoError(t, err)
	assert.Same(t, doc.Defs[0], def)

	tests := []struct{ name, ref, want string }{
		{"non-existent ref", "#/$defs/B", "does not resolve"},
		{"partial path", "#/$defs", "not supported"},
		{"property path", "#/properties/a", "not supported"},
		{"invalid", "not-a-path", "not supported"},
	}
	for _, tt := range tests {
		t.Run(tt.name, func(t *testing.T) {
			_, err := doc.Resolve(&schema.Element{Ref: tt.ref})
			assert.ErrorContains(t, err, tt.want)
		})
	}
}
