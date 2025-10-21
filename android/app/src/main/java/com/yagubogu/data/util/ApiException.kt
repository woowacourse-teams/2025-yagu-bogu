package com.yagubogu.data.util

sealed class ApiException(
    errorMessage: String?,
) : Exception(errorMessage) {
    data class BadRequest(
        val errorMessage: String?,
    ) : ApiException(errorMessage) // 400

    data class Unauthorized(
        val errorMessage: String?,
    ) : ApiException(errorMessage) // 401

    data class Forbidden(
        val errorMessage: String?,
    ) : ApiException(errorMessage) // 403

    data class NotFound(
        val errorMessage: String?,
    ) : ApiException(errorMessage) // 404

    data class Conflict(
        val errorMessage: String?,
    ) : ApiException(errorMessage) // 409
}
