//go:build graphspec_noembed

package bridge

import (
	"testing"

	"github.com/stretchr/testify/require"
)

func TestResolveLibraryPathNoEmbeddedLib(t *testing.T) {
	t.Setenv(LibPathEnv, "") // ensure the override branch is skipped

	_, err := resolveLibraryPath()
	require.Error(t, err)
	require.ErrorContains(t, err, "no graphdatamodel shared library is available")
}
