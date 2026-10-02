//go:build linux && amd64 && !ontologyspec_noembed

package bridge

import _ "embed"

//go:embed lib/linux-amd64/libontologymodel.so
var embeddedLib []byte
