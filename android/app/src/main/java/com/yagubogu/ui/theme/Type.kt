package com.yagubogu.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val PretendardMedium =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
    )
val PretendardMedium16 = PretendardMedium.copy(fontSize = 16.sp)

val PretendardSemiBold =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
    )

val PretendardBold =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
    )

val PretendardBold20 = PretendardBold.copy(fontSize = 20.sp)
