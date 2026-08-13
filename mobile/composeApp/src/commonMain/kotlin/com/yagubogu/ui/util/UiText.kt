package com.yagubogu.ui.util

import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.getString

sealed class UiText {
    data class DynamicString(
        val value: String,
    ) : UiText()

    class StringRes(
        val resId: StringResource,
        vararg val args: Any,
    ) : UiText()

    suspend fun asString(): String =
        when (this) {
            is DynamicString -> value
            is StringRes -> getString(resId, *args)
        }
}
