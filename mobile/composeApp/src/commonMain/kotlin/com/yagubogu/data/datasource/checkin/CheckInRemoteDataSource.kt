package com.yagubogu.data.datasource.checkin

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
import com.yagubogu.data.service.CheckInApiService
import com.yagubogu.data.util.safeApiCall
import kotlinx.datetime.LocalDate

class CheckInRemoteDataSource(
    private val checkInApiService: CheckInApiService,
) : CheckInDataSource {
    override suspend fun addCheckIn(gameId: Long): Result<Unit> {
        val checkInRequest = CheckInRequest(gameId = gameId)
        return safeApiCall {
            checkInApiService.postCheckIn(checkInRequest)
        }
    }

    override suspend fun deleteCheckIn(checkInId: Long): Result<Unit> =
        safeApiCall {
            checkInApiService.deleteCheckIn(checkInId)
        }

    override suspend fun getCheckInCounts(year: Int): Result<CheckInCountsResponse> =
        safeApiCall {
            checkInApiService.getCheckInCounts(year)
        }

    override suspend fun getStadiumFanRates(date: LocalDate): Result<FanRateResponse> =
        safeApiCall {
            checkInApiService.getStadiumFanRates(date.toString())
        }

    override suspend fun getCheckInHistories(
        year: Int,
        month: Int?,
        sort: String,
        isWinOnly: Boolean,
    ): Result<CheckInHistoryResponse> =
        safeApiCall {
            checkInApiService.getCheckInHistories(
                year = year,
                month = month,
                result = if (isWinOnly) "WIN" else "ALL",
                order = sort,
            )
        }

    override suspend fun getCheckInStatus(date: LocalDate): Result<CheckInStatusResponse> =
        safeApiCall {
            checkInApiService.getCheckInStatus(date.toString())
        }

    override suspend fun getStadiumCheckInCounts(year: Int?): Result<StadiumCheckInCountsResponse> =
        safeApiCall {
            checkInApiService.getStadiumCheckInCounts(year)
        }

    override suspend fun addPastCheckIn(gameId: Long): Result<Unit> {
        val checkInRequest = PastCheckInRequest(gameId = gameId)
        return safeApiCall {
            checkInApiService.postPastCheckIn(checkInRequest)
        }
    }

    override suspend fun getGameReview(checkInId: Long): Result<CheckInReviewResponse> =
        safeApiCall {
            checkInApiService.getGameReview(checkInId)
        }

    override suspend fun getMemo(checkInId: Long): Result<CheckInMemoResponse> =
        safeApiCall {
            checkInApiService.getMemo(checkInId)
        }

    override suspend fun updateMemo(
        checkInId: Long,
        content: String,
    ): Result<Unit> =
        safeApiCall {
            checkInApiService.putMemo(checkInId, CheckInMemoRequest(content))
        }

    override suspend fun deleteMemo(checkInId: Long): Result<Unit> =
        safeApiCall {
            checkInApiService.deleteMemo(checkInId)
        }

    override suspend fun getImagePresignedUrl(
        contentType: String,
        contentLength: Long,
    ): Result<PresignedUrlStartResponse> =
        safeApiCall {
            checkInApiService.postImagePresignedUrl(
                PresignedUrlStartRequest(
                    contentType,
                    contentLength,
                ),
            )
        }

    override suspend fun getImages(checkInId: Long): Result<CheckInImagesResponse> =
        safeApiCall {
            checkInApiService.getImages(checkInId)
        }

    override suspend fun addImage(
        checkInId: Long,
        imageKey: String,
    ): Result<CheckInImageDto> =
        safeApiCall {
            checkInApiService.postImage(checkInId, CheckInImageRequest(imageKey))
        }

    override suspend fun deleteImage(
        checkInId: Long,
        imageId: Long,
    ): Result<Unit> =
        safeApiCall {
            checkInApiService.deleteImage(checkInId, imageId)
        }
}
