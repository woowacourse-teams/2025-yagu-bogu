package com.yagubogu.ui.util

import kotlin.math.abs
import kotlin.math.roundToInt

/** `%.1f` 포맷을 대체합니다. */
fun Double.formatOneDecimal(): String {
    val scaled = (this * 10).roundToInt()
    return "${scaled / 10}.${abs(scaled % 10)}"
}

/** `%0{width}d` 포맷을 대체합니다. */
fun Int.zeroPad(width: Int): String = toString().padStart(width, '0')

/** `%,d` 포맷을 대체합니다. */
fun Long.formatWithComma(): String =
    toString()
        .reversed()
        .chunked(3)
        .joinToString(",")
        .reversed()
