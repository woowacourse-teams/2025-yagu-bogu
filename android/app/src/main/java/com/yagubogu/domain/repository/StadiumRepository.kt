package com.yagubogu.domain.repository

import com.yagubogu.domain.model.Stadiums

interface StadiumRepository {
    suspend fun getStadiums(): Stadiums
}
