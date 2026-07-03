//go:build darwin && arm64 && !graphspec_noembed

package bridge

import _ "embed"

//go:embed lib/macos-arm64/libgraphdatamodel.dylib
var embeddedLib []byte
