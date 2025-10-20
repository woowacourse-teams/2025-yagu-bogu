package com.yagubogu.domain.repository

import com.yagubogu.presentation.home.model.Stadiums
import java.time.LocalDate

interface StadiumRepository {
    suspend fun getStadiums(date: LocalDate): Result<Stadiums>
}
