package com.yagubogu.data.repository.checkin

import com.yagubogu.data.dto.response.checkin.CheckInGameDto
import com.yagubogu.data.dto.response.checkin.CheckInImageDto
import com.yagubogu.data.dto.response.checkin.CheckInReviewResponse
import com.yagubogu.data.dto.response.checkin.FanRateByGameDto
import com.yagubogu.data.dto.response.checkin.StadiumCheckInCountDto
import com.yagubogu.data.dto.response.presigned.PresignedUrlStartResponse
import kotlinx.datetime.LocalDate

interface CheckInRepository {
    suspend fun addCheckIn(gameId: Long): Result<Unit>

    suspend fun deleteCheckIn(checkInId: Long): Result<Unit>

    suspend fun getCheckInCounts(year: Int): Result<Int>

    suspend fun getStadiumFanRates(date: LocalDate): Result<List<FanRateByGameDto>>

    suspend fun getCheckInHistories(
        year: Int,
        month: Int?,
        sort: String,
        isWinOnly: Boolean,
    ): Result<List<CheckInGameDto>>

    suspend fun getCheckInStatus(date: LocalDate): Result<Boolean>

    suspend fun getStadiumCheckInCounts(year: Int?): Result<List<StadiumCheckInCountDto>>

    suspend fun addPastCheckIn(gameId: Long): Result<Unit>

    suspend fun getGameReview(checkInId: Long): Result<CheckInReviewResponse>

    suspend fun getMemo(checkInId: Long): Result<String?>

    suspend fun updateMemo(
        checkInId: Long,
        content: String,
    ): Result<Unit>

    suspend fun deleteMemo(checkInId: Long): Result<Unit>

    suspend fun getImagePresignedUrl(
        contentType: String,
        contentLength: Long,
    ): Result<PresignedUrlStartResponse>

    suspend fun getImages(checkInId: Long): Result<List<CheckInImageDto>>

    suspend fun addImage(
        checkInId: Long,
        imageKey: String,
    ): Result<CheckInImageDto>

    suspend fun deleteImage(
        checkInId: Long,
        imageId: Long,
    ): Result<Unit>
}
