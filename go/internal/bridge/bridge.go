package bridge

import (
	"fmt"
	"math"
	"runtime"
	"sync"
	"unsafe"

	"github.com/ebitengine/purego"
)

// The native library writes a single status byte followed by the raw UTF-8 payload, representing the result on
// success and the error message on failure. It returns the number of total bytes written, so the payload is buf[1:res].
const (
	statusOK    byte = 0
	statusError byte = 1

	// The library returns the negative size the output buffer needs to be when the one we provided was too small.
	// These two codes are not sizes: -1 is a generic argument failure and internalError means the library could not
	// allocate a response at all, so neither is retryable.
	argError      = -1
	internalError = math.MinInt32
)

// MaxHeapEnv is read by the native library, on its first call, to cap its heap. It is documented for callers in
// the README, and named here only so failures to allocate can point at it.
const MaxHeapEnv = "GRAPHSPEC_MAX_HEAP_BYTES"

type Op string

const (
	Migrate  Op = "Migrate"
	Validate Op = "Validate"

	// exported native method names
	migrate  = "migrate"
	validate = "validate"
)

type bridge struct {
	migrate  func(inputJSON unsafe.Pointer, inputType, targetType, targetVersion string, outputBuffer unsafe.Pointer, bufferSize int32) int32
	validate func(inputJSON unsafe.Pointer, outputBuffer unsafe.Pointer, bufferSize int32) int32
}

var loadBridge = sync.OnceValues(bindBridge)

func Call(op Op, model []byte, args ...string) ([]byte, error) {
	b, err := loadBridge()
	if err != nil {
		return nil, err
	}

	if len(model) == 0 {
		return nil, fmt.Errorf("empty input provided")
	}
	for i, arg := range args {
		if len(arg) == 0 {
			return nil, fmt.Errorf("empty input provided [%d]", i+1)
		}
	}
	// The C boundary expects NUL terminated strings, so append a null byte to model bytes
	cModel := append(model, 0)

	// To simplify memory management on the Kotlin side we create an output buffer, still under
	// the scope of the Go GC, for the Kotlin library to write a response to. We must also provide
	// the maximum buffer size to avoid overflows when the Kotlin library writes the output. We use
	// the length of the model as reference.
	buf := make([]byte, 2*len(model))
	res, err := b.call(op, cModel, args, buf)
	if err != nil && res < argError && res != internalError { // buffer not large enough, retry with required size
		buf = make([]byte, -res)
		res, err = b.call(op, cModel, args, buf)
	}
	if err != nil {
		return nil, err
	}
	if res < 1 {
		return nil, fmt.Errorf("bridge returned a malformed response of %d bytes", res)
	}

	status, payload := buf[0], buf[1:res]
	if status == statusError {
		return nil, fmt.Errorf("received error from library: %s", payload)
	}
	if status != statusOK {
		return nil, fmt.Errorf("bridge returned unknown status byte %d", status)
	}
	return payload, nil
}

func (b *bridge) call(op Op, model []byte, args []string, buf []byte) (int, error) {
	// The input and output buffers are pinned for the duration of the native call so the Go GC cannot move them while
	// the Kotlin library holds the pointers. Pinning also keeps them reachable.
	var pinner runtime.Pinner
	pinner.Pin(&model[0])
	pinner.Pin(&buf[0])
	defer pinner.Unpin()

	in := unsafe.Pointer(&model[0])
	out := unsafe.Pointer(&buf[0])
	outLen := int32(len(buf))

	var res int32
	switch op {
	case Migrate:
		if len(args) != 3 {
			return argError, fmt.Errorf("migrate requires 3 arguments, got %d", len(args))
		}
		res = b.migrate(in, args[0], args[1], args[2], out, outLen)
	case Validate:
		if len(args) != 0 {
			return argError, fmt.Errorf("validate requires 0 arguments, got %d", len(args))
		}
		res = b.validate(in, out, outLen)
	default:
		return argError, fmt.Errorf("unknown bridge call: %s", op)
	}

	if res == internalError {
		return internalError, fmt.Errorf("library could not allocate a response, the input likely exceeds the heap allowed by %s", MaxHeapEnv)
	}
	if res < 0 {
		return int(res), fmt.Errorf("bridge error (buffer too small or internal failure): code %d", res)
	}
	return int(res), nil
}

func bindBridge() (b *bridge, err error) {
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

	b = &bridge{}
	purego.RegisterLibFunc(&b.migrate, lib, migrate)
	purego.RegisterLibFunc(&b.validate, lib, validate)
	return b, nil
}
