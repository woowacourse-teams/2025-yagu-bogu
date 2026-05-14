package com.yagubogu.data.service

import com.yagubogu.data.dto.request.checkin.CheckInImageRequest
import com.yagubogu.data.dto.request.checkin.CheckInMemoRequest
import com.yagubogu.data.dto.request.checkin.CheckInRequest
import com.yagubogu.data.dto.request.checkin.PastCheckInRequest
import com.yagubogu.data.dto.request.presigned.PresignedUrlStartRequest
import com.yagubogu.data.dto.response.checkin.CheckInCountsResponse
import com.yagubogu.data.dto.response.checkin.CheckInHistoryResponse
import com.yagubogu.data.dto.response.checkin.CheckInImageDto
import com.yagubogu.data.dto.response.checkin.CheckInImagesResponse
import com.yagubogu.data.dto.response.checkin.CheckInMemoResponse
import com.yagubogu.data.dto.response.checkin.CheckInReviewResponse
import com.yagubogu.data.dto.response.checkin.CheckInStatusResponse
import com.yagubogu.data.dto.response.checkin.FanRateResponse
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountsResponse
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.PUT
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query

interface CheckInApiService {
    @POST("/api/v1/check-ins")
    suspend fun postCheckIn(
        @Body body: CheckInRequest,
    )

    @DELETE("/api/v1/check-ins/{checkInId}")
    suspend fun deleteCheckIn(
        @Path("checkInId") checkInId: Long,
    )

    @GET("/api/v1/check-ins/counts")
    suspend fun getCheckInCounts(
        @Query("year") year: Int,
    ): CheckInCountsResponse

    @GET("/api/v1/check-ins/stadiums/fan-rates")
    suspend fun getStadiumFanRates(
        @Query("date") date: String,
    ): FanRateResponse

    @GET("/api/v1/check-ins/members")
    suspend fun getCheckInHistories(
        @Query("year") year: Int,
        @Query("month") month: Int?,
        @Query("result") result: String,
        @Query("order") order: String,
    ): CheckInHistoryResponse

    @GET("/api/v1/check-ins/status")
    suspend fun getCheckInStatus(
        @Query("date") date: String,
    ): CheckInStatusResponse

    @GET("/api/v1/check-ins/stadiums/counts")
    suspend fun getStadiumCheckInCounts(
        @Query("year") year: Int?,
    ): StadiumCheckInCountsResponse

    @POST("/api/v1/past-check-ins")
    suspend fun postPastCheckIn(
        @Body body: PastCheckInRequest,
    )

    @GET("/api/v1/check-ins/{checkInId}/review")
    suspend fun getGameReview(
        @Path("checkInId") checkInId: Long,
    ): CheckInReviewResponse

    @GET("/api/v1/check-ins/{checkInId}/memo")
    suspend fun getMemo(
        @Path("checkInId") checkInId: Long,
    ): CheckInMemoResponse

    @PUT("/api/v1/check-ins/{checkInId}/memo")
    suspend fun putMemo(
        @Path("checkInId") checkInId: Long,
        @Body body: CheckInMemoRequest,
    )

    @DELETE("/api/v1/check-ins/{checkInId}/memo")
    suspend fun deleteMemo(
        @Path("checkInId") checkInId: Long,
    )

    @POST("/api/v1/check-ins/image/presigned-url")
    suspend fun postImagePresignedUrl(
        @Body body: PresignedUrlStartRequest,
    ): PresignedUrlStartResponse

    @GET("/api/v1/check-ins/{checkInId}/images")
    suspend fun getImages(
        @Path("checkInId") checkInId: Long,
    ): CheckInImagesResponse

    @POST("/api/v1/check-ins/{checkInId}/images")
    suspend fun postImage(
        @Path("checkInId") checkInId: Long,
        @Body body: CheckInImageRequest,
    ): CheckInImageDto

    @DELETE("/api/v1/check-ins/{checkInId}/images/{imageId}")
    suspend fun deleteImage(
        @Path("checkInId") checkInId: Long,
        @Path("imageId") imageId: Long,
    )
}
