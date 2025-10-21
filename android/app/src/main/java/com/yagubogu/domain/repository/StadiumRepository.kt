package com.yagubogu.domain.repository

import com.yagubogu.presentation.home.model.StadiumsWithGames
import java.time.LocalDate

interface StadiumRepository {
    suspend fun getStadiumsWithGames(date: LocalDate): Result<StadiumsWithGames>
}
