#!/bin/bash
set -euo pipefail

# Ensure we are in the root of the repo
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

INPUT_SPEC="$REPO_ROOT/ontology-graph-spec.schema.json"
OUTPUT_PACKAGE="model"
OUTPUT_FILE="$REPO_ROOT/go/$OUTPUT_PACKAGE/model.go"

echo "Starting Go model generation..."

./gradlew :generateGraphModelJsonSchema
echo "✓ Generated JSON spec"

# Generate Go types
go -C tools/schema-generator run . --schema "$INPUT_SPEC" --out "$OUTPUT_FILE" --package "$OUTPUT_PACKAGE"
echo "✓ Go models generated"
