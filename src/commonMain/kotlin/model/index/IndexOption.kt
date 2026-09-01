package model.index

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import model.type.IndexType
import kotlin.js.JsExport

@Serializable(with = IndexOptionSerializer::class)
@JsExport
@SerialName("IndexOption")
sealed interface IndexOption {
    val type: IndexType
}
