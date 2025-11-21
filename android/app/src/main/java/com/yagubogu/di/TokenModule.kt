package com.yagubogu.di

import android.content.Context
import com.yagubogu.data.network.TokenAuthenticator
import com.yagubogu.data.network.TokenInterceptor
import com.yagubogu.data.network.TokenManager
import com.yagubogu.data.service.TokenApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TokenModule {
    // --- Token Manager ---
    @Provides
    @Singleton
    fun provideTokenManager(
        @ApplicationContext context: Context,
    ): TokenManager = TokenManager(context)

    // --- Token Interceptor ---
    @Provides
    @Singleton
    fun provideTokenInterceptor(tokenManager: TokenManager): TokenInterceptor = TokenInterceptor(tokenManager)

    // --- Token Authenticator ---
    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenManager: TokenManager,
        tokenApiService: TokenApiService,
    ): TokenAuthenticator = TokenAuthenticator(tokenManager, tokenApiService)
}
