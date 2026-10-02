//go:build darwin && arm64 && !ontologyspec_noembed

package bridge

import _ "embed"

//go:embed lib/macos-arm64/libontologymodel.dylib
var embeddedLib []byte
