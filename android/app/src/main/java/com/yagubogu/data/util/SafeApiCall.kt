package com.yagubogu.data.util

import io.ktor.client.plugins.ResponseException
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import kotlin.coroutines.cancellation.CancellationException

suspend inline fun <T> safeApiCall(crossinline apiCall: suspend () -> T): Result<T> =
    runCatching {
        apiCall()
    }.fold(
        onSuccess = {
            Result.success(it)
        },
        onFailure = { e: Throwable ->
            // 코루틴 취소 예외는 잡지 말고 던져야 함 (구조적 동시성 유지)
            if (e is CancellationException) throw e

            val exception: Throwable =
                when (e) {
                    // HTTP 에러 (3xx, 4xx, 5xx)
                    is ResponseException -> {
                        val errorBody: String = e.response.bodyAsText()

                        when (e.response.status) {
                            HttpStatusCode.BadRequest -> ApiException.BadRequest(errorBody)
                            HttpStatusCode.Unauthorized -> ApiException.Unauthorized(errorBody)
                            HttpStatusCode.Forbidden -> ApiException.Forbidden(errorBody)
                            HttpStatusCode.NotFound -> ApiException.NotFound(errorBody)
                            HttpStatusCode.Conflict -> ApiException.Conflict(errorBody)
                            else -> e
                        }
                    }

                    else -> e
                }

            Result.failure(exception)
        },
    )
