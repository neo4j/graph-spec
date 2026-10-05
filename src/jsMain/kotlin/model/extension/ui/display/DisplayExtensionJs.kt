/*
 * Copyright (c) "Neo4j"
 * Neo4j Sweden AB [https://neo4j.com]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package model.extension.ui.display

import kotlinx.js.JsPlainObject
import model.jso
import kotlin.js.JsExport

@JsExport
@JsPlainObject
external interface DisplayExtensionJs {
    var color: String?
    var caption: String?
    var icon: String?
    var x: Double?
    var y: Double?
}

fun displayExtensionJs(
    color: String? = null,
    caption: String? = null,
    icon: String? = null,
    x: Double? = null,
    y: Double? = null
): DisplayExtensionJs = jso {
    this.color = color
    this.caption = caption
    this.icon = icon
    this.x = x
    this.y = y
}

fun DisplayExtension.toJs() = displayExtensionJs(
    color = color,
    caption = caption,
    icon = icon,
    x = x,
    y = y
)

fun DisplayExtensionJs.toClass(): DisplayExtension = DisplayExtension(
    color = color,
    caption = caption,
    icon = icon,
    x = x,
    y = y
)

@JsExport
class DisplayExtensionEditor {
    companion object {
        @JsStatic
        fun setColor(display: DisplayExtensionJs, color: String?) {
            display.color = color
        }

        @JsStatic
        fun setCaption(display: DisplayExtensionJs, caption: String?) {
            display.caption = caption
        }

        @JsStatic
        fun setIcon(display: DisplayExtensionJs, icon: String?) {
            display.icon = icon
        }

        @JsStatic
        fun setPosition(display: DisplayExtensionJs, x: Double?, y: Double?) {
            display.x = x
            display.y = y
        }
    }
}
