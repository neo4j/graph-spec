# Schema Generator

A tool that generates Go types from a JSON Schema.

Not all JSON schema features are supported. This tool currently supports the subset that kotlinx-schema emits. Anything 
outside that subset is rejected with the JSON pointer of the offending schema.

## Usage

```shell  
go -C tools/schema-generator run . --schema "$PWD/ontology-graph-spec.schema.json" --out "$PWD/go/model/model.go" --package model```  
```

| Flag        | Description                                                           |  
|-------------|-----------------------------------------------------------------------|  
| `--schema`  | Path of the JSON Schema to read. Required.                            |  
| `--out`     | Path of the Go file to write. Required.                               |  
| `--package` | Name of the Go package. Defaults to the name of the output directory. |  
  
## Support and Functionality  
  
### Supported Keywords
Supported keywords are: `type`, `properties`, `required`, `additionalProperties`, `items`, `enum`, `const`, `oneOf`,  
`$ref`, `$defs`, `description` and `title`.

Notes:
* `description` becomes a doc comment
* The root's `title` names the root type
* `minimum` is ignored without error 
* `$schema`, `$id` and `$defs` are only allowed on the root - any other keyword is an error  
* The root and every entry of `$defs` must be one of:  
  - an object with `properties`, which becomes a struct  
  - a string `enum`, which becomes a named `string` type with a constant per value  
  - a `oneOf` of `$ref`s to object definitions, which becomes a union  
* Objects, enums and unions cannot be declared inline. Properties, list items and map values refer to them with a `$ref`  
of the form `#/$defs/Name`  
  
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
    ShapeVariant}  
  
type ShapeVariant interface {  
    ShapeType() string    isShape()}  
  
type ShapeCircle struct {  
    Radius float64 `json:"radius"`}  
