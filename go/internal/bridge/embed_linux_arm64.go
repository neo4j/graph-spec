//go:build linux && arm64 && !graphspec_noembed

package bridge

import _ "embed"

//go:embed lib/linux-arm64/libgraphdatamodel.so
var embeddedLib []byte
