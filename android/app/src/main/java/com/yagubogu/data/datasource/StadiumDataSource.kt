package com.yagubogu.data.datasource

import com.yagubogu.data.dto.response.StadiumsResponse

interface StadiumDataSource {
    suspend fun getStadiums(): Result<StadiumsResponse>
}
