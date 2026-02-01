package com.yagubogu.ui.main.model

sealed interface AutoLoginState {
    data object SignUp : AutoLoginState

    data object SignIn : AutoLoginState

    data object Failure : AutoLoginState

    data object Loading : AutoLoginState
}
