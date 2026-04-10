package com.yagubogu.di

import com.tweener.alarmee.AlarmeeService
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration
import com.tweener.alarmee.createAlarmeeService
import org.koin.dsl.module

expect fun createAlarmeePlatformConfiguration(): AlarmeePlatformConfiguration

val alarmeeModule =
    module {
        single<AlarmeeService> {
            createAlarmeeService().also { service ->
                service.initialize(
                    platformConfiguration = createAlarmeePlatformConfiguration(),
                )
            }
        }
    }
