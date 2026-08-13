package com.yagubogu.di

import com.yagubogu.BuildKonfig
import com.yagubogu.ui.login.AndroidGoogleCredentialManager
import com.yagubogu.ui.login.auth.OAuthCredentialManager
import com.yagubogu.ui.login.auth.OAuthCredentialResult
import org.koin.androidx.scope.dsl.activityScope
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val authModule =
    module {
        activityScope {
            scoped<OAuthCredentialManager>(named<Qualifier.Google>()) {
                AndroidGoogleCredentialManager(
                    context = get(),
                    serverClientId = BuildKonfig.WEB_CLIENT_ID,
                    nonce = "",
                )
            }
        }
        // Apple 로그인을 Android에서 사용하지 않으므로 익명 객체 생성
        single<OAuthCredentialManager>(named<Qualifier.Apple>()) {
            object : OAuthCredentialManager {
                override suspend fun getCredentialResult(): OAuthCredentialResult = OAuthCredentialResult.Cancel

                override suspend fun signOut(): Result<Unit> = Result.success(Unit)
            }
        }
    }
