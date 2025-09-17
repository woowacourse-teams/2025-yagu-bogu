package com.yagubogu.data.datasource.stadium

import com.yagubogu.data.dto.response.stadium.StadiumsResponse
import com.yagubogu.data.service.StadiumApiService
import com.yagubogu.data.util.safeApiCall

class StadiumRemoteDataSource(
    private val stadiumApiService: StadiumApiService,
) : StadiumDataSource {
    override suspend fun getStadiums(): Result<StadiumsResponse> =
        safeApiCall {
            stadiumApiService.getStadiums()
        }
}
