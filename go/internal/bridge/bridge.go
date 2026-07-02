package bridge

import (
	"encoding/json"
	"fmt"
	"runtime"
	"sync"
	"unsafe"

	"github.com/ebitengine/purego"
)

type Resp struct {
	Data   string `json:"data"`
	ErrMsg string `json:"error"`
}

type Op string

const (
	Migrate  Op = "Migrate"
	Validate Op = "Validate"

	// export native method names
	migrate  = "migrate"
	validate = "validate"
)

// bridgeFuncs holds the lib's exported functions once bound.
type bridgeFuncs struct {
	migrate  func(inputJSON, inputType, targetType, targetVersion string, outputBuffer unsafe.Pointer, bufferSize int32) int32
	validate func(inputJSON string, outputBuffer unsafe.Pointer, bufferSize int32) int32
}

// loadBridge binds the lib's exported functions exactly once, lazily on first Call.
var loadBridge = sync.OnceValues(bindBridge)

// Call invokes the named bridge operation with the given string inputs and returns
// the lib's response, decoded from its JSON envelope.
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

func bindBridge() (b *bridgeFuncs, err error) {
	defer func() {
		// purego.RegisterLibFunc panics if a symbol cannot be bound which gets converted to an error.
		if r := recover(); r != nil {
			b, err = nil, fmt.Errorf("failed to bind graphdatamodel symbols: %v", r)
		}
	}()

	lib, err := library()
	if err != nil {
		return nil, err
	}

	b = &bridgeFuncs{}
	purego.RegisterLibFunc(&b.migrate, lib, migrate)
	purego.RegisterLibFunc(&b.validate, lib, validate)
	return b, nil
}
