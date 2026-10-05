//go:build linux && arm64 && !ontologygraphspec_noembed

package bridge

import _ "embed"

//go:embed lib/linux-arm64/libontologygraphmodel.so
var embeddedLib []byte
