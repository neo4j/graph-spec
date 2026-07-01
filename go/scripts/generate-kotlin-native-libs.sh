#!/bin/bash

# Ensure we are in the root of the repo
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

echo "Starting Kotlin/Native lib generation..."

./gradlew linkReleaseSharedMacosArm64 linkReleaseSharedLinuxX64 linkReleaseSharedLinuxArm64 && \
cp build/bin/macosArm64/releaseShared/libgraphdatamodel.dylib go/internal/bridge/lib/macos-arm64/ && \
cp build/bin/linuxX64/releaseShared/libgraphdatamodel.so go/internal/bridge/lib/linux-amd64/ && \
cp build/bin/linuxArm64/releaseShared/libgraphdatamodel.so go/internal/bridge/lib/linux-arm64/

echo "✓ Updated Kotlin/Native shared libs for Go library"
