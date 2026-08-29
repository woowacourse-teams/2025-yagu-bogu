package com.yagubogu.notification

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import android.widget.RemoteViews
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yagubogu.R
import com.yagubogu.YaguBoguActivity
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class ScoreWidgetNotificationManager(
    private val context: Context,
    private val stateStore: ScoreWidgetStateStore = ScoreWidgetStateStore(context),
    private val availability: ScoreWidgetNotificationAvailability = ScoreWidgetNotificationAvailability(context),
) {
    suspend fun handle(payload: ScoreWidgetPayload): HandleResult =
        mutex.withLock {
            val currentState = stateStore.currentState()
            if (payload.displayRevision <= currentState.displayRevision) {
                return HandleResult.IgnoredStale
            }

            if (currentState.gameId != null &&
                currentState.gameId != payload.gameId &&
                payload.type != ScoreWidgetPayload.Type.START &&
                !currentState.isStale(payload.updatedAt)
            ) {
                return HandleResult.IgnoredDifferentGame
            }

            stateStore.save(payload)

            // POST_NOTIFICATIONS는 권한 요청 후 거부/철회될 수 있으므로
            // notify() 직전에 동일 스코프에서 명시적으로 검사한다 (MissingPermission lint 대응).
            val permissionGranted =
                Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                    ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted || !availability.isChannelEnabled()) {
                return HandleResult.SavedNotificationDisabled
            }

            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, createNotification(payload).build())
            HandleResult.Displayed
        }

    suspend fun resetForDebug() {
        mutex.withLock {
            stateStore.clear()
            cancel(context)
        }
    }

    private suspend fun createNotification(payload: ScoreWidgetPayload): NotificationCompat.Builder {
        val isOngoing =
            payload.type != ScoreWidgetPayload.Type.END &&
                payload.gameState != ScoreWidgetPayload.GameState.COMPLETED &&
                payload.gameState != ScoreWidgetPayload.GameState.CANCELED

        return NotificationCompat
            .Builder(context, ScoreWidgetNotificationChannel.ID)
            .setSmallIcon(R.drawable.ic_yagubogu_notification)
            .setContentTitle(notificationTitle(payload))
            .setContentText(notificationText(payload))
            .setCustomContentView(createCompactContentView(payload))
            .setCustomBigContentView(createExpandedContentView(payload))
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setContentIntent(createContentIntent(payload.gameId))
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOnlyAlertOnce(true)
            .setOngoing(isOngoing)
            .setShowWhen(false)
    }

    private suspend fun createCompactContentView(payload: ScoreWidgetPayload): RemoteViews =
        RemoteViews(context.packageName, R.layout.notification_score_widget_compact).apply {
            setTextViewText(R.id.score_widget_compact_away_team, payload.awayTeamName)
            setTextViewText(R.id.score_widget_compact_home_team, payload.homeTeamName)
            setTextViewText(R.id.score_widget_compact_status, statusText(payload))
            setMascot(R.id.score_widget_compact_away_logo, payload.awayTeamCode)
            setMascot(R.id.score_widget_compact_home_logo, payload.homeTeamCode)

            val showScores =
                (payload.awayScore != null && payload.homeScore != null) ||
                    payload.gameState == ScoreWidgetPayload.GameState.CANCELED
            setViewVisibility(R.id.score_widget_compact_away_score, if (showScores) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.score_widget_compact_score_separator, if (showScores) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.score_widget_compact_home_score, if (showScores) View.VISIBLE else View.GONE)
            setTextViewText(R.id.score_widget_compact_away_score, scoreText(payload.awayScore, payload))
            setTextViewText(R.id.score_widget_compact_home_score, scoreText(payload.homeScore, payload))
        }

    private suspend fun createExpandedContentView(payload: ScoreWidgetPayload): RemoteViews =
        RemoteViews(context.packageName, R.layout.notification_score_widget).apply {
            setTextViewText(R.id.score_widget_status, statusText(payload))
            setTextViewText(
                R.id.score_widget_stadium,
                payload.stadiumName ?: context.getString(R.string.score_widget_title),
            )
            setTextViewText(R.id.score_widget_away_team, payload.awayTeamName)
            setTextViewText(R.id.score_widget_home_team, payload.homeTeamName)
            val showScores =
                (payload.awayScore != null && payload.homeScore != null) ||
                    payload.gameState == ScoreWidgetPayload.GameState.CANCELED
            setViewVisibility(R.id.score_widget_away_score, if (showScores) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.score_widget_home_score, if (showScores) View.VISIBLE else View.GONE)
            setTextViewText(R.id.score_widget_away_score, scoreText(payload.awayScore, payload))
            setTextViewText(R.id.score_widget_home_score, scoreText(payload.homeScore, payload))
            setMascot(R.id.score_widget_away_logo, payload.awayTeamCode)
            setMascot(R.id.score_widget_home_logo, payload.homeTeamCode)

            val showLiveDetails = payload.gameState == ScoreWidgetPayload.GameState.LIVE
            setViewVisibility(R.id.score_widget_details, if (showLiveDetails) View.VISIBLE else View.GONE)
            setViewVisibility(R.id.score_widget_bases, if (showLiveDetails) View.VISIBLE else View.GONE)
            if (showLiveDetails) {
                setBaseState(R.id.score_widget_base_first, payload.bases?.first == true)
                setBaseState(R.id.score_widget_base_second, payload.bases?.second == true)
                setBaseState(R.id.score_widget_base_third, payload.bases?.third == true)
                setCountState(payload.count)
                setPlayerName(
                    viewId = R.id.score_widget_pitcher,
                    labelResource = R.string.score_widget_pitcher,
                    name = payload.pitcherName,
                )
                setPlayerName(
                    viewId = R.id.score_widget_batter,
                    labelResource = R.string.score_widget_batter,
                    name = payload.batterName,
                )
            }
        }

    private fun RemoteViews.setBaseState(
        viewId: Int,
        isActive: Boolean,
    ) {
        setImageViewResource(
            viewId,
            if (isActive) R.drawable.bg_score_widget_base_active else R.drawable.bg_score_widget_base_inactive,
        )
    }

    private fun RemoteViews.setCountState(count: ScoreWidgetPayload.Count?) {
        setCountDots(SCORE_WIDGET_BALL_DOTS, count?.balls ?: 0, 3, R.drawable.bg_score_widget_dot_ball)
        setCountDots(SCORE_WIDGET_STRIKE_DOTS, count?.strikes ?: 0, 2, R.drawable.bg_score_widget_dot_strike)
        setCountDots(SCORE_WIDGET_OUT_DOTS, count?.outs ?: 0, 2, R.drawable.bg_score_widget_dot_out)
    }

    private fun RemoteViews.setCountDots(
        viewIds: IntArray,
        activeCount: Int,
        maximum: Int,
        activeResource: Int,
    ) {
        viewIds.take(maximum).forEachIndexed { index, viewId ->
            setImageViewResource(
                viewId,
                if (index < activeCount) activeResource else R.drawable.bg_score_widget_dot_inactive,
            )
        }
    }

    private fun RemoteViews.setPlayerName(
        viewId: Int,
        labelResource: Int,
        name: String?,
    ) {
        setViewVisibility(viewId, if (name == null) View.INVISIBLE else View.VISIBLE)
        if (name != null) {
            setTextViewText(viewId, context.getString(labelResource, name))
        }
    }

    private suspend fun RemoteViews.setMascot(
        viewId: Int,
        teamCode: String,
    ) {
        val bitmap = ScoreWidgetMascotBitmapCache.get(context, teamCode)
        if (bitmap != null) {
            setImageViewBitmap(viewId, bitmap)
        } else {
            setImageViewResource(viewId, R.drawable.ic_yagubogu_notification)
        }
    }

    private fun createContentIntent(gameId: Long): PendingIntent {
        val intent =
            Intent(context, YaguBoguActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_GAME_ID, gameId)
            }

        return PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun statusText(payload: ScoreWidgetPayload): String =
        when (payload.gameState) {
            ScoreWidgetPayload.GameState.SCHEDULED -> context.getString(R.string.score_widget_scheduled)
            ScoreWidgetPayload.GameState.LIVE ->
                payload.inning?.let { inning ->
                    val half =
                        when (payload.inningHalf) {
                            ScoreWidgetPayload.InningHalf.TOP -> context.getString(R.string.score_widget_top)
                            ScoreWidgetPayload.InningHalf.BOTTOM -> context.getString(R.string.score_widget_bottom)
                            null -> ""
                        }
                    context.getString(R.string.score_widget_inning, inning, half)
                } ?: context.getString(R.string.score_widget_live)
            ScoreWidgetPayload.GameState.COMPLETED -> context.getString(R.string.score_widget_completed)
            ScoreWidgetPayload.GameState.CANCELED -> context.getString(R.string.score_widget_canceled)
        }

    private fun notificationTitle(payload: ScoreWidgetPayload): String =
        when (payload.gameState) {
            ScoreWidgetPayload.GameState.SCHEDULED ->
                context.getString(
                    R.string.score_widget_title_match,
                    payload.awayTeamName,
                    payload.homeTeamName,
                    context.getString(R.string.score_widget_scheduled),
                )
            ScoreWidgetPayload.GameState.LIVE,
            ScoreWidgetPayload.GameState.COMPLETED,
            -> scoreTitle(payload)
            ScoreWidgetPayload.GameState.CANCELED ->
                context.getString(
                    R.string.score_widget_title_match,
                    payload.awayTeamName,
                    payload.homeTeamName,
                    context.getString(R.string.score_widget_canceled),
                )
        }

    private fun scoreTitle(payload: ScoreWidgetPayload): String {
        val awayScore = payload.awayScore ?: return notificationMatchTitle(payload)
        val homeScore = payload.homeScore ?: return notificationMatchTitle(payload)

        return context.getString(
            R.string.score_widget_title_score,
            payload.awayTeamName,
            awayScore,
            homeScore,
            payload.homeTeamName,
            statusText(payload),
        )
    }

    private fun notificationMatchTitle(payload: ScoreWidgetPayload): String =
        context.getString(
            R.string.score_widget_title_match,
            payload.awayTeamName,
            payload.homeTeamName,
            statusText(payload),
        )

    private fun scoreText(
        score: Int?,
        payload: ScoreWidgetPayload,
    ): String =
        score?.toString()
            ?: if (payload.gameState == ScoreWidgetPayload.GameState.CANCELED) "-" else ""

    private fun notificationText(payload: ScoreWidgetPayload): String =
        if (payload.gameState == ScoreWidgetPayload.GameState.LIVE && payload.count != null) {
            context.getString(
                R.string.score_widget_content_count,
                payload.count.balls,
                payload.count.strikes,
                payload.count.outs,
            )
        } else {
            payload.stadiumName ?: context.getString(R.string.score_widget_title)
        }

    enum class HandleResult {
        Displayed,
        SavedNotificationDisabled,
        IgnoredStale,
        IgnoredDifferentGame,
    }

    companion object {
        fun cancel(context: Context) {
            NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
        }

        private const val NOTIFICATION_ID = 4021
        private const val EXTRA_GAME_ID = "score_widget_game_id"
        private val mutex = Mutex()
        private val SCORE_WIDGET_BALL_DOTS =
            intArrayOf(
                R.id.score_widget_ball_dot_1,
                R.id.score_widget_ball_dot_2,
                R.id.score_widget_ball_dot_3,
            )
        private val SCORE_WIDGET_STRIKE_DOTS =
            intArrayOf(
                R.id.score_widget_strike_dot_1,
                R.id.score_widget_strike_dot_2,
            )
        private val SCORE_WIDGET_OUT_DOTS =
            intArrayOf(
                R.id.score_widget_out_dot_1,
                R.id.score_widget_out_dot_2,
            )
    }
}
