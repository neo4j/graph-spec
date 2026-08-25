package main

import (
	"fmt"
	"os"
	"runtime"
	"syscall"
	"time"

	"github.com/neo4j/graph-spec/go/v4/internal/bridge"
)

func peak() float64 {
	var u syscall.Rusage
	syscall.Getrusage(syscall.RUSAGE_SELF, &u)
	b := float64(u.Maxrss)
	if runtime.GOOS != "darwin" {
		b *= 1024
	}
	return b / (1 << 20)
}

func main() {
	stage := os.Args[1]
	dm, _ := os.ReadFile("/tmp/large.json")
	gs, _ := os.ReadFile("/tmp/large-gs.json")

	// Warm the runtime so dylib load and init land outside the reading.
	small, _ := os.ReadFile("/tmp/small.json")
	bridge.Call(bridge.Migrate, string(small), "data_model", "graph_spec", "4.0.0")
	base := peak()

	start := time.Now()
	var out []byte
	var err error
	switch stage {
	case "warm":
	case "decode": // empty migration path: decode + encode only
		out, err = bridge.Call(bridge.Migrate, string(gs), "graph_spec", "graph_spec", "4.0.0")
	case "validate": // decode plus typed model build, near zero output
		out, err = bridge.Call(bridge.Validate, string(gs))
	case "forward":
		out, err = bridge.Call(bridge.Migrate, string(dm), "data_model", "graph_spec", "4.0.0")
	case "reverse":
		out, err = bridge.Call(bridge.Migrate, string(gs), "graph_spec", "data_model", "3.0.0")
	case "roundtrip":
		out, err = bridge.Call(bridge.Migrate, string(dm), "data_model", "graph_spec", "4.0.0")
		if err == nil {
			out, err = bridge.Call(bridge.Migrate, string(out), "graph_spec", "data_model", "3.0.0")
		}
	case "dumpgs":
		out, err = bridge.Call(bridge.Migrate, string(dm), "data_model", "graph_spec", "4.0.0")
		if err == nil {
			os.WriteFile("/tmp/large-gs.json", out, 0o644)
		}
	}
	if err != nil {
		fmt.Println("error:", err)
		os.Exit(1)
	}
	fmt.Printf("%-10s in %6.2fMB out %6.2fMB  time %8v  peak %7.0fMB (base %.0fMB)\n",
		stage, float64(len(dm))/(1<<20), float64(len(out))/(1<<20),
		time.Since(start).Round(time.Millisecond), peak(), base)
}
