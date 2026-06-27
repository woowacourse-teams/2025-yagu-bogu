package com.yagubogu.ui.share

import com.yagubogu.data.repository.checkin.CheckInRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.stats.StatsRepository
import com.yagubogu.ui.mapper.toUiModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

class LoadAttendanceTicketShareDataUseCase(
    private val memberRepository: MemberRepository,
    private val checkInRepository: CheckInRepository,
    private val statsRepository: StatsRepository,
) {
    suspend operator fun invoke(year: Int): Result<AttendanceTicketShareData> =
        runCatching {
            coroutineScope {
                val favoriteTeam = async { memberRepository.getFavoriteTeam().getOrThrow() }
                val yearItems =
                    async {
                        checkInRepository
                            .getCheckInHistories(
                                year = year,
                                month = null,
                                sort = resolveShareYearHistorySort(),
                                isWinOnly = false,
                            ).getOrThrow()
                            .map { checkInGame -> checkInGame.toUiModel() }
                    }
                val statsCounts =
                    async {
                        statsRepository
                            .getStatsCounts(year)
                            .map { statsCountsResponse -> statsCountsResponse.toUiModel() }
                            .getOrThrow()
                    }
                val winRate = async { statsRepository.getStatsWinRate(year).getOrThrow() }

                AttendanceTicketShareData(
                    favoriteTeam = favoriteTeam.await(),
                    yearItems = yearItems.await(),
                    recordStats = statsCounts.await().toAttendanceShareRecordStats(winRate = winRate.await()),
                )
            }
        }
}
