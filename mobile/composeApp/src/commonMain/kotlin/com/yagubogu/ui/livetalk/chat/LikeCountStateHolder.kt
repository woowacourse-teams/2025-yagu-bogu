package com.yagubogu.ui.livetalk.chat

import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import com.yagubogu.ui.livetalk.chat.model.HomeAwayType
import com.yagubogu.ui.livetalk.chat.model.LivetalkTeams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * 현장톡 내에서 홈팀과 원정팀의 '좋아요' 수 상태를 관리합니다.
 *
 * 이 클래스는 다음을 담당합니다:
 * - 서버로부터 받은 실제 좋아요 수(`homeTeamLikeRealCount`, `awayTeamLikeRealCount`)를 추적합니다.
 * - 사용자의 로컬 인터랙션을 포함하여 UI에 표시되는 좋아요 수(`homeTeamLikeShowingCount`)를 관리합니다.
 * - 사용자가 누른 '좋아요' 클릭(`pendingLikeCount`)을 버퍼링하여 서버에 일괄 전송합니다.
 * - [SharedFlow] 이벤트를 통해 다른 사용자로부터 들어온 '좋아요'에 대한 값을 트리거합니다.
 *
 * [Mutex]를 사용하여 '좋아요' 수 업데이트 시 스레드 안전성을 보장하며, 사용자가 '좋아요' 버튼을
 * 빠르게 연속으로 클릭할 때 발생할 수 있는 경쟁 상태(race condition)를 방지합니다.
 */
class LikeCountStateHolder {
    private val logger = Logger.withTag("LikeCountStateHolder")

    private var isHomeTeamLikeInitialized: Boolean = false
    private var isAwayTeamLikeInitialized: Boolean = false
    private var homeTeamLikeRealCount: Long = 0L
    private var awayTeamLikeRealCount: Long = 0L

    var pendingLikeCount = 0
        private set

    private val lock = Mutex()

    private val _homeTeamLikeShowingCount: MutableStateFlow<Long?> = MutableStateFlow(null)
    val homeTeamLikeShowingCount: StateFlow<Long?> = _homeTeamLikeShowingCount.asStateFlow()

    private val _awayTeamLikeShowingCount: MutableStateFlow<Long?> = MutableStateFlow(null)
    val awayTeamLikeShowingCount: StateFlow<Long?> = _awayTeamLikeShowingCount.asStateFlow()

    private val _homeTeamLikeChangeAmount: MutableSharedFlow<Long?> = MutableSharedFlow()
    val homeTeamLikeChangeAmount: SharedFlow<Long?> = _homeTeamLikeChangeAmount.asSharedFlow()

    private val _awayTeamLikeChangeAmount: MutableSharedFlow<Long?> = MutableSharedFlow()
    val awayTeamLikeChangeAmount: SharedFlow<Long?> = _awayTeamLikeChangeAmount.asSharedFlow()

    suspend fun updateLikeCount(
        livetalkTeams: LivetalkTeams,
        likeCountsResponse: LikeCountsResponse,
    ) {
        // 서버에서 받아온 좋아요 수
        val remoteHomeTeamLikeCount: Long =
            if (likeCountsResponse.counts.isEmpty()) {
                0L
            } else {
                likeCountsResponse.counts.firstOrNull { it.teamCode == livetalkTeams.homeTeam.name }?.totalCount
                    ?: 0L
            }
        val remoteAwayTeamLikeCount: Long =
            if (likeCountsResponse.counts.isEmpty()) {
                0L
            } else {
                likeCountsResponse.counts.firstOrNull { it.teamCode == livetalkTeams.awayTeam.name }?.totalCount
                    ?: 0L
            }
        logger.d { "remoteHomeTeamLikeCount : $remoteHomeTeamLikeCount" }
        logger.d { "remoteAwayTeamLikeCount : $remoteAwayTeamLikeCount" }

        lock.withLock {
            if (!isHomeTeamLikeInitialized) {
                homeTeamLikeRealCount = remoteHomeTeamLikeCount
                _homeTeamLikeShowingCount.value = remoteHomeTeamLikeCount
                isHomeTeamLikeInitialized = true
            }
            if (!isAwayTeamLikeInitialized) {
                awayTeamLikeRealCount = remoteAwayTeamLikeCount
                _awayTeamLikeShowingCount.value = remoteAwayTeamLikeCount
                isAwayTeamLikeInitialized = true
            }

            // 서버에서 받은 좋아요 수보다 (로컬 클릭 포함)실제 응원수가 작은 경우만 애니메이션 실행
            if (homeTeamLikeRealCount < remoteHomeTeamLikeCount) {
                val diffCount: Long = remoteHomeTeamLikeCount - homeTeamLikeRealCount
                homeTeamLikeRealCount = remoteHomeTeamLikeCount
                _homeTeamLikeChangeAmount.emit(diffCount)
            }
            if (awayTeamLikeRealCount < remoteAwayTeamLikeCount) {
                val diffCount: Long = remoteAwayTeamLikeCount - awayTeamLikeRealCount
                awayTeamLikeRealCount = remoteAwayTeamLikeCount
                _awayTeamLikeChangeAmount.emit(diffCount)
            }
        }
    }

    suspend fun increaseHomeTeamShowingCount(addValue: Long = 1L) {
        lock.withLock {
            _homeTeamLikeShowingCount.value =
                _homeTeamLikeShowingCount.value?.plus(addValue) ?: addValue
        }
    }

    suspend fun increaseAwayTeamShowingCount(addValue: Long = 1L) {
        lock.withLock {
            _awayTeamLikeShowingCount.value =
                _awayTeamLikeShowingCount.value?.plus(addValue) ?: addValue
        }
    }

    suspend fun increaseLikeCount(myTeamType: HomeAwayType) {
        lock.withLock {
            when (myTeamType) {
                HomeAwayType.HOME -> homeTeamLikeRealCount++
                HomeAwayType.AWAY -> awayTeamLikeRealCount++
            }
            pendingLikeCount++
        }
    }

    suspend fun getCountToSend(): Int {
        lock.withLock {
            val countToSend = pendingLikeCount
            pendingLikeCount = 0
            return countToSend
        }
    }
}
