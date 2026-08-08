package com.yagubogu.notification

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import org.json.JSONObject

private val Context.scoreWidgetDataStore by preferencesDataStore(name = "score_widget")

class ScoreWidgetStateStore(
    private val context: Context,
) {
    suspend fun currentState(): State {
        val preferences = context.scoreWidgetDataStore.data.first()

        return State(
            gameId = preferences[GAME_ID_KEY],
            displayRevision = preferences[DISPLAY_REVISION_KEY] ?: -1L,
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

    data class State(
        val gameId: Long?,
        val displayRevision: Long,
    )

    private companion object {
        val GAME_ID_KEY = longPreferencesKey("game_id")
        val DISPLAY_REVISION_KEY = longPreferencesKey("display_revision")
        val LAST_PAYLOAD_UPDATED_AT_KEY = stringPreferencesKey("last_payload_updated_at")
        val LAST_PAYLOAD_KEY = stringPreferencesKey("last_payload")
    }
}
