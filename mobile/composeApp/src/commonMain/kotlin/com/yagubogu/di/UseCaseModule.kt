package com.yagubogu.di

import com.yagubogu.domain.usecase.DeleteCheckInUseCase
import com.yagubogu.domain.usecase.LoadDiaryUseCase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

val useCaseModule =
    module {
        factoryOf(::LoadDiaryUseCase)
        factoryOf(::DeleteCheckInUseCase)
    }
