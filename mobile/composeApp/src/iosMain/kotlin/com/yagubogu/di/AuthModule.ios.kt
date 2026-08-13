package com.yagubogu.di

import com.yagubogu.di.Qualifier
import com.yagubogu.ui.login.IosAppleCredentialManager
import com.yagubogu.ui.login.IosGoogleCredentialManager
import com.yagubogu.ui.login.auth.OAuthCredentialManager
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val authModule =
    module {
        single<OAuthCredentialManager>(named<Qualifier.Google>()) {
            IosGoogleCredentialManager(get())
        }
        single<OAuthCredentialManager>(named<Qualifier.Apple>()) {
            IosAppleCredentialManager(get())
        }
    }
