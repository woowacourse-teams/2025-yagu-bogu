package com.yagubogu.ui.livetalk.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.request.game.LikeDeltaDto
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.talk.TalkRepository
import com.yagubogu.data.util.ApiException
import com.yagubogu.ui.common.model.MemberProfile
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatBubbleItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkChatUiState
import com.yagubogu.ui.livetalk.chat.model.LivetalkResponseItem
import com.yagubogu.ui.livetalk.chat.model.LivetalkTeams
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.util.now
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock

class LivetalkChatViewModel(
    private val gameId: Long,
    private val talkRepository: TalkRepository,
    private val gameRepository: GameRepository,
    private val memberRepository: MemberRepository,
    private val clock: Clock,
) : ViewModel() {
    private val logger = Logger.withTag("LivetalkChatViewModel")

    val messageStateHolder = MessageStateHolder()
    val likeCountStateHolder = LikeCountStateHolder()

    private val _teams = MutableStateFlow<LivetalkTeams?>(null)
    val teams: StateFlow<LivetalkTeams?> = _teams.asStateFlow()

    private val pollingControlLock = Mutex()
    private var pollingJob: Job? = null

    private var likeBatchingJob: Job? = null
    private val likeDebounceJobs = mutableMapOf<Long, Job>()
    private val remoteLikes = mutableMapOf<Long, LivetalkChatItem>()

    private val _selectedProfile = MutableStateFlow<MemberProfile?>(null)
    val selectedProfile: StateFlow<MemberProfile?> = _selectedProfile.asStateFlow()

    private val _isInitialLoadCompleted: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val isInitialLoadCompleted: StateFlow<Boolean> = _isInitialLoadCompleted.asStateFlow()

    val chatUiState: StateFlow<LivetalkChatUiState> =
        combine(
            messageStateHolder.livetalkChatBubbleItems,
            isInitialLoadCompleted,
        ) { items, isInitialLoadCompleted ->
            when {
                !isInitialLoadCompleted -> LivetalkChatUiState.Loading
                items.isEmpty() -> LivetalkChatUiState.Empty
                else -> LivetalkChatUiState.Success(items)
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = LivetalkChatUiState.Loading,
        )

    init {
        viewModelScope.launch {
            fetchInitial(onFetchSuccess = { startPolling() })
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
                    _isInitialLoadCompleted.value = true
                    messageStateHolder.addBeforeChats(response)
                }.onFailure { exception ->
                    logger.w(exception) { "과거 메시지 API 호출 실패" }
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
                _isInitialLoadCompleted.value = true
                messageStateHolder.addAfterChats(response)
            }.onFailure { exception ->
                logger.w(exception) { "최신 메시지 API 호출 실패" }
            }
    }

    fun sendMessage() {
        val message = messageStateHolder.messageText.value
        if (message.isBlank()) return

        viewModelScope.launch {
            messageStateHolder.updateMessageText("")
            messageStateHolder.addPendingWriteChat(
                LivetalkChatBubbleItem.MyPendingBubbleItem(
                    LivetalkChatItem(
                        clock.now().toEpochMilliseconds(),
                        0L,
                        true,
                        message,
                        null,
                        null,
                        null,
                        LocalDateTime.now(),
                        false,
                    ),
                ),
            )
        }

        viewModelScope.launch {
            val talksResult: Result<LivetalkChatItem> =
                talkRepository.postTalks(gameId, message.trim()).map { it.toUiModel() }
            talksResult
                .onSuccess {
                    stopPolling()
                    startPolling()
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "API 호출 실패" }
                }
        }
    }

    fun deleteMessage(chatId: Long) {
        viewModelScope.launch {
            talkRepository
                .deleteTalks(gameId, chatId)
                .onSuccess {
                    messageStateHolder.deleteChat(chatId)
                    logger.d { "현장톡 정상 삭제" }
                }.onFailure { exception: Throwable ->
                    when (exception) {
                        is ApiException.BadRequest -> logger.w(exception) { "해당 경기에 존재하지 않는 현장톡 삭제 시도" }
                        is ApiException.Forbidden -> logger.w(exception) { "타인의 현장톡 삭제 시도" }
                        is ApiException.NotFound -> logger.w(exception) { "존재하지 않는 현장톡 삭제 시도" }
                        else -> logger.w(exception) { "현장톡 삭제 API 호출 실패" }
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
                    logger.d { "현장톡 정상 신고" }
                }.onFailure { exception: Throwable ->
                    when (exception) {
                        is ApiException.BadRequest -> {
                            logger.e(exception) { "스스로 신고하거나 중복 신고인 경우" }
                        }

                        is ApiException.Forbidden -> {
                            logger.e(exception) { "회원이 존재하지 않거나 존재하지 않는 현장톡 신고 시도" }
                        }

                        else -> {
                            logger.e(exception) { "현장톡 신고 API 호출 실패" }
                        }
                    }
                }
        }
    }

    fun toggleLike(chat: LivetalkChatItem) {
        updateOptimisticLike(chat)
        scheduleLikeSync(chat)
    }

    private fun updateOptimisticLike(chat: LivetalkChatItem) {
        val optimisticLiked = !chat.isLiked
        val optimisticCount =
            if (chat.isLiked) (chat.likeCount - 1).coerceAtLeast(0) else chat.likeCount + 1
        viewModelScope.launch {
            messageStateHolder.updateLikeStatus(chat.chatId, optimisticLiked, optimisticCount)
        }
    }

    private fun scheduleLikeSync(chat: LivetalkChatItem) {
        val chatId = chat.chatId
        if (likeDebounceJobs[chatId]?.isActive != true) {
            remoteLikes[chatId] = chat
        }
        likeDebounceJobs[chatId]?.cancel()
        likeDebounceJobs[chatId] =
            viewModelScope.launch {
                delay(LIKE_DEBOUNCE_MS)
                sendLikeStatus(chatId)
            }
    }

    private suspend fun sendLikeStatus(chatId: Long) {
        // 디바운스 시작 시점의 서버에서 받은 채팅 상태와 현재 UI 상태가 다를 때만 API 호출
        val remoteLike = remoteLikes.remove(chatId) ?: return
        likeDebounceJobs.remove(chatId)

        val currentUiLiked =
            messageStateHolder.livetalkChatBubbleItems.value
                .find { it.livetalkChatItem.chatId == chatId }
                ?.livetalkChatItem
                ?.isLiked ?: return

        if (remoteLike.isLiked == currentUiLiked) return

        talkRepository
            .toggleLike(talkId = chatId)
            .onSuccess { response ->
                // API 호출 중 새 클릭 발생시 새 debounce에 위임
                if (remoteLikes.containsKey(chatId)) return@onSuccess
                messageStateHolder.updateLikeStatus(
                    chatId = chatId,
                    liked = response.liked,
                    likeCount = response.likeCount,
                )
            }.onFailure { exception ->
                // 실패 시 서버 상태로 롤백, 새 클릭이 발생한 경우 롤백하지 않음
                if (remoteLikes.containsKey(chatId)) return@onFailure
                messageStateHolder.updateLikeStatus(
                    chatId,
                    remoteLike.isLiked,
                    remoteLike.likeCount,
                )
                logger.w(exception) { "현장톡 좋아요 API 호출 실패" }
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
        val teams: LivetalkTeams? = teams.value
        teams?.myTeamType ?: return

        gameRepository
            .getLikeCounts(gameId)
            .onSuccess { likeCountsResponse: LikeCountsResponse ->
                likeCountStateHolder.updateLikeCount(teams, likeCountsResponse)
            }.onFailure { exception ->
                logger.w(exception) { "응원수 로드 실패" }
            }
    }

    private suspend fun sendLikeBatch() {
        val teams: LivetalkTeams? = teams.value
        teams ?: return

        val countToSend: Int = likeCountStateHolder.getCountToSend()
        val request =
            LikeBatchRequest(
                windowStartEpochSec = Clock.System.now().epochSeconds,
                likeDelta =
                    LikeDeltaDto(
                        teamCode = teams.myTeam.name,
                        delta = countToSend,
                    ),
            )

        if (countToSend > 0 && teams.myTeamType != null) {
            gameRepository
                .addLikeBatches(gameId, request)
                .onSuccess {
                    logger.d { "응원 배치 전송 성공: $countToSend 건" }
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "응원 배치 전송 실패" }
                }
        }
    }

    private suspend fun fetchInitial(onFetchSuccess: () -> Unit) {
        _isInitialLoadCompleted.value = false
        val result: Result<LivetalkTeams> = talkRepository.getInitial(gameId).map { it.toUiModel() }
        result
            .onSuccess { livetalkTeams: LivetalkTeams ->
                _teams.value = livetalkTeams
                onFetchSuccess()
            }.onFailure { exception ->
                logger.w(exception) { "최초 팀 정보 가져오기 실패" }
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
                    logger.w(exception) { "사용자 프로필 조회 API 호출 실패" }
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
        private const val LIKE_DEBOUNCE_MS = 1_000L
    }
}
