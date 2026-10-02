//go:build linux && arm64 && !ontologyspec_noembed

package bridge

import _ "embed"

//go:embed lib/linux-arm64/libontologymodel.so
var embeddedLib []byte
