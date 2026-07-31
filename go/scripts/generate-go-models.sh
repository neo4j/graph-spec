#!/bin/bash
set -euo pipefail

# Ensure we are in the root of the repo
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

INPUT_SPEC="spec.json"
TEMP_SPEC="spec-sanitised.json"
OUTPUT_PACKAGE="model"
OUTPUT_FILE="model"

echo "Starting Go model generation..."

./gradlew :generateGraphModelJsonSchema
echo "✓ Generated JSON spec"

# Create temporary json spec file that we can sanitise
cd "$REPO_ROOT/go"
cp "$INPUT_SPEC" "$TEMP_SPEC"
# Ensure "title" is present in the spec which is needed for Go generation of top-level model
jq '.title //= .["$id"]' "$TEMP_SPEC" > tmp.json && mv tmp.json "$TEMP_SPEC"
echo "✓ JSON spec sanitised"


if ! command -v schemancer &> /dev/null; then
    echo "schemancer not found, installing..."
    go install github.com/Southclaws/schemancer@latest
fi
# Generate Go types
SCHEMANCER_BIN=$(go env GOPATH)/bin/schemancer
"$SCHEMANCER_BIN" "$TEMP_SPEC" golang "$OUTPUT_FILE" --package $OUTPUT_PACKAGE
echo "✓ Go models generated"


# Final Go formatting
go fmt "$OUTPUT_PACKAGE/$OUTPUT_FILE.go"
# Cleanup temporary sanitised json spec file
rm spec-sanitised.json
echo "✓ Post-processing, formatting and cleanup completed"
