package golang

import (
	"slices"
	"strings"
	"unicode"
)

// initialisms are the words that should be written in upper case in generated code
var initialisms = []string{"API", "ID", "UUID", "URI", "URL"}

// identifier converts s to the Go-compatible name (e.g. "from_node" to "FromNode") and prepends digit-leading strings
// with an escape character
func identifier(s string) string {
	id := camelCase(s)
	if id != "" && unicode.IsDigit([]rune(id)[0]) {
		id = "X" + id
	}
	return id
}

// camelCase converts s to a camel-cased Go-compatible name
func camelCase(s string) string {
	var b strings.Builder
	for _, w := range splitWords(s) {
		if upper := strings.ToUpper(w); slices.Contains(initialisms, upper) {
			b.WriteString(upper)
			continue
		}
		// capitalise the first letter
		runes := []rune(strings.ToLower(w))
		for i, r := range runes {
			if unicode.IsLetter(r) {
				runes[i] = unicode.ToUpper(r)
				break
			}
		}
		b.WriteString(string(runes))
	}
	return b.String()
}

// splitWords splits s into words at characters that cannot appear in a Go identifier and at changes of case
func splitWords(s string) []string {
	var words []string
	runes := []rune(s)
	start := -1
	flush := func(end int) {
		if start >= 0 {
			words = append(words, string(runes[start:end]))
			start = -1
		}
	}
	for i, r := range runes {
		if !unicode.IsLetter(r) && !unicode.IsDigit(r) {
			flush(i)
			continue
		}
		if start >= 0 && boundary(runes, i) {
			flush(i)
		}
		if start < 0 {
			start = i
		}
	}
	flush(len(runes))
	return words
}

// boundary reports whether a new word starts at runes[i], given runes[i-1] is part of the current word.
func boundary(runes []rune, i int) bool {
	prev, cur := runes[i-1], runes[i]
	if !unicode.IsUpper(cur) {
		return false
	}
	if unicode.IsLower(prev) || unicode.IsDigit(prev) {
		return true
	}
	// The last capital of a run starts the next word when lower case follows, as in "HTTPServer".
	return unicode.IsUpper(prev) && i+1 < len(runes) && unicode.IsLower(runes[i+1])
}
