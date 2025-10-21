@file:Suppress("ktlint:standard:filename")

package com.yagubogu.data.util

import retrofit2.HttpException
import retrofit2.Response

@Suppress("UNCHECKED_CAST")
inline fun <T> safeApiCall(apiCall: () -> Response<T>): Result<T> =
    runCatching {
        val response: Response<T> = apiCall()
        if (response.isSuccessful) {
            response.body() as T
        } else {
            val errorBody = response.errorBody()?.string()
            when (response.code()) {
                400 -> throw ApiException.BadRequest(errorBody)
                401 -> throw ApiException.Unauthorized(errorBody)
                403 -> throw ApiException.Forbidden(errorBody)
                404 -> throw ApiException.NotFound(errorBody)
                409 -> throw ApiException.Conflict(errorBody)
                else -> throw HttpException(response)
            }
        }
    }
