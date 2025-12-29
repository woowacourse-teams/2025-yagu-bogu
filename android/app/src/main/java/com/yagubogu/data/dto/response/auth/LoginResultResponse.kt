package com.yagubogu.data.dto.response.auth

sealed interface LoginResultResponse {
    data object SignUp : LoginResultResponse

    data object SignIn : LoginResultResponse
}
