package com.yagubogu.di

import android.content.Context
import com.yagubogu.BuildConfig
import com.yagubogu.ui.login.auth.GoogleCredentialManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext

@Module
@InstallIn(ActivityComponent::class)
object AuthModule {
    @Provides
    fun provideGoogleCredentialManager(
        @ActivityContext context: Context,
    ): GoogleCredentialManager = GoogleCredentialManager(context, BuildConfig.WEB_CLIENT_ID, "")
}
