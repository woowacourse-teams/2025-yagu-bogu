package com.yagubogu.data.datasource.member

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
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
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source

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

    override suspend fun uploadProfileImage(url: String, imageFileUri: Uri) =
        safeApiCall { // Context 가져오기
            val requestBody = imageFileUri.toRequestBody()
            memberApiService.putProfileImageToS3(url, requestBody)
        }

    override suspend fun postCompleteUploadProfileImage(request: MemberCompleteRequest): Result<MemberCompleteResponse> =
        safeApiCall {
            memberApiService.postCompleteUpload(request)
        }


    private fun Uri.toRequestBody(): RequestBody {
        return object : RequestBody() {
            private val contentResolver = context.contentResolver

            override fun contentType(): MediaType? =
                contentResolver.getType(this@toRequestBody)?.toMediaTypeOrNull()

            override fun contentLength(): Long {
                return contentResolver.query(
                    this@toRequestBody,
                    arrayOf(MediaStore.Images.Media.SIZE),
                    null, null, null
                )?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE))
                    } else -1L
                } ?: -1L
            }

            override fun writeTo(sink: BufferedSink) {
                contentResolver.openInputStream(this@toRequestBody)?.use { inputStream ->
                    sink.writeAll(inputStream.source())
                }
            }
        }
    }
}
