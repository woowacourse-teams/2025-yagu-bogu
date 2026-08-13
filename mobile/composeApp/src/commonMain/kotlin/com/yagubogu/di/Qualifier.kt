package com.yagubogu.di

sealed interface Qualifier {
    data object BaseUrl : Qualifier

    data object GlobalClient : Qualifier

    data object StreamClient : Qualifier

    data object TokenRefreshMutex : Qualifier

    data object Google : Qualifier

    data object Apple : Qualifier
}
