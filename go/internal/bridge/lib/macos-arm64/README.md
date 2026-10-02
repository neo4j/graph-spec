# macOS bridge library (libontologygraphmodel.dylib)

This directory holds `libontologygraphmodel.dylib`, the Kotlin/Native shared
library embedded by the Go bridge on darwin/arm64
(`//go:embed lib/macos-arm64/libontologygraphmodel.dylib`).

**The dylib is currently not checked in.** Building it
(`./gradlew linkReleaseSharedMacosArm64`) requires full Xcode —
`xcodebuild` fails on hosts with only the Command Line Tools installed
(xcode-select pointing at `/Library/Developer/CommandLineTools`).

The dylib is produced:

- in CI/release builds — the `native-libs` job in `.github/workflows/release.yaml`
  runs on `macos-latest` (full Xcode) and rebuilds it on every release, or
- locally on a machine with full Xcode, via
  `./go/scripts/generate-kotlin-native-libs.sh`

On a CLT-only host, use the `ontologygraphspec_noembed` build tag
(`go build -tags ontologygraphspec_noembed ./...`) or point
`ONTOLOGYGRAPHMODEL_LIB_PATH` at an externally built dylib.
