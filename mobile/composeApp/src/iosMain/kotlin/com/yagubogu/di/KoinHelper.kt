package com.yagubogu.di

import com.yagubogu.ui.login.AppleSignInDelegate
import com.yagubogu.ui.login.GoogleSignInDelegate
import org.koin.core.context.startKoin
import org.koin.dsl.module

fun initKoinIos(
    googleSignInDelegate: GoogleSignInDelegate,
    appleSignInDelegate: AppleSignInDelegate,
) {
    startKoin {
        modules(
            sharedModules +
                module {
                    single<GoogleSignInDelegate> { googleSignInDelegate }
                    single<AppleSignInDelegate> { appleSignInDelegate }
                },
        )
    }
}
