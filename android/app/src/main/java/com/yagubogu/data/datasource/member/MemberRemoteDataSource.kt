package com.yagubogu.data.datasource.member

import android.content.Context
import android.net.Uri
import com.yagubogu.data.dto.request.member.MemberCompleteRequest
import com.yagubogu.data.dto.request.member.MemberFavoriteRequest
import com.yagubogu.data.dto.request.member.MemberNicknameRequest
import com.yagubogu.data.dto.request.member.MemberPresignedUrlRequest
import com.yagubogu.data.dto.response.member.MemberCompleteResponse
import com.yagubogu.data.dto.response.member.MemberFavoriteResponse
import com.yagubogu.data.dto.response.member.MemberInfoResponse
import com.yagubogu.data.dto.response.member.MemberNicknameResponse
import com.yagubogu.data.dto.response.member.MemberPresignedUrlResponse
import com.yagubogu.data.service.MemberApiService
import com.yagubogu.data.util.safeApiCall
import com.yagubogu.domain.model.Team
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import timber.log.Timber

class MemberRemoteDataSource(
    private val context: Context,
    private val memberApiService: MemberApiService,
) : MemberDataSource {
    override suspend fun getMemberInfo(): Result<MemberInfoResponse> =
        safeApiCall {
            memberApiService.getMemberInfo()
        }

    override suspend fun getNickname(): Result<MemberNicknameResponse> =
        safeApiCall {
            memberApiService.getNickname()
        }

    override suspend fun updateNickname(nickname: String): Result<MemberNicknameResponse> =
        safeApiCall {
            val request = MemberNicknameRequest(nickname)
            memberApiService.patchNickname(request)
        }

    override suspend fun getFavoriteTeam(): Result<MemberFavoriteResponse> =
        safeApiCall {
            memberApiService.getFavoriteTeam()
        }

    override suspend fun updateFavoriteTeam(team: Team): Result<MemberFavoriteResponse> =
        safeApiCall {
            val request = MemberFavoriteRequest(team.name)
            memberApiService.patchFavoriteTeam(request)
        }

    override suspend fun deleteMember(): Result<Unit> =
        safeApiCall {
            memberApiService.deleteMember()
        }

    override suspend fun getPresignedProfileImageUrl(request: MemberPresignedUrlRequest): Result<MemberPresignedUrlResponse> =
        safeApiCall {
            memberApiService.postPresignedUrl(request)
        }

    override suspend fun uploadProfileImage(
        url: String,
        imageFileUri: Uri,
        contentType: String,
        contentLength: Long
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val requestBody = createRequestBody(imageFileUri, contentType, contentLength)

            val request = Request.Builder()
                .url(url)
                .put(requestBody)
                .build()

            val response = OkHttpClient()
                .newCall(request)
                .execute()

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                val errorBody = response.body.string()
                Timber.e("S3 Upload failed: ${response.code}")
                Timber.e("S3 Error body: $errorBody")
                Result.failure(Exception("Upload failed: ${response.code}"))
            }
        } catch (e: Exception) {
            Timber.e(e, "S3 Upload exception")
            Result.failure(e)
        }
    }

    private fun createRequestBody(
        uri: Uri,
        contentType: String,
        contentLength: Long
    ): RequestBody {
        return object : RequestBody() {
            override fun contentType(): MediaType? {
                return contentType.toMediaTypeOrNull()
            }

            override fun contentLength(): Long {
                return contentLength
            }

            override fun writeTo(sink: BufferedSink) {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    sink.writeAll(inputStream.source())
                }
            }
        }
    }

    override suspend fun postCompleteUploadProfileImage(request: MemberCompleteRequest): Result<MemberCompleteResponse> =
        safeApiCall {
            memberApiService.postCompleteUpload(request)
        }
}
