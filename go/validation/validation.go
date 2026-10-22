package validation

import (
	"encoding/json"
	"fmt"

	"github.com/neo4j/ontology-graph-spec/go/internal/bridge"
	"github.com/neo4j/ontology-graph-spec/go/model"
)

type Issue struct {
	Code    string            `json:"code"`
	Message string            `json:"message"`
	Path    string            `json:"path"`
	Details map[string]string `json:"details"`
}

type Set string

const (
	SetCore             Set = "core"
	SetLabelled         Set = "labelled"
	SetConsistentSchema Set = "consistent_schema"
	SetMappable         Set = "mappable"
	SetBulkImportable   Set = "bulk_importable"
	SetWellConstrained  Set = "well_constrained"
	SetAll              Set = "all"
)

func Validate(model model.GraphModel, validationSet Set) ([]Issue, error) {
	bytes, err := json.Marshal(model)
	if err != nil {
		return nil, err
	}
	res, err := bridge.Call(bridge.Validate, bytes, string(validationSet))
	if err != nil {
		return nil, err
	}
	var issues []Issue
	if err := json.Unmarshal(res, &issues); err != nil {
		return nil, fmt.Errorf("error parsing validation issues: %w", err)
	}
	return issues, nil
}
