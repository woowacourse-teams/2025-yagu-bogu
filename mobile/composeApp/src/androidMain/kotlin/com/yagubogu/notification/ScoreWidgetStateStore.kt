package com.yagubogu.notification

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import java.time.Duration
import java.time.Instant

private val Context.scoreWidgetDataStore by preferencesDataStore(name = "score_widget")

class ScoreWidgetStateStore(
    private val context: Context,
) {
    suspend fun currentState(): State {
        val preferences = context.scoreWidgetDataStore.data.first()

        return State(
            gameId = preferences[GAME_ID_KEY],
            displayRevision = preferences[DISPLAY_REVISION_KEY] ?: -1L,
            lastUpdatedAt = preferences[LAST_PAYLOAD_UPDATED_AT_KEY]?.let(Instant::parse),
        )
    }

    suspend fun save(payload: ScoreWidgetPayload) {
        context.scoreWidgetDataStore.edit { preferences ->
            preferences[GAME_ID_KEY] = payload.gameId
            preferences[DISPLAY_REVISION_KEY] = payload.displayRevision
            preferences[LAST_PAYLOAD_UPDATED_AT_KEY] = payload.updatedAt.toString()
            preferences[LAST_PAYLOAD_KEY] = JSONObject(payload.rawData).toString()
        }
    }

    suspend fun clear() {
        context.scoreWidgetDataStore.edit { preferences ->
            preferences.remove(GAME_ID_KEY)
            preferences.remove(DISPLAY_REVISION_KEY)
            preferences.remove(LAST_PAYLOAD_UPDATED_AT_KEY)
            preferences.remove(LAST_PAYLOAD_KEY)
        }
    }

    data class State(
        val gameId: Long?,
        val displayRevision: Long,
        val lastUpdatedAt: Instant?,
    ) {
        // 이전 게임의 마지막 갱신이 이 시간보다 오래됐다면, START 유실 등으로
        // 새 게임 전환을 더는 감지할 수 없는 상태로 보고 강제로 전환을 허용한다.
        fun isStale(now: Instant): Boolean =
            lastUpdatedAt == null || Duration.between(lastUpdatedAt, now) > STALE_THRESHOLD
    }

    private companion object {
        val GAME_ID_KEY = longPreferencesKey("game_id")
        val DISPLAY_REVISION_KEY = longPreferencesKey("display_revision")
        val LAST_PAYLOAD_UPDATED_AT_KEY = stringPreferencesKey("last_payload_updated_at")
        val LAST_PAYLOAD_KEY = stringPreferencesKey("last_payload")
        val STALE_THRESHOLD: Duration = Duration.ofHours(6)
    }
}
