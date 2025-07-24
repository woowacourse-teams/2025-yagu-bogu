@file:Suppress("ktlint:standard:filename")

package com.yagubogu.data.util

import retrofit2.HttpException
import retrofit2.Response

@Suppress("UNCHECKED_CAST")
inline fun <T> safeApiCall(apiCall: () -> Response<T>): Result<T> =
    runCatching {
        val response = apiCall()

        if (response.isSuccessful) {
            response.body() as T
        } else {
            throw HttpException(response)
        }
    }
