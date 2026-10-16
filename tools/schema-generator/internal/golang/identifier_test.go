package golang

import (
	"testing"

	"github.com/stretchr/testify/assert"
)

func TestIdentifier(t *testing.T) {
	tests := map[string]string{
		"fromNode":                  "FromNode",
		"from_node":                 "FromNode",
		"FROM-NODE":                 "FromNode",
		"PROPERTY_TYPE":             "PropertyType",
		"LIST<ANY>":                 "ListAny",
		"LOCAL DATETIME":            "LocalDatetime",
		"fulltext.default_analyzer": "FulltextDefaultAnalyzer",
		"spatial.wgs-84-3d.max":     "SpatialWgs843DMax",
		"Neo4jType":                 "Neo4jType",
		"INTEGER8":                  "Integer8",
		"Float32Max":                "Float32Max",
		"HTTPServer":                "HttpServer",
		"user_id":                   "UserID",
		"apiUrl":                    "APIURL",
		"uuid":                      "UUID",
		"3d":                        "X3D",
		"día":                       "Día",
		"<>":                        "",
		"":                          "",
	}
	for input, want := range tests {
		assert.Equal(t, want, identifier(input), "identifier(%q)", input)
	}
}
