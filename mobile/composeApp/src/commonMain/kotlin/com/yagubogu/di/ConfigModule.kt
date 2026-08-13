package com.yagubogu.di

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.remoteconfig.remoteConfig
import org.koin.dsl.module

val configModule =
    module {
        single { Firebase.remoteConfig }
    }
