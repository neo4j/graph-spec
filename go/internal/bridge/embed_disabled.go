//go:build graphspec_noembed || (!(linux && amd64) && !(linux && arm64) && !(darwin && arm64))

package bridge

// embeddedLib is nil when embedding is disabled via the `graphspec_noembed` build
// tag, or on a platform without a bundled library.
var embeddedLib []byte
