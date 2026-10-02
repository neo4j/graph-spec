#!/bin/bash
set -euo pipefail

# Ensure we are in the root of the repo
REPO_ROOT=$(git rev-parse --show-toplevel)
cd "$REPO_ROOT"

INPUT_SPEC="ontology-spec.schema.json"
TEMP_SPEC="spec-sanitised.json"
OUTPUT_PACKAGE="model"
OUTPUT_FILE="model"

echo "Starting Go model generation..."

# The v1 schema lives at the repo root; no Gradle schema-generation step
# remains. The sanitised temp copy carries two generation-only tweaks — the
# schema itself (spec surface) is never touched:
#
#  1. title pin: schemancer names the root Go type from the schema title;
#     pinning "GraphModel" keeps the top-level type consumer-stable
#     (ADR-0006). v1 has no bracketed enum values (type tokens are plain
#     strings), so the 4.0.0 pipeline's perl bracket dance is gone.
#  2. format:"uri" demotion: schemancer maps format:"uri" to net/url.URL,
#     which encoding/json cannot round-trip (unmarshalling a JSON string
#     into url.URL fails; url.URL has no Text(Un)Marshaler). The Kotlin
#     model types reference/$schema as String and URI-format validation is
#     Kotlin-side, so the format marker is dropped here and the Go fields
#     stay plain strings.
cd "$REPO_ROOT/go"
trap 'rm -f "$TEMP_SPEC"' EXIT
jq '
  .title = "GraphModel"
  | walk(if type == "object" and .format == "uri" then del(.format) else . end)
' "$REPO_ROOT/$INPUT_SPEC" > "$TEMP_SPEC"
echo "✓ JSON spec sanitised"

# schemancer generates the Go types. Pinned to v1.2.0: that version was
# probe-verified to parse the v1 schema (JSON Schema draft 2020-12,
# $defs/$ref/pattern/boolean schemas) and emit compilable Go.
SCHEMANCER_BIN=$(go env GOPATH)/bin/schemancer
if ! command -v "$SCHEMANCER_BIN" &> /dev/null; then
    echo "schemancer not found, installing..."
    go install github.com/Southclaws/schemancer@v1.2.0
fi
# Generate Go types
"$SCHEMANCER_BIN" "$TEMP_SPEC" golang "$OUTPUT_FILE" --package $OUTPUT_PACKAGE
echo "✓ Go models generated"

# schemancer drops `additionalProperties: true`: ExtensionsMap would lose
# named neo4j:* keys, Tool its owner-defined fields (cypher, url, ...), and
# Extension the custom envelope's unknown fields — breaking the format's
# "carried untouched, never rejected" invariant. Inject an unexported extras
# field into the three open-surface structs; the hand-written companions in
# go/model/extras.go (not generated, survives regen) implement the JSON
# round-trip on top of it. The guard fails the run if schemancer's output
# ever stops matching the injection anchors.
perl -pi -e '
  if (!$imported && /^package model$/) { $_ .= "\nimport \"encoding/json\"\n"; $imported = 1 }
  if (/^type (?:ExtensionsMap|Tool|Extension) struct \{$/) {
    $bt = chr(96);
    $_ .= "\textra map[string]json.RawMessage ${bt}json:\"-\"${bt}\n";
    $injected++;
  }
  END { die "extra-field injection missed its anchors (got " . ($injected // 0) . ", want 3)\n" unless ($injected // 0) == 3 }
' "$OUTPUT_PACKAGE/$OUTPUT_FILE.go"
echo "✓ Open-surface extras fields injected"

# Final Go formatting
go fmt "$OUTPUT_PACKAGE/$OUTPUT_FILE.go"
echo "✓ Formatting and cleanup completed"
