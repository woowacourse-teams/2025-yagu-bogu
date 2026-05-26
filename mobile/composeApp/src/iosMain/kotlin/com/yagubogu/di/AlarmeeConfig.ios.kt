package com.yagubogu.di

import com.tweener.alarmee.configuration.AlarmeeIosPlatformConfiguration
import com.tweener.alarmee.configuration.AlarmeePlatformConfiguration

actual fun provideAlarmeeConfig(): AlarmeePlatformConfiguration = AlarmeeIosPlatformConfiguration
