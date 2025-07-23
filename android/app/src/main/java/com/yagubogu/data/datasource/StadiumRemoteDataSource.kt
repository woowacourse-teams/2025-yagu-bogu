package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.StadiumsResponse
import com.yagubogu.data.service.StadiumApiService

class StadiumRemoteDataSource(
    private val stadiumApiService: StadiumApiService,
) {
    suspend fun getStadiums(): StadiumsResponse = stadiumApiService.getStadiums()
}
