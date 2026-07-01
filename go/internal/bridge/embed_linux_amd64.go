//go:build linux && amd64 && !graphspec_noembed

package bridge

import _ "embed"

//go:embed lib/linux-amd64/libgraphdatamodel.so
var embeddedLib []byte
