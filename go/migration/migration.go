package migration

import (
	"encoding/json"
	"fmt"

	"github.com/neo4j/graph-spec/go/internal/bridge"
	"github.com/neo4j/graph-spec/go/model"
)

type ModelType string
type ModelVersion string

const (
	ModelTypeDataModel        ModelType = "data_model"
	ModelTypeDataModelWrapped ModelType = "data_model_wrapped"
	ModelTypeImportSpec       ModelType = "import_spec"
	ModelTypeOntologySpec     ModelType = "graph_spec"

	ModelVersionOntologySpecLatest ModelVersion = "4.0.0"
	ModelVersionDataModelV23       ModelVersion = "2.3.0"
	ModelVersionDataModelV24       ModelVersion = "2.4.0"
	ModelVersionDataModelV30       ModelVersion = "3.0.0"
	ModelVersionImportSpecV1       ModelVersion = "1.0.0"
)

// ToOntologySpec returns the provided [jsonModel] migrated to the latest ontology model representation.
// The input model should be a JSON string matching the provided [modelType]. If the migration path
// from [modelType] to ontology model is not supported or the input model is malformed an error will
// be returned.
func ToOntologySpec(jsonModel string, modelType ModelType) (model.GraphModel, error) {
	res, err := bridge.Call(bridge.Migrate, []byte(jsonModel), string(modelType), string(ModelTypeOntologySpec), string(ModelVersionOntologySpecLatest))
	if err != nil {
		return model.GraphModel{}, err
	}

	var graph model.GraphModel
	if err := json.Unmarshal(res, &graph); err != nil {
		return model.GraphModel{}, fmt.Errorf("failed to unmarshal into graph model: %s", err)
	}
	return graph, nil
}

// FromOntologySpec returns the provided ontology-spec [model] migrated to the target model. The returned
// model is a JSON string. If the migration path from ontology-spec to [targetType]:[targetVersion] is
// not supported or the input model is invalid an error will be returned.
func FromOntologySpec(model model.GraphModel, targetType ModelType, targetVersion ModelVersion) (string, error) {
	bytes, err := json.Marshal(model)
	if err != nil {
		return "", err
	}
	res, err := bridge.Call(bridge.Migrate, bytes, string(ModelTypeOntologySpec), string(targetType), string(targetVersion))
	if err != nil {
		return "", err
	}
	return string(res), nil
}
