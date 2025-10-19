package com.yagubogu.domain.repository

import com.yagubogu.presentation.home.model.Stadiums
import java.time.LocalDate

interface StadiumRepository {
    suspend fun getStadiumsForCheckIn(date: LocalDate): Result<Stadiums>

    // TODO: 과거 직관 인증 getStadiumsForPastCheckIn
}
