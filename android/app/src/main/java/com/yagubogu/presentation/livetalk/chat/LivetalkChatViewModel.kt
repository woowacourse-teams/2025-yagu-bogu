package com.yagubogu.presentation.livetalk.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.request.game.LikeDeltaDto
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.talk.TalkRepository
import com.yagubogu.data.util.ApiException
import com.yagubogu.presentation.livetalk.chat.model.LivetalkChatItem
import com.yagubogu.presentation.livetalk.chat.model.LivetalkReportEvent
import com.yagubogu.presentation.livetalk.chat.model.LivetalkResponseItem
import com.yagubogu.presentation.livetalk.chat.model.LivetalkTeams
import com.yagubogu.presentation.livetalk.chat.model.LivetalkUiState
import com.yagubogu.presentation.mapper.toUiModel
import com.yagubogu.ui.common.model.MemberProfile
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.Instant

class LivetalkChatViewModel @AssistedInject constructor(
    @Assisted private val gameId: Long,
    @Assisted private val isVerified: Boolean,
    private val talkRepository: TalkRepository,
    private val gameRepository: GameRepository,
    private val memberRepository: MemberRepository,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(
            gameId: Long,
            isVerified: Boolean,
        ): LivetalkChatViewModel
    }

    val messageStateHolder = MessageStateHolder(isVerified)
    val likeCountStateHolder = LikeCountStateHolder()

    private val _livetalkUiState =
        MutableLiveData<LivetalkUiState>(LivetalkUiState.Loading)
    val livetalkUiState: LiveData<LivetalkUiState> get() = _livetalkUiState

    val isStadiumLoading: LiveData<Boolean> =
        livetalkUiState.map { it !is LivetalkUiState.Success }

    private val _livetalkTeams = MutableLiveData<LivetalkTeams>() // deprecated
    val livetalkTeams: LiveData<LivetalkTeams> get() = _livetalkTeams // deprecated

    private val _teams = MutableStateFlow<LivetalkTeams?>(null)
    val teams: StateFlow<LivetalkTeams?> = _teams.asStateFlow()

    lateinit var cachedLivetalkTeams: LivetalkTeams
        private set

    private val pollingControlLock = Mutex()
    private var pollingJob: Job? = null

    private var likeBatchingJob: Job? = null

    private val _selectedProfile = MutableStateFlow<MemberProfile?>(null)
    val selectedProfile: StateFlow<MemberProfile?> = _selectedProfile.asStateFlow()

    init {
        viewModelScope.launch {
            fetchTeams(gameId)
            startPolling()
        }
    }

    fun fetchBeforeTalks() {
        if (!messageStateHolder.hasNext) return

        viewModelScope.launch {
            val result: Result<LivetalkResponseItem> =
                talkRepository
                    .getBeforeTalks(gameId, messageStateHolder.oldestMessageCursor, CHAT_LOAD_LIMIT)
                    .map { it.toUiModel() }
            result
                .onSuccess { response: LivetalkResponseItem ->
                    messageStateHolder.addBeforeChats(response)
                }.onFailure { exception ->
                    Timber.w(exception, "과거 메시지 API 호출 실패")
                }
        }
    }

    private suspend fun fetchAfterTalks() {
        val result: Result<LivetalkResponseItem> =
            talkRepository
                .getAfterTalks(
                    gameId,
                    messageStateHolder.newestMessageCursor,
                    CHAT_LOAD_LIMIT,
                ).map { it.toUiModel() }
        result
            .onSuccess { response: LivetalkResponseItem ->
                messageStateHolder.addAfterChats(response)
            }.onFailure { exception ->
                Timber.w(exception, "최신 메시지 API 호출 실패")
            }
    }

    fun sendMessage() {
        val message = messageStateHolder.messageText.value
        if (message.isBlank()) return

        viewModelScope.launch {
            val talksResult: Result<LivetalkChatItem> =
                talkRepository.postTalks(gameId, message.trim()).map { it.toUiModel() }
            talksResult
                .onSuccess {
                    stopPolling()
                    startPolling()
                    messageStateHolder.messageFormText.value = "" // deprecated
                    messageStateHolder.updateMessageText("")
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

                        is ApiException.Forbidden -> {
                            Timber.d("회원이 존재하지 않거나 존재하지 않는 현장톡 신고 시도")
                        }

                        else -> {
                            Timber.d(exception)
                        }
                    }
                }
        }
    }

    fun addLikeToBatch() {
        viewModelScope.launch {
            likeCountStateHolder.increaseMyTeamShowingCount()
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
        if (!::cachedLivetalkTeams.isInitialized || cachedLivetalkTeams.myTeamType == null) {
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

        if (countToSend > 0 && ::cachedLivetalkTeams.isInitialized && cachedLivetalkTeams.myTeamType != null) {
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
        val result: Result<LivetalkTeams> = talkRepository.getInitial(gameId).map { it.toUiModel() }
        result
            .onSuccess { livetalkTeams: LivetalkTeams ->
                _livetalkTeams.value = livetalkTeams // deprecated
                _teams.value = livetalkTeams

                cachedLivetalkTeams = livetalkTeams
                _livetalkUiState.value = LivetalkUiState.Success
            }.onFailure { exception ->
                Timber.w(exception, "최초 팀 정보 가져오기 실패")
                _livetalkUiState.value = LivetalkUiState.Error
            }
    }

    fun fetchMemberProfile(memberId: Long) {
        viewModelScope.launch {
            val memberProfileResult: Result<MemberProfile> =
                memberRepository.getMemberProfile(memberId).map { it.toUiModel() }
            memberProfileResult
                .onSuccess { memberProfile: MemberProfile ->
                    _selectedProfile.emit(memberProfile)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "사용자 프로필 조회 API 호출 실패")
                }
        }
    }

    fun dismissProfile() {
        viewModelScope.launch {
            _selectedProfile.emit(null)
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
            CoroutineScope(Dispatchers.IO).launch {
                sendLikeBatch()
            }
        }
    }

    companion object {
        private const val POLLING_INTERVAL_MILLS = 10_000L

        private const val CHAT_LOAD_LIMIT = 30

        private const val LIKE_BATCH_INTERVAL_MILLS = 5_000L

        fun provideFactory(
            assistedFactory: Factory,
            gameId: Long,
            isVerified: Boolean,
        ): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T = assistedFactory.create(gameId, isVerified) as T
            }
    }
}
