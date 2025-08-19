package com.yagubogu.data.util

open class ApiException(
    message: String? = null,
) : Exception(message)

class BadRequestException(
    message: String?,
) : ApiException(message) // 400

class UnauthorizedException(
    message: String?,
) : ApiException(message) // 401

class ForbiddenException(
    message: String?,
) : ApiException(message) // 403

class NotFoundException(
    message: String?,
) : ApiException(message) // 404
