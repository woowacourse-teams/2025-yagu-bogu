package com.yagubogu.data.util

sealed interface ApiException {
    data class BadRequest(
        override val message: String?,
    ) : Exception(message),
        ApiException // 400

    data class Unauthorized(
        override val message: String?,
    ) : Exception(message),
        ApiException // 401

    data class Forbidden(
        override val message: String?,
    ) : Exception(message),
        ApiException // 403

    data class NotFound(
        override val message: String?,
    ) : Exception(message),
        ApiException // 404
}
