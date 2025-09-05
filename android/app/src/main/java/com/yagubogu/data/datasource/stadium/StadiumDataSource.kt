package com.yagubogu.data.datasource.stadium

import com.yagubogu.data.dto.response.stadium.StadiumsResponse

interface StadiumDataSource {
    suspend fun getStadiums(): Result<StadiumsResponse>
}
