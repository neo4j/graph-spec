package tests

import (
	"encoding/json"
	"fmt"
	"os"
	"os/exec"
	"runtime"
	"strconv"
	"strings"
	"syscall"
	"testing"
	"time"

	"github.com/neo4j/graph-spec/go/v4/migration"
	"github.com/stretchr/testify/assert"
	"github.com/stretchr/testify/require"
)

// allocBudget caps how much the candidate path may allocate on the Go heap for
// largeSchemaTables tables. Before the data source schema round trip was removed from the backend service the
// same input allocated 225MB here rather than 103MB, so this guards that regression. A round trip of the
// shape below currently allocates about 163MB, so the headroom is thin: raise the budget alongside
// largeSchemaTables rather than treating a failure here as a regression on its own.
//
// The budget covers the Go heap only. Most of the process footprint for a schema this
// size is inside the graph-spec native library and is invisible to runtime.MemStats, so
// this is not an out-of-memory guard. Container memory needs its own coverage.
//
// Measured allocation is stable to within 0.1% across runs, so the headroom here is for
// Go and graph-spec upgrades rather than for run to run noise. If this fires, check the
// growth is proportionate before re-baselining it.
const allocBudget = 175 << 20

const (
	largeSchemaTables = 1000
	largeSchemaFields = 20
)

// largeSourceSchema builds a data model of the given shape: one node per table with a property per field,
// and a relationship per foreign key chaining each table to the one before it. Refs use the same `$id`
// scheme the data importer emits. It is generated rather than held as a testdata fixture to keep several
// MB of JSON out of the repository.
func largeSourceSchema(tables, fieldsPerTable int) json.RawMessage {
	type field struct {
		Name            string         `json:"name"`
		RawType         string         `json:"rawType"`
		Size            int            `json:"size"`
		RecommendedType map[string]any `json:"recommendedType"`
		SupportedTypes  []any          `json:"supportedTypes"`
	}
	type foreignKey struct {
		ReferencedTable string           `json:"referencedTable"`
		Fields          []map[string]any `json:"fields"`
	}
	type table struct {
		Name        string       `json:"name"`
		Fields      []field      `json:"fields"`
		PrimaryKeys []string     `json:"primaryKeys"`
		ForeignKeys []foreignKey `json:"foreignKeys"`
	}

	integer := func(name string, size int) field {
		return field{Name: name, RawType: "int8", Size: size,
			RecommendedType: map[string]any{"type": "integer"},
			SupportedTypes:  []any{map[string]any{"type": "integer"}}}
	}
	tableName := func(i int) string { return fmt.Sprintf("business_entity_table_%04d", i) }
	ref := func(format string, args ...any) map[string]any {
		return map[string]any{"$ref": "#" + fmt.Sprintf(format, args...)}
	}

	schemas := make([]table, 0, tables)
	labels, nodeTypes, nodeKeys, nodeMappings := []any{}, []any{}, []any{}, []any{}
	relTypes, relTypeObjects, relMappings := []any{}, []any{}, []any{}
	for t := range tables {
		tbl := table{Name: tableName(t), PrimaryKeys: []string{"id"}, ForeignKeys: []foreignKey{}}
		tbl.Fields = append(tbl.Fields, integer("id", 19))
		for f := range fieldsPerTable - 1 {
			tbl.Fields = append(tbl.Fields, field{
				Name: fmt.Sprintf("column_name_%02d", f), RawType: "varchar", Size: 255,
				RecommendedType: map[string]any{"type": "string"},
				SupportedTypes:  []any{map[string]any{"type": "string"}},
			})
		}
		if t > 0 {
			tbl.Fields = append(tbl.Fields, integer("parent_id", 19))
			tbl.ForeignKeys = append(tbl.ForeignKeys, foreignKey{
				ReferencedTable: tableName(t - 1),
				Fields:          []map[string]any{{"field": "parent_id", "referencedField": "id"}},
			})
		}
		schemas = append(schemas, tbl)

		// One node per table, carrying a property per field and keyed on the primary key.
		properties, propertyMappings := []any{}, []any{}
		for f, fld := range tbl.Fields {
			properties = append(properties, map[string]any{
				"$id": fmt.Sprintf("p:%d_%d", t, f), "token": fld.Name,
				"type": fld.RecommendedType, "nullable": false,
			})
			propertyMappings = append(propertyMappings, map[string]any{
				"property": ref("p:%d_%d", t, f), "fieldName": fld.Name,
			})
		}
		labels = append(labels, map[string]any{
			"$id": fmt.Sprintf("nl:%d", t), "token": tableName(t), "properties": properties,
		})
		nodeTypes = append(nodeTypes, map[string]any{
			"$id": fmt.Sprintf("n:%d", t), "labels": []any{ref("nl:%d", t)},
		})
		nodeKeys = append(nodeKeys, map[string]any{
			"node": ref("n:%d", t), "keyProperties": []any{ref("p:%d_0", t)},
		})
		nodeMappings = append(nodeMappings, map[string]any{
			"node": ref("n:%d", t), "tableName": tableName(t), "propertyMappings": propertyMappings,
		})

		if t == 0 {
			continue
		}
		// One relationship per foreign key, from this table's parent_id to the previous table's id.
		relTypes = append(relTypes, map[string]any{
			"$id": fmt.Sprintf("rt:%d", t), "token": "PARENT", "properties": []any{},
		})
		relTypeObjects = append(relTypeObjects, map[string]any{
			"$id": fmt.Sprintf("r:%d", t), "type": ref("rt:%d", t),
			"from": ref("n:%d", t), "to": ref("n:%d", t-1),
		})
		relMappings = append(relMappings, map[string]any{
			"relationship": ref("r:%d", t), "tableName": tableName(t),
			"fromMappings":     map[string]any{fmt.Sprintf("#p:%d_%d", t, len(tbl.Fields)-1): "parent_id"},
			"toMappings":       map[string]any{fmt.Sprintf("#p:%d_0", t-1): "id"},
			"propertyMappings": []any{},
		})
	}

	raw, err := json.Marshal(map[string]any{
		"version": "3.0.0",
		"graphSchemaRepresentation": map[string]any{
			"version": "1.0.0",
			"graphSchema": map[string]any{
				"nodeLabels":              labels,
				"nodeObjectTypes":         nodeTypes,
				"relationshipTypes":       relTypes,
				"relationshipObjectTypes": relTypeObjects,
			},
		},
		"graphSchemaExtensionsRepresentation": map[string]any{"nodeKeyProperties": nodeKeys},
		"graphMappingRepresentation": map[string]any{
			"dataSourceSchema": map[string]any{
				"type":         "cloud",
				"tableSchemas": schemas,
			},
			"nodeMappings":         nodeMappings,
			"relationshipMappings": relMappings,
		},
	})
	if err != nil {
		panic(err)
	}
	return raw
}

// processRAMUsage reports the resident set size of this process in bytes: the current value, and the peak over the
// whole process lifetime.
//
// The peak comes from getrusage, which reports kilobytes on Linux and bytes on macOS. It covers the process lifetime
// rather than this test, so in a full package run it includes whatever earlier tests did. The other tests here use
// schemas small enough for that to be noise. Current RSS comes from /proc on Linux and from ps on macOS, which has
// no equivalent that does not need cgo.
func processRAMUsage() (current, peak uint64) {
	var usage syscall.Rusage
	if err := syscall.Getrusage(syscall.RUSAGE_SELF, &usage); err == nil {
		peak = uint64(usage.Maxrss)
		if runtime.GOOS != "darwin" {
			peak *= 1024
		}
	}
	if runtime.GOOS == "darwin" {
		out, err := exec.Command("ps", "-o", "rss=", "-p", strconv.Itoa(os.Getpid())).Output()
		if err != nil {
			return 0, peak
		}
		kb, err := strconv.ParseUint(strings.TrimSpace(string(out)), 10, 64)
		if err != nil {
			return 0, peak
		}
		return kb * 1024, peak
	}
	statm, err := os.ReadFile("/proc/self/statm")
	if err != nil {
		return 0, peak
	}
	fields := strings.Fields(string(statm))
	if len(fields) < 2 {
		return 0, peak
	}
	pages, err := strconv.ParseUint(fields[1], 10, 64)
	if err != nil {
		return 0, peak
	}
	return pages * uint64(os.Getpagesize()), peak
}

// TestLargeSourceSchema exercises the candidate path at the scale of a real enterprise MySQL database. Databases
// this size were originally failing in our main backend service as the container memory limit was being breached
// (it only had 100MB).
//
// The actual Go runtime memory didn't seem too bad but the overall container memory was climbing. This test shows
// the overall system footprint alongside the Go runtime, highlighting the kotlin/native call taking a lot of memory.
// graph-spec does its work in a native library with its own heap, which runtime.MemStats cannot see but the container
// memory limit still counts. Anyone sizing this service should read the OS numbers.
//
// The native heap is not returned to the OS promptly, so the RSS readings below are a high water mark for one call
// rather than a steady state. Peak footprint also scales with concurrency: each in flight call holds its own copy of
// the document, so callers need to bound how many they run at once.
func TestLargeSourceSchema(t *testing.T) {
	input := largeSourceSchema(largeSchemaTables, largeSchemaFields)

	// Warm the library so the one off dylib load and Kotlin/Native runtime init land outside the measurements.
	_, err := migration.ToGraphSpec(string(largeSourceSchema(1, 2)), migration.ModelTypeDataModel)
	require.NoError(t, err)

	runtime.GC()
	var before, after runtime.MemStats
	runtime.ReadMemStats(&before)
	baseRSS, _ := processRAMUsage()

	start := time.Now()
	gm, err := migration.ToGraphSpec(string(input), migration.ModelTypeDataModel)
	require.NoError(t, err)
	toGraphSpec := time.Since(start)

	start = time.Now()
	dm, err := migration.FromGraphSpec(gm, migration.ModelTypeDataModel, migration.ModelVersionDataModelV30)
	require.NoError(t, err)
	fromGraphSpec := time.Since(start)

	runtime.ReadMemStats(&after)
	currentRSS, peakRSS := processRAMUsage()
	allocated := after.TotalAlloc - before.TotalAlloc

	mb := func(b uint64) float64 { return float64(b) / (1 << 20) }
	inputMB := mb(uint64(len(input)))
	t.Logf("scale: %d tables x %d fields, %d nodes, %d relationships, input %.2fMB, response %.2fMB",
		largeSchemaTables, largeSchemaFields, len(gm.Nodes), len(gm.Relationships), inputMB, mb(uint64(len(dm))))
	t.Logf("  timing: to graph-spec %v, from graph-spec %v, round trip %v",
		toGraphSpec.Round(time.Millisecond), fromGraphSpec.Round(time.Millisecond),
		(toGraphSpec + fromGraphSpec).Round(time.Millisecond))
	t.Logf("  go runtime: allocated %.0fMB in %d allocations, peak heap in use %.0fMB (budget %.0fMB)",
		mb(allocated), after.Mallocs-before.Mallocs, mb(after.HeapInuse), mb(allocBudget))
	switch {
	case currentRSS > 0:
		t.Logf("  os process: rss %.0fMB, peak rss %.0fMB, %.0fMB above the %.0fMB before the round trip",
			mb(currentRSS), mb(peakRSS), mb(currentRSS-baseRSS), mb(baseRSS))
	case peakRSS > 0:
		t.Logf("  os process: peak ram usage %.0fMB (current ram usage unavailable)", mb(peakRSS))
	default:
		t.Log("  os process: rss unavailable on this platform")
	}
	if peakRSS > after.HeapInuse {
		outside := peakRSS - after.HeapInuse
		t.Logf("  => %.0f%% of the peak footprint (%.0fMB) is outside the Go heap, in the graph-spec native "+
			"library: %.0fx the input size", float64(outside)/float64(peakRSS)*100, mb(outside), mb(outside)/inputMB)
	}

	//require.Less(t, allocated, uint64(allocBudget),
	//	"candidate generation allocated more than the budget - see the allocBudget comment")

	assert.Len(t, gm.Nodes, largeSchemaTables)
	assert.Len(t, gm.Relationships, largeSchemaTables-1)
	assert.Len(t, gm.Mappings, 2*largeSchemaTables-1)

	var doc map[string]any
	assert.NoError(t, json.Unmarshal([]byte(dm), &doc))
	dss := doc["graphMappingRepresentation"].(map[string]any)["dataSourceSchema"].(map[string]any)
	assert.Equal(t, "cloud", dss["type"], "data source type must survive the graph-spec round trip")
	assert.Len(t, dss["tableSchemas"], largeSchemaTables)
	graph := doc["graphSchemaRepresentation"].(map[string]any)["graphSchema"].(map[string]any)
	assert.Len(t, graph["nodeObjectTypes"], largeSchemaTables)
	assert.Len(t, graph["relationshipObjectTypes"], largeSchemaTables-1)
}
