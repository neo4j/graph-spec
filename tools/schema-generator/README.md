# schema-generator

A tool that generates Go types from a JSON Schema.

_Note: this tool currently does not support all possible JSON schema features. It supports the subset of JSON schema 
that kotlinx-schema emits. Anything outside that subset is rejected with the JSON pointer of the offending schema._  

## Usage

```shell
go -C tools/schema-generator run . --schema "$PWD/go/spec.json" --out "$PWD/go/model/model.go" --package model
```

| Flag        | Description                                                           |
|-------------|-----------------------------------------------------------------------|
| `--schema`  | Path of the JSON Schema to read. Required.                            |
| `--out`     | Path of the Go file to write. Required.                               |
| `--package` | Name of the Go package. Defaults to the name of the output directory. |

## Supported Schemas

The supported keywords are `type`, `properties`, `required`, `additionalProperties`, `items`, `enum`, `const`, `oneOf`,
`$ref` and `$defs`. `description` becomes a doc comment, the root's `title` names the root type, and `minimum` is
ignored. `$schema`, `$id` and `$defs` are only allowed on the root. Any other keyword is an error.

The root and every entry of `$defs` must be one of:
- an object with `properties`, which becomes a struct
- a string `enum`, which becomes a named `string` type with a constant per value
- a `oneOf` of `$ref`s to object definitions, which becomes a union

Objects, enums and unions cannot be declared inline. Properties, list items and map values refer to them with a `$ref`
of the form `#/$defs/Name`.

### Translations

| Schema                                                           | Go                                   |
|------------------------------------------------------------------|--------------------------------------|
| `string`, `integer`, `number`, `boolean`                         | `string`, `int64`, `float64`, `bool` |
| `{}`                                                             | `any`                                |
| `array` with `items`                                             | `[]T`                                |
| `object` with an `additionalProperties` schema                   | `map[string]T`                       |
| `$ref`                                                           | the named type                       |

Definitions and properties are sorted by name, so the output does not depend on the order of the schema.

### Optional & Nullable Values

A value is nullable when its schema is `"type": [T, "null"]`, or `"oneOf": [{"type": "null"}, {"$ref": ...}]` for a
reference.

| Property                  | Go field                                 |
|---------------------------|------------------------------------------|
| required and not nullable | `T`                                      |
| required and nullable     | `*T`, encodes nil as `null`              |
| not required              | `*T` with `omitempty`, nil leaves it out |

Nullable list items and map values are pointers too (`[]*T`). Fields of type `any` are never pointers, since `any` can
already hold nil.

### Unions

A `oneOf` of two or more `$ref`s to object definitions becomes a discriminated union. The discriminator is the one
property of the first variant with a `const`, which is how kotlinx-schema marks the class discriminator. Every variant
must declare it with a distinct `const`.

```go
type Shape struct {
	ShapeVariant
}

type ShapeVariant interface {
	ShapeType() string
	isShape()
}

type ShapeCircle struct {
	Radius float64 `json:"radius"`
}
```

- The union is a struct embedding the variant interface, so it can be decoded. Its `UnmarshalJSON` reads the
  discriminator and decodes the matching variant. Missing or unknown values are errors.
- Variants are stored as values, so a type switch matches `ShapeCircle`, not `*ShapeCircle`.
- Variants have no field for the discriminator. Their `MarshalJSON` writes it, so it can never be missing or wrong.
- A nil variant encodes as `null`. A nullable union field is `*Shape`, following the rules above.

### Naming

Names are converted to CamelCase following Go conventions. Words are split at characters that cannot appear in a Go
identifier and at changes of case, and common initialisms are upper case. So `from_node` becomes `FromNode`,
`LIST<STRING>` becomes `ListString` and `user_id` becomes `UserID`. JSON tags and enum values keep the original
names. A name that would start with a digit is prefixed with `X`.

A definition that is only ever used as a variant of one union is prefixed with the union's name, unless its name
already starts or ends with it. For example, `CIRCLE` in `Shape` becomes `ShapeCircle` and `RectangleShape` stays as
it is.

When two schema names would produce the same Go identifier, such as `Node` and `NODE`, generation fails and names
both.

## Development

```shell
go test ./...                         # unit, golden and generated-code tests
go test ./internal/generator -update  # rewrite the golden files after an intended change to the output
```

- `internal/generator/testdata/golden/<case>.json` is generated and compared with `<case>.go.golden`.
- Every golden file is compiled and vetted in a temporary module.
- `testdata/runtime/<case>_test.go` runs against the package generated from the golden case of the same name.
  These tests check encoding behaviour.

The generator works in stages:

1. `internal/schema` loads the supported keywords.
2. `internal/model` builds a language-neutral model of declarations and types.
3. `internal/golang` derives and checks the Go names, then writes the source.
