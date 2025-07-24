package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.StadiumsResponse
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
