package com.yagubogu.data.service

import com.yagubogu.data.dto.response.StatCountsResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface StatsApiService {
    @GET("/api/stats/counts")
    suspend fun getStatsCounts(
        @Query("memberId") memberId: Long,
        @Query("year") year: Int,
    ): StatCountsResponse
}
