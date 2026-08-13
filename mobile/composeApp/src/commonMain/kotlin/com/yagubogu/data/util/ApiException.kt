package com.yagubogu.data.util

sealed class ApiException(
    override val message: String?,
    override val cause: Throwable? = null,
) : Exception(message) {
    // 400: 잘못된 요청 (파라미터 오류 등)
    class BadRequest(
        message: String?,
    ) : ApiException(message)

    // 401: 인증 실패 (토큰 만료 등)
    class Unauthorized(
        message: String?,
    ) : ApiException(message)

    // 403: 권한 없음
    class Forbidden(
        message: String?,
    ) : ApiException(message)

    // 404: 리소스를 찾을 수 없음
    class NotFound(
        message: String?,
    ) : ApiException(message)

    // 409: 리소스 충돌 (중복 닉네임 등)
    class Conflict(
        message: String?,
    ) : ApiException(message)

    // 422: 유효성 검사 실패
    class UnprocessableEntity(
        message: String?,
    ) : ApiException(message)

    // 5xx: 서버 내부 에러
    class ServerError(
        message: String?,
    ) : ApiException(message)

    // 기타 네트워크 에러 (Timeout, No Internet 등)
    class NetworkError(
        message: String?,
        cause: Throwable,
    ) : ApiException(message, cause)

    // 정의되지 않은 기타 에러
    class Unknown(
        message: String?,
        cause: Throwable? = null,
    ) : ApiException(message, cause)
}
