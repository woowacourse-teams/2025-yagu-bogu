package com.yagubogu.ui.livetalk.chat

import com.yagubogu.data.dto.response.game.LikeCountsResponse
import com.yagubogu.ui.livetalk.chat.model.LivetalkTeams
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

/**
 * 현장톡 내에서 우리 팀과 상대 팀의 '좋아요' 수 상태를 관리합니다.
 *
 * 이 클래스는 다음을 담당합니다:
 * - 서버로부터 받은 실제 좋아요 수(`myTeamLikeRealCount`, `otherTeamLikeRealCount`)를 추적합니다.
 * - 사용자의 로컬 인터랙션을 포함하여 UI에 표시되는 좋아요 수(`myTeamLikeShowingCount`)를 관리합니다.
 * - 사용자가 누른 '좋아요' 클릭(`pendingLikeCount`)을 버퍼링하여 서버에 일괄 전송합니다.
 * - [SharedFlow] 이벤트를 통해 다른 사용자로부터 들어온 '좋아요'에 대한 값을 트리거합니다.
 *
 * [Mutex]를 사용하여 '좋아요' 수 업데이트 시 스레드 안전성을 보장하며, 사용자가 '좋아요' 버튼을
 * 빠르게 연속으로 클릭할 때 발생할 수 있는 경쟁 상태(race condition)를 방지합니다.
 */
class LikeCountStateHolder {
    private var myTeamLikeRealCount: Long = 0L
    private var otherTeamLikeRealCount: Long = 0L

    var pendingLikeCount = 0
        private set

    private val lock = Mutex()

    private val _myTeamLikeShowingCount: MutableStateFlow<Long?> = MutableStateFlow(null)
    val myTeamLikeShowingCount: StateFlow<Long?> = _myTeamLikeShowingCount.asStateFlow()

    private val _myTeamLikeChangeAmount: MutableSharedFlow<Long?> = MutableSharedFlow()
    val myTeamLikeChangeAmount: SharedFlow<Long?> = _myTeamLikeChangeAmount.asSharedFlow()

    private val _otherTeamLikeChangeAmount: MutableSharedFlow<Long?> = MutableSharedFlow()
    val otherTeamLikeChangeAmount: SharedFlow<Long?> = _otherTeamLikeChangeAmount.asSharedFlow()

    suspend fun updateLikeCount(
        livetalkTeams: LivetalkTeams,
        likeCountsResponse: LikeCountsResponse,
    ) {
        // 서버에서 받아온 좋아요 수
        val remoteMyTeamLikeCount: Long =
            if (likeCountsResponse.counts.isEmpty()) {
                0L
            } else {
                likeCountsResponse.counts.firstOrNull { it.teamCode == livetalkTeams.myTeam.name }?.totalCount
                    ?: 0L
            }
        val remoteOtherTeamLikeCount: Long =
            if (likeCountsResponse.counts.isEmpty()) {
                0L
            } else {
                likeCountsResponse.counts.firstOrNull { it.teamCode == livetalkTeams.otherTeam?.name }?.totalCount
                    ?: 0L
            }
        Timber.d("remoteMyTeamLikeCount : $remoteMyTeamLikeCount")
        Timber.d("remoteOtherTeamLikeCount : $remoteOtherTeamLikeCount")

        lock.withLock {
            if (myTeamLikeRealCount == 0L) {
                myTeamLikeRealCount = remoteMyTeamLikeCount
                _myTeamLikeShowingCount.value = remoteMyTeamLikeCount
            }
            if (otherTeamLikeRealCount == 0L) {
                otherTeamLikeRealCount = remoteOtherTeamLikeCount
            }

            // 서버에서 받은 좋아요 수보다 (로컬 클릭 포함)실제 응원수가 작은 경우만 애니메이션 실행
            if (myTeamLikeRealCount < remoteMyTeamLikeCount) {
                val diffCount: Long = remoteMyTeamLikeCount - myTeamLikeRealCount
                myTeamLikeRealCount = remoteMyTeamLikeCount
                _myTeamLikeChangeAmount.emit(diffCount)
            }
            if (otherTeamLikeRealCount < remoteOtherTeamLikeCount) {
                val diffCount: Long = remoteOtherTeamLikeCount - otherTeamLikeRealCount
                otherTeamLikeRealCount = remoteOtherTeamLikeCount
                _otherTeamLikeChangeAmount.emit(diffCount)
            }
        }
    }

    suspend fun increaseMyTeamShowingCount(addValue: Long = 1L) {
        lock.withLock {
            _myTeamLikeShowingCount.value =
                _myTeamLikeShowingCount.value?.plus(addValue) ?: addValue
        }
    }

    suspend fun increaseLikeCount() {
        lock.withLock {
            myTeamLikeRealCount++
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
