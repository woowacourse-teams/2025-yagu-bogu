package com.yagubogu.data.util

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import retrofit2.HttpException
import retrofit2.Response
import timber.log.Timber

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

suspend inline fun <reified T> safeKtorApiCall(crossinline apiCall: suspend () -> HttpResponse): Result<T> =
    runCatching {
        val response: HttpResponse = apiCall()

        if (response.status.isSuccess()) {
            response.body<T>()
        } else {
            val errorBody: String = response.bodyAsText()

            when (response.status) {
                HttpStatusCode.BadRequest -> throw ApiException.BadRequest(errorBody)
                HttpStatusCode.Unauthorized -> throw ApiException.Unauthorized(errorBody)
                HttpStatusCode.Forbidden -> throw ApiException.Forbidden(errorBody)
                HttpStatusCode.NotFound -> throw ApiException.NotFound(errorBody)
                HttpStatusCode.Conflict -> throw ApiException.Conflict(errorBody)
                else -> throw Exception("HTTP ${response.status.value}: $errorBody")
            }
        }
    }.onFailure { exception: Throwable ->
        Timber.e(exception)
    }
