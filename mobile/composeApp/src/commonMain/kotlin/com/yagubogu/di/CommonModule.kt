package com.yagubogu.di

import com.russhwolf.settings.Settings
import org.koin.dsl.module

val commonModule =
    module {
        single { Settings() }
    }
