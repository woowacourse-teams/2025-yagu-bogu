package com.yagubogu.notification

import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.widget.WidgetSettingsRepository
import kotlinx.coroutines.flow.first

class ScoreWidgetMessageProcessor(
    private val widgetSettingsRepository: WidgetSettingsRepository,
    private val notificationManager: ScoreWidgetNotificationManager,
) {
    private val logger = Logger.withTag("ScoreWidgetProcessor")

    suspend fun process(data: Map<String, String>): ProcessResult {
        if (!widgetSettingsRepository.enabled.first()) {
            logger.i { "실시간 스코어 위젯이 꺼져 있어 메시지를 무시합니다." }
            return ProcessResult.IgnoredDisabled
        }

        val payload = ScoreWidgetPayload.from(data)
        if (payload == null) {
            logger.w { "유효하지 않은 실시간 스코어 위젯 payload를 무시합니다." }
            return ProcessResult.InvalidPayload
        }

        val result = ProcessResult.Processed(notificationManager.handle(payload))
        logger.i {
            "실시간 스코어 위젯 ${payload.type} 처리: gameId=${payload.gameId}, revision=${payload.displayRevision}, 결과=$result"
        }
        return result
    }

    suspend fun resetForDebug() {
        notificationManager.resetForDebug()
        logger.i { "실시간 스코어 위젯 debug 상태를 초기화했습니다." }
    }

    sealed interface ProcessResult {
        data class Processed(
            val notificationResult: ScoreWidgetNotificationManager.HandleResult,
        ) : ProcessResult

        data object IgnoredDisabled : ProcessResult

        data object InvalidPayload : ProcessResult
    }
}
