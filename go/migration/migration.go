package migration

import (
	"encoding/json"
	"fmt"

	"github.com/neo4j/graph-spec/go/internal/bridge"
	"github.com/neo4j/graph-spec/go/model"
)

type ModelType string
type ModelVersion string

// The token contract mirrors the Kotlin side (src/commonMain/kotlin/model/Type.kt and
// model/Version.kt): the rebuilt bridge library supports exactly one migration, the
// one-way graph spec 4.0.0 -> ontology spec 1.0.0 converter (ADR-0008). The SpecV4
// tokens are the converter's INPUT contract only — this SDK never writes them.
const (
	ModelTypeSpecV4       ModelType = "graph_spec"
	ModelTypeOntologySpec ModelType = "ontology_spec"

	ModelVersionSpecV4             ModelVersion = "4.0.0"
	ModelVersionOntologySpecLatest ModelVersion = "1.0.0"
)

// ToOntologySpec returns the provided [jsonModel] migrated to the latest ontology model representation.
// The input model should be a JSON string matching the provided [modelType] — in v1 the only
// supported input is a graph spec 4.0.0 document ([ModelTypeSpecV4]); pre-4.0.0 inputs
// (data model 2.x/3.0, import_spec) are rejected by the library (ADR-0008 §10). If the
// migration path from [modelType] to ontology model is not supported or the input model is
// malformed an error will be returned.
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
// model is a JSON string. The v1 converter is one-way (ADR-0008): no migration path out of
// ontology-spec exists, so every target is rejected by the library with an
// unsupported-migration error.
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
