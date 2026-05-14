package com.yagubogu.data.datasource.checkin

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
import kotlinx.datetime.LocalDate

interface CheckInDataSource {
    suspend fun addCheckIn(gameId: Long): Result<Unit>

    suspend fun deleteCheckIn(checkInId: Long): Result<Unit>

    suspend fun getCheckInCounts(year: Int): Result<CheckInCountsResponse>

    suspend fun getStadiumFanRates(date: LocalDate): Result<FanRateResponse>

    suspend fun getCheckInHistories(
        year: Int,
        month: Int?,
        sort: String,
        isWinOnly: Boolean,
    ): Result<CheckInHistoryResponse>

    suspend fun getCheckInStatus(date: LocalDate): Result<CheckInStatusResponse>

    suspend fun getStadiumCheckInCounts(year: Int?): Result<StadiumCheckInCountsResponse>

    suspend fun addPastCheckIn(gameId: Long): Result<Unit>

    suspend fun getGameReview(checkInId: Long): Result<CheckInReviewResponse>

    suspend fun getMemo(checkInId: Long): Result<CheckInMemoResponse>

    suspend fun updateMemo(
        checkInId: Long,
        content: String,
    ): Result<Unit>

    suspend fun deleteMemo(checkInId: Long): Result<Unit>

    suspend fun getImagePresignedUrl(
        contentType: String,
        contentLength: Long,
    ): Result<PresignedUrlStartResponse>

    suspend fun getImages(checkInId: Long): Result<CheckInImagesResponse>

    suspend fun addImage(
        checkInId: Long,
        imageKey: String,
    ): Result<CheckInImageDto>

    suspend fun deleteImage(
        checkInId: Long,
        imageId: Long,
    ): Result<Unit>
}
