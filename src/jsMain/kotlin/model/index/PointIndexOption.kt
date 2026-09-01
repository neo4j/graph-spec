package model.index

import kotlinx.js.JsPlainObject
import model.jso

@JsExport
@JsPlainObject
external interface PointIndexOptionJs : IndexOptionJs {
    var cartesianMin: Array<Double>
    var cartesianMax: Array<Double>
    var cartesian3DMin: Array<Double>
    var cartesian3DMax: Array<Double>
    var wgs84Min: Array<Double>
    var wgs84Max: Array<Double>
    var wgs843DMin: Array<Double>
    var wgs843DMax: Array<Double>
}

fun pointIndexOptionJs(
    cartesianMin: Array<Double> = arrayOf(-1000000.0, -1000000.0),
    cartesianMax: Array<Double> = arrayOf(1000000.0, 1000000.0),
    cartesian3DMin: Array<Double> = arrayOf(-1000000.0, -1000000.0, -1000000.0),
    cartesian3DMax: Array<Double> = arrayOf(1000000.0, 1000000.0, 1000000.0),
    wgs84Min: Array<Double> = arrayOf(-180.0, -90.0),
    wgs84Max: Array<Double> = arrayOf(180.0, 90.0),
    wgs843DMin: Array<Double> = arrayOf(-180.0, -90.0, -1000000.0),
    wgs843DMax: Array<Double> = arrayOf(180.0, 90.0, 1000000.0),
): PointIndexOptionJs = jso {
    this.type = "POINT"
    this.cartesianMin = cartesianMin
    this.cartesianMax = cartesianMax
    this.cartesian3DMin = cartesian3DMin
    this.cartesian3DMax = cartesian3DMax
    this.wgs84Min = wgs84Min
    this.wgs84Max = wgs84Max
    this.wgs843DMin = wgs843DMin
    this.wgs843DMax = wgs843DMax
}

fun PointIndexOption.toJs() = pointIndexOptionJs(
    cartesianMin = cartesianMin.toTypedArray(),
    cartesianMax = cartesianMax.toTypedArray(),
    cartesian3DMin = cartesian3DMin.toTypedArray(),
    cartesian3DMax = cartesian3DMax.toTypedArray(),
    wgs84Min = wgs84Min.toTypedArray(),
    wgs84Max = wgs84Max.toTypedArray(),
    wgs843DMin = wgs843DMin.toTypedArray(),
    wgs843DMax = wgs843DMax.toTypedArray(),
)

fun PointIndexOptionJs.toClass() = PointIndexOption(
    cartesianMin = cartesianMin.toDoubleArray(),
    cartesianMax = cartesianMax.toDoubleArray(),
    cartesian3DMin = cartesian3DMin.toDoubleArray(),
    cartesian3DMax = cartesian3DMax.toDoubleArray(),
    wgs84Min = wgs84Min.toDoubleArray(),
    wgs84Max = wgs84Max.toDoubleArray(),
    wgs843DMin = wgs843DMin.toDoubleArray(),
    wgs843DMax = wgs843DMax.toDoubleArray(),
)
