# macOS bridge library (libontologymodel.dylib)

This directory holds `libontologymodel.dylib`, the Kotlin/Native shared
library embedded by the Go bridge on darwin/arm64
(`//go:embed lib/macos-arm64/libontologymodel.dylib`).

**The dylib is currently not checked in.** Building it
(`./gradlew linkReleaseSharedMacosArm64`) requires full Xcode —
`xcodebuild` fails on hosts with only the Command Line Tools installed
(xcode-select pointing at `/Library/Developer/CommandLineTools`).

The dylib is produced:

- in CI/release builds, or
- locally on a machine with full Xcode, via
  `./go/scripts/generate-kotlin-native-libs.sh`

On a CLT-only host, use the `ontologyspec_noembed` build tag
(`go build -tags ontologyspec_noembed ./...`) or point
`ONTOLOGYMODEL_LIB_PATH` at an externally built dylib.
