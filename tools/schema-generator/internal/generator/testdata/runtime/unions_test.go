package model

import (
	"encoding/json"
	"strings"
	"testing"
)

const drawing = `{
	"shapes": [
		{"type": "CIRCLE", "radius": 1.5},
		{"type": "RECTANGLE", "width": 2, "height": 3},
		{"type": "POINT", "x": 0, "y": 1}
	],
	"background": null,
	"origin": {"type": "POINT", "x": 4, "y": 5},
	"marker": {"type": "PIN", "label": "home"},
	"expression": {"op": "add", "left": {"op": "lit", "value": 1}, "right": {"op": "lit", "value": 2}}
}`

func TestUnionsDecodeToVariants(t *testing.T) {
	var d Drawing
	if err := json.Unmarshal([]byte(drawing), &d); err != nil {
		t.Fatal(err)
	}
	if c, ok := d.Shapes[0].ShapeVariant.(ShapeCircle); !ok || c.Radius != 1.5 {
		t.Errorf("shapes[0] = %#v", d.Shapes[0].ShapeVariant)
	}
	if _, ok := d.Shapes[1].ShapeVariant.(RectangleShape); !ok {
		t.Errorf("shapes[1] = %#v", d.Shapes[1].ShapeVariant)
	}
	if d.Shapes[2].ShapeType() != "POINT" {
		t.Errorf("shapes[2] type = %q", d.Shapes[2].ShapeType())
	}
	if d.Background != nil {
		t.Errorf("background = %#v", d.Background)
	}
	if p, ok := d.Marker.MarkerVariant.(MarkerPin); !ok || p.Label != "home" {
		t.Errorf("marker = %#v", d.Marker.MarkerVariant)
	}
	add, ok := d.Expression.ExprVariant.(ExprAdd)
	if !ok {
		t.Fatalf("expression = %#v", d.Expression.ExprVariant)
	}
	if l, ok := add.Right.ExprVariant.(ExprLiteral); !ok || l.Value != 2 {
		t.Errorf("expression.right = %#v", add.Right.ExprVariant)
	}
}

func TestUnionsRoundTrip(t *testing.T) {
	var d Drawing
	if err := json.Unmarshal([]byte(drawing), &d); err != nil {
		t.Fatal(err)
	}
	out, err := json.Marshal(d)
	if err != nil {
		t.Fatal(err)
	}
	assertSameJSON(t, drawing, string(out))
}

func TestVariantsWriteTheirDiscriminator(t *testing.T) {
	out, err := json.Marshal(Drawing{
		Shapes:     []Shape{{ShapeCircle{Radius: 2}}},
		Background: &Shape{RectangleShape{Width: 1, Height: 1}},
	})
	if err != nil {
		t.Fatal(err)
	}
	assertSameJSON(t, `{
		"shapes": [{"type": "CIRCLE", "radius": 2}],
		"background": {"type": "RECTANGLE", "width": 1, "height": 1}
	}`, string(out))

	// A variant used outside its unions, and shared by two of them, writes its discriminator once.
	point, err := json.Marshal(Point{X: 1, Y: 2})
	if err != nil {
		t.Fatal(err)
	}
	assertSameJSON(t, `{"type": "POINT", "x": 1, "y": 2}`, string(point))

	var m Marker
	if err := json.Unmarshal(point, &m); err != nil {
		t.Fatal(err)
	}
	if _, ok := m.MarkerVariant.(Point); !ok {
		t.Errorf("marker = %#v", m.MarkerVariant)
	}
}

func TestRequiredNullableUnionEncodesNull(t *testing.T) {
	out, err := json.Marshal(Drawing{Shapes: []Shape{}})
	if err != nil {
		t.Fatal(err)
	}
	assertSameJSON(t, `{"shapes": [], "background": null}`, string(out))
}

func TestUnionDecodeErrors(t *testing.T) {
	tests := map[string]string{
		`{"radius": 1}`:                    `decoding Shape: missing "type" property`,
		`{"type": "HEXAGON"}`:              `decoding Shape: unknown "type" value "HEXAGON"`,
		`{"type": 3}`:                      `decoding Shape: json: cannot unmarshal number`,
		`{"type": "CIRCLE", "radius": ""}`: `decoding Shape: json: cannot unmarshal string`,
		`[]`:                               `decoding Shape: json: cannot unmarshal array`,
	}
	for input, want := range tests {
		var s Shape
		err := json.Unmarshal([]byte(input), &s)
		if err == nil || !strings.Contains(err.Error(), want) {
			t.Errorf("decoding %s: got error %v, want %q", input, err, want)
		}
	}
}

func assertSameJSON(t *testing.T, want, got string) {
	t.Helper()
	var w, g any
	if err := json.Unmarshal([]byte(want), &w); err != nil {
		t.Fatal(err)
	}
	if err := json.Unmarshal([]byte(got), &g); err != nil {
		t.Fatal(err)
	}
	wb, _ := json.Marshal(w)
	gb, _ := json.Marshal(g)
	if string(wb) != string(gb) {
		t.Errorf("JSON differs:\nwant %s\ngot  %s", wb, gb)
	}
}
