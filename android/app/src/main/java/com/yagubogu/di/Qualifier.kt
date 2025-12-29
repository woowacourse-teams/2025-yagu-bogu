package com.yagubogu.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

// --- OkHttpClient ---
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseTokenClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StreamClient

// --- Retrofit ---
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseRetrofit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseTokenRetrofit
