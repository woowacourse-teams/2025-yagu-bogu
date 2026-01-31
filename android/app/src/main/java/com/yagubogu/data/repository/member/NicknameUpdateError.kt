package com.yagubogu.data.repository.member

sealed interface NicknameUpdateError {
    object DuplicateNickname : NicknameUpdateError // 409: 이미 존재함

    object InvalidNickname : NicknameUpdateError // 400, 422: 형식이 잘못됨

    object MemberNotFound : NicknameUpdateError // 404: 탈퇴했거나 없는 회원

    object NoPermission : NicknameUpdateError // 403: 권한 없음

    object PayloadTooLarge : NicknameUpdateError // 413: 데이터가 너무 큼

    object ServerError : NicknameUpdateError // 500, 502: 서버 장애

    data class Unknown(
        val message: String?,
    ) : NicknameUpdateError // 기타
}
