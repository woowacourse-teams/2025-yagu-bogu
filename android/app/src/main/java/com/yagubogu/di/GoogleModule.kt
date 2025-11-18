package com.yagubogu.di

import android.content.Context
import com.yagubogu.BuildConfig
import com.yagubogu.data.auth.GoogleCredentialManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object GoogleModule {
    @Provides
    @ActivityScoped
    fun provideGoogleCredentialManager(
        @ActivityContext context: Context,
    ): GoogleCredentialManager = GoogleCredentialManager(context, BuildConfig.WEB_CLIENT_ID, "")
}
