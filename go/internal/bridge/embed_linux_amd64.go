//go:build linux && amd64 && !ontologygraphspec_noembed

package bridge

import _ "embed"

//go:embed lib/linux-amd64/libontologygraphmodel.so
var embeddedLib []byte
