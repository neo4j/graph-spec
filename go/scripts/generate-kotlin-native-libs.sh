#!/bin/bash

# Regenerate the Kotlin/Native shared libraries embedded by the Go bridge.
#
# Usage: ./go/scripts/generate-kotlin-native-libs.sh
#
# Builds the release shared libs for all three bridge targets via Gradle
# (requires JDK 17+, e.g. JAVA_HOME=$HOME/.local/share/jdks/temurin-17.jdk/Contents/Home)
# and copies them into go/internal/bridge/lib/:
#
#   build/bin/macosArm64/releaseShared/libontologygraphmodel.dylib -> go/internal/bridge/lib/macos-arm64/
#   build/bin/linuxX64/releaseShared/libontologygraphmodel.so     -> go/internal/bridge/lib/linux-amd64/
#   build/bin/linuxArm64/releaseShared/libontologygraphmodel.so   -> go/internal/bridge/lib/linux-arm64/
#
# NOTE: linkReleaseSharedMacosArm64 requires full Xcode (xcodebuild); it fails
# on hosts with only the Command Line Tools installed. On a CLT-only host,
# build the linux libs with:
#   ./gradlew linkReleaseSharedLinuxX64 linkReleaseSharedLinuxArm64
# and copy them per the paths above; the macOS dylib is produced in CI/release
# or on a machine with full Xcode.

# Ensure we are in the root of the repo
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

echo "Starting Kotlin/Native lib generation..."

./gradlew linkReleaseSharedMacosArm64 linkReleaseSharedLinuxX64 linkReleaseSharedLinuxArm64 && \
cp build/bin/macosArm64/releaseShared/libontologygraphmodel.dylib go/internal/bridge/lib/macos-arm64/ && \
cp build/bin/linuxX64/releaseShared/libontologygraphmodel.so go/internal/bridge/lib/linux-amd64/ && \
cp build/bin/linuxArm64/releaseShared/libontologygraphmodel.so go/internal/bridge/lib/linux-arm64/

echo "✓ Updated Kotlin/Native shared libs for Go library"
