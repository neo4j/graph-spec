package model

import (
	"encoding/json"
	"testing"
)

func TestRequiredNullableFieldsEncodeNull(t *testing.T) {
	out, err := json.Marshal(Profile{})
	if err != nil {
		t.Fatal(err)
	}
	want := `{"anything":null,"nullableItems":null,"nullableList":null,"nullableMap":null,"nullableRef":null,` +
		`"nullableValues":null,"typeArray":null}`
	if string(out) != want {
		t.Errorf("got  %s\nwant %s", out, want)
	}
}

func TestNullableRoundTrip(t *testing.T) {
	input := `{"anything":[1,"two"],"nullableItems":["x",null],"nullableList":[],"nullableMap":{"a":1},` +
		`"nullableRef":{"street":"s"},"nullableValues":{"home":{"street":"u"},"work":null},"optionalNullable":false,` +
		`"typeArray":"a"}`
	var p Profile
	if err := json.Unmarshal([]byte(input), &p); err != nil {
		t.Fatal(err)
	}
	if p.OptionalNullable == nil || *p.OptionalNullable {
		t.Errorf("optionalNullable = %v", p.OptionalNullable)
	}
	if p.NullableItems[1] != nil || p.NullableValues["work"] != nil {
		t.Error("null elements should decode to nil pointers")
	}
	out, err := json.Marshal(p)
	if err != nil {
		t.Fatal(err)
	}
	if string(out) != input {
		t.Errorf("got  %s\nwant %s", out, input)
	}
}
