package model.index

import kotlinx.js.JsPlainObject
import model.type.IndexType

@JsExport
@JsPlainObject
external interface IndexOptionJs {
    val type: String
}

fun IndexOption.toJs(): IndexOptionJs = when (this) {
    is FullTextIndexOption -> toJs()
    is PointIndexOption -> toJs()
    is VectorIndexOption -> toJs()
}

fun IndexOptionJs.toClass(): IndexOption = when (this.type) {
    IndexType.FULLTEXT.name -> (this as FullTextIndexOptionJs).toClass()
    IndexType.POINT.name -> (this as PointIndexOptionJs).toClass()
    IndexType.VECTOR.name -> (this as VectorIndexOptionJs).toClass()
    else -> error("Unexpected mapping type: ${this.type}")
}
