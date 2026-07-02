package bridge

import (
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"sync"

	"github.com/ebitengine/purego"
)

// LibPathEnv can be set to an absolute path to the graphdatamodel shared library.
// When set it takes precedence over the embedded copy, letting callers supply their
// own library (e.g. one baked into an image at a fixed path for hardened runtimes
// where extracting to a temp dir is not possible). It is dormant when unset.
const LibPathEnv = "GRAPHDATAMODEL_LIB_PATH"

var library = sync.OnceValues(openLibrary)

func libraryExt() string {
	if runtime.GOOS == "darwin" {
		return ".dylib"
	}
	return ".so"
}

func resolveLibraryPath() (string, error) {
	if p := os.Getenv(LibPathEnv); p != "" {
		return p, nil
	}
	if len(embeddedLib) > 0 {
		return extractEmbeddedLib()
	}
	return "", fmt.Errorf("no graphdatamodel shared library is available: this build has no embedded library, "+
		"meaning it was built with graphspec_noembed or an unsupported platform is being used. Set %s to the shared "+
		"library's path", LibPathEnv)
}

// extractEmbeddedLib writes the embedded library into a private, per-process temp dir and returns its path.
func extractEmbeddedLib() (string, error) {
	dir, err := os.MkdirTemp("", "graphdatamodel-")
	if err != nil {
		return "", fmt.Errorf("could not create temp dir for embedded library: %w", err)
	}
	path := filepath.Join(dir, "libgraphdatamodel"+libraryExt())
	// File written with owner-only perms
	if err := os.WriteFile(path, embeddedLib, 0o700); err != nil {
		_ = os.RemoveAll(dir)
		return "", fmt.Errorf("could not write embedded library: %w", err)
	}
	return path, nil
}

func openLibrary() (uintptr, error) {
	path, err := resolveLibraryPath()
	if err != nil {
		return 0, err
	}

	lib, err := purego.Dlopen(path, purego.RTLD_NOW|purego.RTLD_GLOBAL)
	if err != nil {
		return 0, fmt.Errorf(
			"could not load the graphdatamodel shared library %q: %w; ensure the runtime provides "+
				"glibc and libstdc++ and that the shared library exists, or set %s to its path",
			path, err, LibPathEnv)
	}
	return lib, nil
}
