package com.yagubogu.di

import com.yagubogu.data.local.CommonPreferences
import org.koin.dsl.module

val commonLocalModule =
    module {
        single { CommonPreferences() }
    }
