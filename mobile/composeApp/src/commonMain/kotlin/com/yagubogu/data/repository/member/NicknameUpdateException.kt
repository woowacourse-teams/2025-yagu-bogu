package com.yagubogu.data.repository.member

class NicknameUpdateException(
    val error: NicknameUpdateError,
    cause: Throwable? = null,
) : Exception(cause)
