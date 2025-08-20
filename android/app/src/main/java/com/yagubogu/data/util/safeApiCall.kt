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
                400 -> throw BadRequestException(errorBody)
                401 -> throw UnauthorizedException(errorBody)
                403 -> throw ForbiddenException(errorBody)
                404 -> throw NotFoundException(errorBody)
                else -> throw HttpException(response)
            }
        }
    }
