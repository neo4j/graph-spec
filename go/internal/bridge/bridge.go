package bridge

import (
	"encoding/json"
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"sync"
	"unsafe"

	"github.com/ebitengine/purego"
)

// LibPathEnv can be set to an absolute path to the graphdatamodel shared library.
// When set it takes precedence over the embedded copy, letting callers supply their
// own library (e.g. one baked into an image at a fixed path for hardened runtimes
// where extracting to a temp dir is not possible). It is dormant when unset.
const LibPathEnv = "GRAPHDATAMODEL_LIB_PATH"

// embeddedLib holds the platform's shared library bytes. It is populated by the
// embed_<platform>.go files and is nil when embedding is disabled (the
// `graphspec_noembed` build tag) or the platform has no bundled library — in which
// case GRAPHDATAMODEL_LIB_PATH must be set.

type Resp struct {
	Data   string `json:"data"`
	ErrMsg string `json:"error"`
}

type Op string

const (
	Migrate  Op = "Migrate"
	Validate Op = "Validate"
)

// bridgeFuncs holds the library's exported functions once bound.
type bridgeFuncs struct {
	migrate  func(inputJSON, inputType, targetType, targetVersion string, outputBuffer unsafe.Pointer, bufferSize int32) int32
	validate func(inputJSON string, outputBuffer unsafe.Pointer, bufferSize int32) int32
}

// loadBridge resolves, opens, and binds the shared library exactly once, lazily on
// the first Call — so importing this package never fails; any failure is returned
// from that first Call.
var loadBridge = sync.OnceValues(openBridge)

func libraryExt() string {
	if runtime.GOOS == "darwin" {
		return ".dylib"
	}
	return ".so"
}

// resolveLibraryPath decides which shared library the loader should open, in order:
//  1. the GRAPHDATAMODEL_LIB_PATH override, used verbatim when set;
//  2. the embedded library, extracted to a private temp file (the zero-setup default).
//
// When neither is available (built with `graphspec_noembed`, or an unsupported
// platform, and no override set) it returns an error rather than searching the
// dynamic loader's path, so we never load a same-named library an attacker could
// plant on that path.
func resolveLibraryPath() (string, error) {
	if p := os.Getenv(LibPathEnv); p != "" {
		return p, nil
	}
	if len(embeddedLib) > 0 {
		return extractEmbeddedLib()
	}
	return "", fmt.Errorf("no graphdatamodel library available: this binary was built without the embedded library, so %s must be set to the library path", LibPathEnv)
}

// extractEmbeddedLib writes the embedded library into a private, per-process temp
// directory (created 0700 with an unpredictable name) and returns its path. Using a
// private directory rather than a shared, predictable path avoids a temp-file
// hijack, where another user could pre-create the file we would otherwise load.
func extractEmbeddedLib() (string, error) {
	dir, err := os.MkdirTemp("", "graphdatamodel-")
	if err != nil {
		return "", fmt.Errorf("could not create temp dir for embedded library: %w", err)
	}
	path := filepath.Join(dir, "libgraphdatamodel"+libraryExt())
	if err := os.WriteFile(path, embeddedLib, 0o700); err != nil {
		_ = os.RemoveAll(dir)
		return "", fmt.Errorf("could not write embedded library: %w", err)
	}
	return path, nil
}

func openBridge() (b *bridgeFuncs, err error) {
	// purego.RegisterLibFunc panics if a symbol cannot be bound; convert that to an error.
	defer func() {
		if r := recover(); r != nil {
			b, err = nil, fmt.Errorf("failed to bind graphdatamodel symbols: %v", r)
		}
	}()

	path, err := resolveLibraryPath()
	if err != nil {
		return nil, err
	}

	lib, err := purego.Dlopen(path, purego.RTLD_NOW|purego.RTLD_GLOBAL)
	if err != nil {
		return nil, fmt.Errorf("could not load graphdatamodel library %q (set %s to override): %w", path, LibPathEnv, err)
	}

	b = &bridgeFuncs{}
	purego.RegisterLibFunc(&b.migrate, lib, "migrate")
	purego.RegisterLibFunc(&b.validate, lib, "validate")
	return b, nil
}

func Call(op Op, inputs ...string) (string, error) {
	b, err := loadBridge()
	if err != nil {
		return "", err
	}

	if len(inputs) == 0 {
		return "", fmt.Errorf("empty input provided")
	}
	for i, input := range inputs {
		if len(input) == 0 {
			return "", fmt.Errorf("empty input provided [%d]", i)
		}
	}

	// The library writes its JSON response into a caller-provided buffer. It is a Go
	// buffer (so it stays under the Go GC), pinned for the duration of the native call
	// so the GC cannot move it while the library holds the pointer.
	buf := make([]byte, 2*len(inputs[0]))
	var pinner runtime.Pinner
	pinner.Pin(&buf[0])
	defer pinner.Unpin()

	bufSize, err := callBridge(b, op, inputs, unsafe.Pointer(&buf[0]), int32(len(buf)))
	if err != nil {
		return "", err
	}

	var resp Resp
	if err := json.Unmarshal(buf[:bufSize], &resp); err != nil {
		return "", fmt.Errorf("failed to parse result: %w", err)
	}
	if resp.ErrMsg != "" {
		return "", fmt.Errorf("received error from library: %s", resp.ErrMsg)
	}

	return resp.Data, nil
}

func callBridge(b *bridgeFuncs, op Op, inputs []string, out unsafe.Pointer, outLen int32) (int, error) {
	var res int32
	switch op {
	case Migrate:
		if len(inputs) < 4 {
			return -1, fmt.Errorf("migrate requires 4 inputs, got %d", len(inputs))
		}
		res = b.migrate(inputs[0], inputs[1], inputs[2], inputs[3], out, outLen)
	case Validate:
		res = b.validate(inputs[0], out, outLen)
	default:
		return -1, fmt.Errorf("unknown bridge call: %s", op)
	}

	if res < 0 {
		return int(res), fmt.Errorf("bridge error (buffer too small or internal failure): code %d", res)
	}
	return int(res), nil
}
