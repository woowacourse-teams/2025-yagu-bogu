package com.yagubogu.data.util

import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import timber.log.Timber

suspend inline fun <reified T> safeApiCall(crossinline apiCall: suspend () -> HttpResponse): Result<T> =
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
