# Go GraphSpec

This package contains the Go GraphSpec Library. It consists of:

* **Go-native GraphSpec types** - These are automatically generated from the Kotlin source of truth models
* **Methods for migration and validation** - Enables migration between older model types and latest GraphSpec model, as 
    well as validation of a given GraphSpec model. These methods call into the Kotlin source of truth methods via a
    Kotlin/Native library.

## Usage

This library can be imported into your Go project via the standard `go get`: 

```shell
# Use SSH for git to avoid having to authenticate when fetching this library
git config --global url."git@github.com:".insteadOf "https://github.com/" 

# Fetch Go library
go get github.com/neo4j/graph-spec/go/vX@vX.Y.Z
```

> [!NOTE]
> Because it is a glibc-based Kotlin/Native library, the runtime must provide `glibc` and `libstdc++`.

## How the Kotlin/Native library is loaded

The migration and validation methods call into a Kotlin/Native shared library
(`libgraphdatamodel.so` on Linux, `libgraphdatamodel.dylib` on macOS) at runtime via
[purego](https://github.com/ebitengine/purego). 

By default, the library is automatically embedded in the binary for supported platforms (`linux/amd64`, `linux/arm64`, 
`darwin/arm64`) and loaded on first use, so nothing extra is needed after `go get` on any of those platforms. 

### Using Without Embedding

Automatic embedding can be disabled if needed (e.g. if the runtime is locked down and extracting and loading the 
embedded native library is not possible). Instead of automatic embedding, the relevant shared library can be hosted
in a desired location available to the Go binary at runtime, and pointed to via the `GRAPHDATAMODEL_LIB_PATH` env var.
If this env var is set, the shared library will be loaded from there. The shared libraries are stored in this repo 
under the `go/internal/bridge/lib/` directory.

Optionally, if not using automatic embedding, a Go binary without the bundled library can be built from source with the 
`graphspec_noembed` tag:

```
go build -tags graphspec_noembed ./...
```

> [!NOTE]
> The shared native lib must be provided either via automatic embedding or the `GRAPHDATAMODEL_LIB_PATH` var. If not 
> available, e.g. for an unsupported platform and no `GRAPHDATAMODEL_LIB_PATH`, an error will be returned when run.

## Development

### Building and testing

The Go library can be built and tested as normal:

```shell
go test ./...
```

Tests load the shared library exactly as consumers do. With embedding on (the default) nothing
extra is needed. If building or testing with `graphspec_noembed`, set `GRAPHDATAMODEL_LIB_PATH`
first (see [Using Without Embedding](#using-without-embedding)).

### Generating Kotlin/Native libraries

```shell
./go/scripts/generate-kotlin-native-libs.sh
```

This rebuilds the shared libraries for all supported platforms from the Kotlin source and copies them into 
`internal/bridge/lib/`. This should be done when the underlying Kotlin logic materially changes. 

The regenerated `.so`/`.dylib` files are checked in to git history. They are what `//go:embed` bundles, so a
published tag whose module omits them will fail to build for consumers.

### Generating Go types

```shell
./go/scripts/generate-go-models.sh
```

The GraphModel Go struct and associated structs are automatically generated from the Kotlin source of truth. They live
in the `go/models` package. The pipeline for generating these types is:

_Kotlin_ → _JSON Schema_ → _Go_

Some temporary sanitising is needed to ensure there are no issues relating to Go enum variable names when generating
Neo4jTypes. The script handles this.

> [!NOTE]
> The deployment pipeline automatically checks the Go types are up-to-date. If you receive a pipeline failure relating
> to out-of-date types then rerun the above script.

#### Generating JSON schema

If you want to update the JSON schema separately you can run the individual Gradle command from the repo root:

```shell
./gradlew :generateGraphModelJsonSchema
```

JSON Schema is generated automatically from the Kotlin source of truth via the `kotlinx-schema` library.

#### Go type generation

The Go structs are generated using the [schemancer library](https://github.com/Southclaws/schemancer). This library was chosen as it correctly handled 
polymorphism (e.g. mapping types). Other type radiation libraries such as modelina and quicktype condense down all 
polymorphic types into a single super struct.
