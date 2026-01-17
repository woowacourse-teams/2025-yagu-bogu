package com.yagubogu.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.sp

val Int.dpToSp
    @Composable
    get() = with(LocalDensity.current) { Dp(toFloat()).toSp() }

val PretendardRegular =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Normal,
    )
val PretendardRegular12 = PretendardRegular.copy(fontSize = 12.sp)
val PretendardRegular16 = PretendardRegular.copy(fontSize = 16.sp)

val PretendardMedium =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Medium,
    )
val PretendardMedium12 = PretendardMedium.copy(fontSize = 12.sp)
val PretendardMedium16 = PretendardMedium.copy(fontSize = 16.sp)
val PretendardMedium24 = PretendardMedium.copy(fontSize = 24.sp)

val PretendardSemiBold =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.SemiBold,
    )
val PretendardSemiBold12 = PretendardSemiBold.copy(fontSize = 12.sp)
val PretendardSemiBold16 = PretendardSemiBold.copy(fontSize = 16.sp)
val PretendardSemiBold20 = PretendardSemiBold.copy(fontSize = 20.sp)

val PretendardBold =
    TextStyle(
        fontFamily = PretendardFontFamily,
        fontWeight = FontWeight.Bold,
    )
val PretendardBold12 = PretendardBold.copy(fontSize = 12.sp)
val PretendardBold16 = PretendardBold.copy(fontSize = 16.sp)
val PretendardBold20 = PretendardBold.copy(fontSize = 20.sp)
val PretendardBold32 = PretendardBold.copy(fontSize = 32.sp)

val EsamanruMedium =
    TextStyle(
        fontFamily = EsamanruFontFamily,
        fontWeight = FontWeight.Medium,
    )
val EsamanruMedium20 = EsamanruMedium.copy(fontSize = 20.sp)
val EsamanruMedium24 = EsamanruMedium.copy(fontSize = 24.sp)

val EsamanruBold =
    TextStyle(
        fontFamily = EsamanruFontFamily,
        fontWeight = FontWeight.Bold,
    )
val EsamanruBold32 = EsamanruBold.copy(fontSize = 32.sp)
