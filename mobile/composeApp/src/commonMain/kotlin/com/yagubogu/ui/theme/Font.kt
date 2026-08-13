package com.yagubogu.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import yagubogu.composeapp.generated.resources.Res
import yagubogu.composeapp.generated.resources.esamanru_bold
import yagubogu.composeapp.generated.resources.esamanru_light
import yagubogu.composeapp.generated.resources.esamanru_medium
import yagubogu.composeapp.generated.resources.pretendard_bold
import yagubogu.composeapp.generated.resources.pretendard_light
import yagubogu.composeapp.generated.resources.pretendard_medium
import yagubogu.composeapp.generated.resources.pretendard_regular
import yagubogu.composeapp.generated.resources.pretendard_semibold

val PretendardFontFamily
    @Composable get() =
        FontFamily(
            Font(Res.font.pretendard_light, FontWeight.Light),
            Font(Res.font.pretendard_regular, FontWeight.Normal),
            Font(Res.font.pretendard_medium, FontWeight.Medium),
            Font(Res.font.pretendard_semibold, FontWeight.SemiBold),
            Font(Res.font.pretendard_bold, FontWeight.Bold),
        )

val EsamanruFontFamily
    @Composable get() =
        FontFamily(
            Font(Res.font.esamanru_light, FontWeight.Light),
            Font(Res.font.esamanru_medium, FontWeight.Medium),
            Font(Res.font.esamanru_bold, FontWeight.Bold),
        )
