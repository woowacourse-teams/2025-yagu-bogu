package com.yagubogu.notification

import java.time.Instant

data class ScoreWidgetPayload(
    val type: Type,
    val gameId: Long,
    val displayRevision: Long,
    val gameRevision: Long?,
    val updatedAt: Instant,
    val stadiumName: String?,
    val homeTeamCode: String,
    val homeTeamName: String,
    val awayTeamCode: String,
    val awayTeamName: String,
    val myTeamCode: String,
    val homeScore: Int,
    val awayScore: Int,
    val inning: Int?,
    val inningHalf: InningHalf?,
    val gameState: GameState,
    val bases: Bases?,
    val count: Count?,
    val pitcherName: String?,
    val batterName: String?,
    val rawData: Map<String, String>,
) {
    enum class Type {
        START,
        UPDATE,
        END,
    }

    enum class GameState {
        SCHEDULED,
        LIVE,
        COMPLETED,
        CANCELED,
    }

    enum class InningHalf {
        TOP,
        BOTTOM,
    }

    data class Bases(
        val first: Boolean,
        val second: Boolean,
        val third: Boolean,
    )

    data class Count(
        val balls: Int,
        val strikes: Int,
        val outs: Int,
    )

    companion object {
        fun from(data: Map<String, String>): ScoreWidgetPayload? =
            runCatching {
                val type = Type.valueOf(data.required("type"))
                val gameId = data.required("gameId").toLong()
                val displayRevision = data.required("displayRevision").toLong()
                val updatedAt = Instant.parse(data.required("updatedAt"))
                val inning = data["inning"]?.takeIf(String::isNotBlank)?.toInt()
                val inningHalf = data["inningHalf"]?.takeIf(String::isNotBlank)?.let(InningHalf::valueOf)
                val bases = data.basesOrNull()
                val count = data.countOrNull()

                ScoreWidgetPayload(
                    type = type,
                    gameId = gameId,
                    displayRevision = displayRevision,
                    gameRevision = data["gameRevision"]?.takeIf(String::isNotBlank)?.toLong(),
                    updatedAt = updatedAt,
                    stadiumName = data["stadiumName"]?.takeIf(String::isNotBlank),
                    homeTeamCode = data.required("homeTeamCode"),
                    homeTeamName = data.required("homeTeamName"),
                    awayTeamCode = data.required("awayTeamCode"),
                    awayTeamName = data.required("awayTeamName"),
                    myTeamCode = data.required("myTeamCode"),
                    homeScore = data.required("homeScore").toInt(),
                    awayScore = data.required("awayScore").toInt(),
                    inning = inning,
                    inningHalf = inningHalf,
                    gameState = GameState.valueOf(data.required("gameState")),
                    bases = bases,
                    count = count,
                    pitcherName = data["pitcherName"]?.takeIf(String::isNotBlank),
                    batterName = data["batterName"]?.takeIf(String::isNotBlank),
                    rawData = data,
                )
            }.getOrNull()

        private fun Map<String, String>.required(key: String): String = get(key)?.takeIf(String::isNotBlank) ?: error("Missing $key")

        private fun Map<String, String>.basesOrNull(): Bases? {
            val first = get("baseFirst") ?: return null
            val second = get("baseSecond") ?: return null
            val third = get("baseThird") ?: return null

            return Bases(
                first = first.toBooleanStrict(),
                second = second.toBooleanStrict(),
                third = third.toBooleanStrict(),
            )
        }

        private fun Map<String, String>.countOrNull(): Count? {
            val balls = get("balls") ?: return null
            val strikes = get("strikes") ?: return null
            val outs = get("outs") ?: return null

            return Count(
                balls = balls.toInt(),
                strikes = strikes.toInt(),
                outs = outs.toInt(),
            )
        }
    }
}
