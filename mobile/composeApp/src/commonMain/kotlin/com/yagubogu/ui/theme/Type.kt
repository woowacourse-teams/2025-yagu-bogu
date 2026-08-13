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

val PretendardRegular
    @Composable get() =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Normal,
        )
val PretendardRegular12
    @Composable get() = PretendardRegular.copy(fontSize = 12.sp)
val PretendardRegular16
    @Composable get() = PretendardRegular.copy(fontSize = 16.sp)

val PretendardMedium
    @Composable get() =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Medium,
        )
val PretendardMedium12
    @Composable get() = PretendardMedium.copy(fontSize = 12.sp)
val PretendardMedium16
    @Composable get() = PretendardMedium.copy(fontSize = 16.sp)
val PretendardMedium24
    @Composable get() = PretendardMedium.copy(fontSize = 24.sp)

val PretendardSemiBold
    @Composable get() =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.SemiBold,
        )
val PretendardSemiBold12
    @Composable get() = PretendardSemiBold.copy(fontSize = 12.sp)
val PretendardSemiBold16
    @Composable get() = PretendardSemiBold.copy(fontSize = 16.sp)
val PretendardSemiBold20
    @Composable get() = PretendardSemiBold.copy(fontSize = 20.sp)

val PretendardBold
    @Composable get() =
        TextStyle(
            fontFamily = PretendardFontFamily,
            fontWeight = FontWeight.Bold,
        )
val PretendardBold12
    @Composable get() = PretendardBold.copy(fontSize = 12.sp)
val PretendardBold16
    @Composable get() = PretendardBold.copy(fontSize = 16.sp)

val PretendardBold20
    @Composable get() = PretendardBold.copy(fontSize = 20.sp)
val PretendardBold32
    @Composable get() = PretendardBold.copy(fontSize = 32.sp)

val EsamanruLight
    @Composable get() =
        TextStyle(
            fontFamily = EsamanruFontFamily,
            fontWeight = FontWeight.Light,
        )

val EsamanruMedium
    @Composable get() =
        TextStyle(
            fontFamily = EsamanruFontFamily,
            fontWeight = FontWeight.Medium,
        )
val EsamanruMedium20
    @Composable get() = EsamanruMedium.copy(fontSize = 20.sp)
val EsamanruMedium24
    @Composable get() = EsamanruMedium.copy(fontSize = 24.sp)

val EsamanruBold
    @Composable get() =
        TextStyle(
            fontFamily = EsamanruFontFamily,
            fontWeight = FontWeight.Bold,
        )
val EsamanruBold32
    @Composable get() = EsamanruBold.copy(fontSize = 32.sp)
