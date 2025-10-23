package com.yagubogu.di

import android.content.ContentResolver
import android.content.Context
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.yagubogu.BuildConfig
import com.yagubogu.data.auth.GoogleCredentialManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityScoped
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AndroidModule {
    @Provides
    @Singleton
    fun provideContentResolver(
        @ApplicationContext context: Context,
    ): ContentResolver = context.contentResolver

    @Provides
    @Singleton
    fun provideFusedLocationClient(
        @ApplicationContext context: Context,
    ): FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
}

@Module
@InstallIn(ActivityComponent::class)
object GoogleModule {
    @Provides
    @ActivityScoped
    fun provideGoogleCredentialManager(
        @ActivityContext context: Context,
    ): GoogleCredentialManager = GoogleCredentialManager(context, BuildConfig.WEB_CLIENT_ID, "")
}
