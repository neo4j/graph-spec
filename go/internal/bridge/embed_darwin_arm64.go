//go:build darwin && arm64 && !ontologyspec_noembed

package bridge

import "embed"

// The macOS dylib is not checked in on CLT-only hosts (it requires full Xcode to
// build — see lib/macos-arm64/README.md), so the directory is embedded and the
// dylib looked up at init: an absent file means no embedded library, and the
// loader falls back to ONTOLOGYMODEL_LIB_PATH or the actionable load error.
//
//go:embed lib/macos-arm64
var macosLib embed.FS

var embeddedLib = func() []byte {
	b, _ := macosLib.ReadFile("lib/macos-arm64/libontologymodel.dylib")
	return b
}()
