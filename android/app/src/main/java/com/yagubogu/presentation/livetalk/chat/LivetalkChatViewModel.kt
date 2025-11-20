package com.yagubogu.presentation.livetalk.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.request.game.LikeDeltaDto
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import com.yagubogu.data.util.ApiException
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.TalkRepository
import com.yagubogu.presentation.livetalk.chat.model.LivetalkReportEvent
import com.yagubogu.ui.common.model.MemberProfile
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.Instant

class LivetalkChatViewModel(
    private val gameId: Long,
    private val talkRepository: TalkRepository,
    private val gameRepository: GameRepository,
    private val memberRepository: MemberRepository,
    isVerified: Boolean,
) : ViewModel() {
    val messageStateHolder = MessageStateHolder(isVerified)
    val likeCountStateHolder = LikeCountStateHolder()

    private val _livetalkUiState =
        MutableLiveData<LivetalkUiState>(LivetalkUiState.Loading)
    val livetalkUiState: LiveData<LivetalkUiState> get() = _livetalkUiState

    val isStadiumLoading: LiveData<Boolean> =
        livetalkUiState.map { it !is LivetalkUiState.Success }

    private val _livetalkTeams = MutableLiveData<LivetalkTeams>()
    val livetalkTeams: LiveData<LivetalkTeams> get() = _livetalkTeams

    lateinit var cachedLivetalkTeams: LivetalkTeams
        private set

    private val pollingControlLock = Mutex()
    private var pollingJob: Job? = null

    private var likeBatchingJob: Job? = null

    private val _profileInfoClickEvent = MutableSharedFlow<MemberProfile>()
    val profileInfoClickEvent: SharedFlow<MemberProfile> = _profileInfoClickEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            fetchTeams(gameId)
            fetchBeforeTalks()
            startPolling()
        }
    }

    fun fetchBeforeTalks() {
        if (!messageStateHolder.hasNext) return

        viewModelScope.launch {
            talkRepository
                .getBeforeTalks(
                    gameId,
                    messageStateHolder.oldestMessageCursor,
                    CHAT_LOAD_LIMIT,
                ).onSuccess { response: LivetalkResponseItem ->
                    messageStateHolder.addBeforeChats(response)
                }.onFailure { exception ->
                    Timber.w(exception, "과거 메시지 API 호출 실패")
                }
        }
    }

    private suspend fun fetchAfterTalks() {
        talkRepository
            .getAfterTalks(
                gameId,
                messageStateHolder.newestMessageCursor,
                CHAT_LOAD_LIMIT,
            ).onSuccess { response: LivetalkResponseItem ->
                messageStateHolder.addAfterChats(response)
            }.onFailure { exception ->
                Timber.w(exception, "최신 메시지 API 호출 실패")
            }
    }

    fun sendMessage() {
        val message = messageStateHolder.messageFormText.value ?: ""
        if (message.isBlank()) return

        viewModelScope.launch {
            val talksResult: Result<LivetalkChatItem> =
                talkRepository.postTalks(gameId, message.trim())
            talksResult
                .onSuccess {
                    stopPolling()
                    startPolling()
                    messageStateHolder.messageFormText.value = ""
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun deleteMessage(chatId: Long) {
        viewModelScope.launch {
            talkRepository
                .deleteTalks(gameId, chatId)
                .onSuccess {
                    messageStateHolder.deleteChat(chatId)
                    Timber.d("현장톡 정상 삭제")
                }.onFailure { exception: Throwable ->
                    when (exception) {
                        is ApiException.BadRequest -> Timber.d("해당 경기에 존재하지 않는 현장톡 삭제 시도")
                        is ApiException.Forbidden -> Timber.d("타인의 현장톡 삭제 시도")
                        is ApiException.NotFound -> Timber.d("존재하지 않는 현장톡 삭제 시도")
                        else -> Timber.d(exception)
                    }
                }
        }
    }

    fun reportMessage(chatId: Long) {
        viewModelScope.launch {
            talkRepository
                .reportTalks(chatId)
                .onSuccess {
                    messageStateHolder.reportChat(chatId)
                    messageStateHolder.updateLivetalkReportEvent(LivetalkReportEvent.Success)
                    Timber.d("현장톡 정상 신고")
                }.onFailure { exception: Throwable ->
                    when (exception) {
                        is ApiException.BadRequest -> {
                            messageStateHolder.updateLivetalkReportEvent(LivetalkReportEvent.DuplicatedReport)
                            Timber.d("스스로 신고하거나 중복 신고인 경우")
                        }

                        is ApiException.Forbidden -> Timber.d("회원이 존재하지 않거나 존재하지 않는 현장톡 신고 시도")
                        else -> Timber.d(exception)
                    }
                }
        }
    }

    fun addLikeToBatch() {
        viewModelScope.launch {
            likeCountStateHolder.increaseLikeCount()
            if (likeBatchingJob?.isActive != true) {
                likeBatchingJob =
                    launch {
                        delay(LIKE_BATCH_INTERVAL_MILLS)
                        sendLikeBatch()
                    }
            }
        }
    }

    private suspend fun getLikeCount() {
        if (cachedLivetalkTeams.myTeamType == null) {
            return
        }

        gameRepository
            .getLikeCounts(gameId)
            .onSuccess { likeCountsResponse: LikeCountsResponse ->
                likeCountStateHolder.updateLikeCount(cachedLivetalkTeams, likeCountsResponse)
            }.onFailure { exception ->
                Timber.w(exception, "응원수 로드 실패")
            }
    }

    private suspend fun sendLikeBatch() {
        val countToSend: Int = likeCountStateHolder.getCountToSend()
        val request =
            LikeBatchRequest(
                windowStartEpochSec = Instant.now().epochSecond,
                likeDelta =
                    LikeDeltaDto(
                        teamCode = cachedLivetalkTeams.myTeam.name,
                        delta = countToSend,
                    ),
            )

        if (countToSend > 0 && cachedLivetalkTeams.myTeamType != null) {
            gameRepository
                .addLikeBatches(gameId, request)
                .onSuccess {
                    Timber.d("응원 배치 전송 성공: $countToSend 건")
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "응원 배치 전송 실패")
                }
        }
    }

    private suspend fun fetchTeams(gameId: Long) {
        talkRepository
            .getInitial(gameId)
            .onSuccess { livetalkTeams: LivetalkTeams ->
                _livetalkTeams.value = livetalkTeams
                cachedLivetalkTeams = livetalkTeams
                _livetalkUiState.value = LivetalkUiState.Success
            }.onFailure { exception ->
                Timber.w(exception, "최초 팀 정보 가져오기 실패")
                _livetalkUiState.value = LivetalkUiState.Error
            }
    }

    fun fetchMemberProfile(memberId: Long) {
        viewModelScope.launch {
            memberRepository
                .getMemberProfile(memberId)
                .onSuccess { memberProfile: MemberProfile ->
                    _profileInfoClickEvent.emit(memberProfile)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "사용자 프로필 조회 API 호출 실패")
                }
        }
    }

    private fun startPolling() {
        viewModelScope.launch {
            pollingControlLock.withLock {
                if (pollingJob?.isActive == true) return@launch

                pollingJob =
                    launch {
                        while (true) {
                            launch { fetchAfterTalks() }
                            launch { getLikeCount() }
                            delay(POLLING_INTERVAL_MILLS)
                        }
                    }
            }
        }
    }

    private fun stopPolling() {
        viewModelScope.launch {
            pollingControlLock.withLock {
                pollingJob?.cancel()
                pollingJob = null
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
        likeBatchingJob?.cancel()

        if (likeCountStateHolder.pendingLikeCount > 0) {
            // TODO 추후 리팩터링에서 GlobalScope 제거하기
            GlobalScope.launch {
                sendLikeBatch()
            }
        }
    }

    companion object {
        private const val POLLING_INTERVAL_MILLS = 10_000L

        private const val CHAT_LOAD_LIMIT = 30

        private const val LIKE_BATCH_INTERVAL_MILLS = 5_000L
    }
}
