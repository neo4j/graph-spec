// Package generator turns a JSON Schema into Go types.
package generator

import (
	"fmt"
	"go/token"

	"github.com/neo4j/graph-spec/tools/schema-generator/internal/golang"
	"github.com/neo4j/graph-spec/tools/schema-generator/internal/model"
	"github.com/neo4j/graph-spec/tools/schema-generator/internal/schema"
)

type Options struct {
	Package    string
	SchemaName string
}

func Generate(schemaJSON []byte, opts Options) ([]byte, error) {
	if !token.IsIdentifier(opts.Package) {
		return nil, fmt.Errorf("%q is not a valid Go package name", opts.Package)
	}
	doc, err := schema.Load(schemaJSON)
	if err != nil {
		return nil, err
	}
	m, err := model.Build(doc)
	if err != nil {
		return nil, err
	}
	return golang.Generate(m, golang.Options{Package: opts.Package, SchemaName: opts.SchemaName})
}
