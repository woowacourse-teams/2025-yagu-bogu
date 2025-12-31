package com.yagubogu.di

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseUrl

// --- HttpClient ---
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseTokenClient

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class StreamClient

// --- Ktorfit ---
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseKtorfit

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class BaseTokenKtorfit
