package generator

import (
    "errors"
    "flag"
    "io/fs"
    "os"
    "os/exec"
    "path/filepath"
    "strings"
    "testing"

    "github.com/stretchr/testify/require"
)

var update = flag.Bool("update", false, "rewrite the golden files")

func TestGolden(t *testing.T) {
    cases, err := filepath.Glob("testdata/golden/*.json")
    require.NoError(t, err)

    for _, schemaPath := range cases {
        name := strings.TrimSuffix(filepath.Base(schemaPath), ".json")
        t.Run(name, func(t *testing.T) {
            data, err := os.ReadFile(schemaPath)
            require.NoError(t, err)

            got, err := Generate(data, Options{Package: "model", SchemaName: filepath.Base(schemaPath)})
            require.NoError(t, err)

            goldenPath := strings.TrimSuffix(schemaPath, ".json") + ".go.golden"
            if *update {
                require.NoError(t, os.WriteFile(goldenPath, got, 0o644))
                return
            }
            want, err := os.ReadFile(goldenPath)
            require.NoError(t, err)
            require.Equal(t, string(want), string(got), "run the tests with -update to accept the change")
        })
    }
}

// TestGeneratedCode runs go vet and go test on the golden file
func TestGeneratedCode(t *testing.T) {
    if testing.Short() {
        t.Skip("runs the go tool")
    }
    goTool, err := exec.LookPath("go")
    if err != nil {
        t.Skip("go tool not found")
    }

    dir := t.TempDir()
    write := func(path string, data []byte) {
        t.Helper()
        require.NoError(t, os.MkdirAll(filepath.Dir(path), 0o755))
        require.NoError(t, os.WriteFile(path, data, 0o644))
    }
    write(filepath.Join(dir, "go.mod"), []byte("module generated\n\ngo 1.25\n"))

    goldens, err := filepath.Glob("testdata/golden/*.go.golden")
    require.NoError(t, err)

    for _, golden := range goldens {
        name := strings.TrimSuffix(filepath.Base(golden), ".go.golden")
        src, err := os.ReadFile(golden)
        require.NoError(t, err)
        write(filepath.Join(dir, name, "model.go"), src)

        test, err := os.ReadFile(filepath.Join("testdata", "runtime", name+"_test.go"))
        if errors.Is(err, fs.ErrNotExist) {
            continue
        }
        require.NoError(t, err)
        write(filepath.Join(dir, name, name+"_test.go"), test)
    }

    for _, args := range [][]string{{"vet", "./..."}, {"test", "./..."}} {
        cmd := exec.Command(goTool, args...)
        cmd.Dir = dir
        out, err := cmd.CombinedOutput()
        require.NoError(t, err, "go %s failed:\n%s", strings.Join(args, " "), out)
    }
}

func TestInvalidPackageName(t *testing.T) {
    _, err := Generate([]byte(`{}`), Options{Package: "func"})
    require.Error(t, err)
}
