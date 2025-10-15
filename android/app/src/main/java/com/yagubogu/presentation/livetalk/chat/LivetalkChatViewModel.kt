package com.yagubogu.presentation.livetalk.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.dto.request.game.LikeBatchRequest
import com.yagubogu.data.dto.request.game.LikeDeltaDto
import com.yagubogu.data.dto.response.game.LikeCountsResponse
import com.yagubogu.data.util.ApiException
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.domain.repository.TalkRepository
import com.yagubogu.presentation.livetalk.chat.model.LivetalkReportEvent
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import java.time.Instant

class LivetalkChatViewModel(
    private val gameId: Long,
    private val talkRepository: TalkRepository,
    private val gameRepository: GameRepository,
    private val isVerified: Boolean,
) : ViewModel() {
    private val _livetalkUiState =
        MutableLiveData<LivetalkUiState>(LivetalkUiState.Loading)
    val livetalkUiState: LiveData<LivetalkUiState> get() = _livetalkUiState

    val isStadiumLoading =
        MediatorLiveData<Boolean>().apply {
            addSource(livetalkUiState) { value = it !is LivetalkUiState.Success }
        }

    private val _livetalkTeams = MutableLiveData<LivetalkTeams>()
    val livetalkTeams: LiveData<LivetalkTeams> get() = _livetalkTeams

    lateinit var cachedLivetalkTeams: LivetalkTeams

    private val _liveTalkChatBubbleItem = MutableLiveData<List<LivetalkChatBubbleItem>>()
    val liveTalkChatBubbleItem: LiveData<List<LivetalkChatBubbleItem>> get() = _liveTalkChatBubbleItem

    val messageFormText = MutableLiveData<String>()
    val canSendMessage =
        MediatorLiveData<Boolean>().apply {
            addSource(messageFormText) { value = isVerified && !it.isNullOrBlank() }
        }

    private val _livetalkReportEvent = MutableSingleLiveData<LivetalkReportEvent>()
    val livetalkReportEvent: SingleLiveData<LivetalkReportEvent> get() = _livetalkReportEvent

    private val _livetalkDeleteEvent = MutableSingleLiveData<Unit>()
    val livetalkDeleteEvent: SingleLiveData<Unit> get() = _livetalkDeleteEvent

    private val fetchTalksLock = Mutex()
    private val pollingControlLock = Mutex()
    private var oldestMessageCursor: Long? = null
    private var newestMessageCursor: Long? = null
    private var hasNext: Boolean = true
    private var pollingJob: Job? = null

    private var myTeamLikeRealCount: Long = 0
    private var otherTeamLikeRealCount: Long = 0

    private val _myTeamLikeShowingCount = MutableLiveData(0L)
    val myTeamLikeShowingCount: LiveData<Long> get() = _myTeamLikeShowingCount

    private val _myTeamLikeAnimationEvent = MutableSingleLiveData<Long>()
    val myTeamLikeAnimationEvent: SingleLiveData<Long> get() = _myTeamLikeAnimationEvent
    private val _otherTeamLikeAnimationEvent = MutableSingleLiveData<Long>()
    val otherTeamLikeAnimationEvent: SingleLiveData<Long> get() = _otherTeamLikeAnimationEvent

    private val fetchLikesLock = Mutex()
    private var pendingLikeCount = 0
    private var likeBatchingJob: Job? = null

    init {
        fetchAll()
    }

    fun addMyTeamShowingCount(addValue: Long = 1L) {
        _myTeamLikeShowingCount.value = _myTeamLikeShowingCount.value?.plus(addValue)
    }

    fun fetchBeforeTalks() {
        if (!hasNext) return

        viewModelScope.launch {
            fetchTalksLock.withLock {
                val result =
                    talkRepository.getBeforeTalks(gameId, oldestMessageCursor, CHAT_LOAD_LIMIT)
                result
                    .onSuccess { response ->
                        val pastChats = response.cursor.chats.map { LivetalkChatBubbleItem.of(it) }
                        val currentList = _liveTalkChatBubbleItem.value ?: emptyList()
                        _liveTalkChatBubbleItem.value = currentList + pastChats

                        hasNext = response.cursor.hasNext
                        oldestMessageCursor = response.cursor.nextCursorId
                    }.onFailure { exception ->
                        Timber.w(exception, "과거 메시지 API 호출 실패")
                    }
            }
        }
    }

    fun sendMessage() {
        val message = messageFormText.value ?: ""
        if (message.isBlank()) return

        viewModelScope.launch {
            val talksResult: Result<LivetalkChatItem> =
                talkRepository.postTalks(gameId, message.trim())
            talksResult
                .onSuccess {
                    stopPolling()
                    startPolling()
                    messageFormText.value = ""
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun deleteMessage(chatId: Long) {
        viewModelScope.launch {
            fetchTalksLock.withLock {
                talkRepository
                    .deleteTalks(gameId, chatId)
                    .onSuccess {
                        val currentChats = _liveTalkChatBubbleItem.value ?: emptyList()
                        val deletedChats =
                            currentChats.filter { it.livetalkChatItem.chatId != chatId }
                        newestMessageCursor = deletedChats.firstOrNull()?.livetalkChatItem?.chatId
                        oldestMessageCursor = deletedChats.lastOrNull()?.livetalkChatItem?.chatId
                        _liveTalkChatBubbleItem.value = deletedChats
                        _livetalkDeleteEvent.setValue(Unit)
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
    }

    fun reportMessage(chatId: Long) {
        viewModelScope.launch {
            talkRepository
                .reportTalks(chatId)
                .onSuccess {
                    val currentChats: List<LivetalkChatBubbleItem> =
                        _liveTalkChatBubbleItem.value ?: emptyList()
                    val updatedChats: List<LivetalkChatBubbleItem> =
                        currentChats.map { chatBubbleItem: LivetalkChatBubbleItem ->
                            if (chatBubbleItem.livetalkChatItem.chatId == chatId) {
                                val updatedChatItem =
                                    chatBubbleItem.livetalkChatItem.copy(
                                        reported = true,
                                        message = "숨김처리되었습니다",
                                    )
                                LivetalkChatBubbleItem.OtherBubbleItem(updatedChatItem)
                            } else {
                                chatBubbleItem
                            }
                        }
                    _liveTalkChatBubbleItem.value = updatedChats
                    _livetalkReportEvent.setValue(LivetalkReportEvent.Success)
                    Timber.d("현장톡 정상 신고")
                }.onFailure { exception: Throwable ->
                    when (exception) {
                        is ApiException.BadRequest -> {
                            _livetalkReportEvent.setValue(LivetalkReportEvent.DuplicatedReport)
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
            fetchLikesLock.withLock {
                myTeamLikeRealCount++
                pendingLikeCount++
                if (likeBatchingJob?.isActive != true) {
                    likeBatchingJob =
                        launch {
                            delay(LIKE_BATCH_INTERVAL_MILLS)
                            sendLikeBatch()
                        }
                }
            }
        }
    }

    private fun fetchAll() {
        viewModelScope.launch {
            fetchTeams(gameId)
            fetchInitialTalks()
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

    private suspend fun getLikeCount() {
        if (cachedLivetalkTeams.myTeamType == null) {
            return
        }

        val result = gameRepository.getLikeCounts(gameId)
        result
            .onSuccess { likeCountsResponse: LikeCountsResponse ->
                // 서버에서 받아온 좋아요 수
                val remoteMyTeamLikeCount: Long =
                    if (likeCountsResponse.counts.isEmpty()) {
                        0L
                    } else {
                        likeCountsResponse.counts.firstOrNull { it.teamCode == cachedLivetalkTeams.myTeam.name }?.totalCount
                            ?: 0L
                    }
                val remoteOtherTeamLikeCount: Long =
                    if (likeCountsResponse.counts.isEmpty()) {
                        0L
                    } else {
                        likeCountsResponse.counts.firstOrNull { it.teamCode != cachedLivetalkTeams.otherTeam?.name }?.totalCount
                            ?: 0L
                    }

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
                    _myTeamLikeAnimationEvent.setValue(diffCount)
                }
                if (otherTeamLikeRealCount < remoteOtherTeamLikeCount) {
                    val diffCount: Long = remoteOtherTeamLikeCount - otherTeamLikeRealCount
                    otherTeamLikeRealCount = remoteOtherTeamLikeCount
                    _otherTeamLikeAnimationEvent.setValue(diffCount)
                }

                Timber.d("내 팀 응원수 로드 성공: $remoteMyTeamLikeCount 건")
                Timber.d("상대 팀 응원수 로드 성공: $remoteOtherTeamLikeCount 건")
            }.onFailure { exception ->
                Timber.w(exception, "응원수 로드 실패")
            }
    }

    private suspend fun sendLikeBatch() {
        val countToSend =
            fetchLikesLock.withLock {
                val count = pendingLikeCount
                pendingLikeCount = 0
                count
            }

        if (countToSend > 0 && cachedLivetalkTeams.myTeamType != null) {
            Timber.d("보낸 수 countToSend: $countToSend")
            val result =
                gameRepository.addLikeBatches(
                    gameId,
                    LikeBatchRequest(
                        windowStartEpochSec = Instant.now().epochSecond,
                        likeDelta =
                            LikeDeltaDto(
                                teamCode = cachedLivetalkTeams.myTeam.name,
                                delta = countToSend,
                            ),
                    ),
                )
            result
                .onSuccess {
                    Timber.d("응원 배치 전송 성공: $countToSend 건")
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "응원 배치 전송 실패")
                }
        }
    }

    private suspend fun fetchTeams(gameId: Long) {
        val result = talkRepository.getInitial(gameId)
        result
            .onSuccess { livetalkTeams: LivetalkTeams ->
                _livetalkTeams.value = livetalkTeams
                cachedLivetalkTeams = livetalkTeams
            }.onFailure { exception ->
                Timber.w(exception, "최초 팀 정보 가져오기 실패")
                _livetalkUiState.value = LivetalkUiState.Error
            }
    }

    private suspend fun fetchInitialTalks() {
        fetchTalksLock.withLock {
            val result = talkRepository.getBeforeTalks(gameId, null, CHAT_LOAD_LIMIT)
            result
                .onSuccess { livetalkResponseItem: LivetalkResponseItem ->
                    _livetalkUiState.value = LivetalkUiState.Success

                    val livetalkChatBubbleItem: List<LivetalkChatBubbleItem> =
                        livetalkResponseItem.cursor.chats.map { LivetalkChatBubbleItem.of(it) }
                    _liveTalkChatBubbleItem.value = livetalkChatBubbleItem

                    hasNext = livetalkResponseItem.cursor.hasNext
                    oldestMessageCursor = livetalkResponseItem.cursor.nextCursorId
                    newestMessageCursor =
                        livetalkChatBubbleItem.firstOrNull()?.livetalkChatItem?.chatId

                    startPolling()
                }.onFailure { exception ->
                    Timber.w(exception, "초기 메시지 API 호출 실패")
                    _livetalkUiState.value = LivetalkUiState.Error
                }
        }
    }

    private suspend fun fetchAfterTalks() {
        fetchTalksLock.withLock {
            val result =
                talkRepository.getAfterTalks(gameId, newestMessageCursor, CHAT_LOAD_LIMIT)
            result
                .onSuccess { response ->
                    val newChats =
                        response.cursor.chats.map { LivetalkChatBubbleItem.of(it) }
                    if (newChats.isNotEmpty()) {
                        val currentList = _liveTalkChatBubbleItem.value ?: emptyList()
                        _liveTalkChatBubbleItem.value = newChats + currentList

                        newestMessageCursor = newChats.first().livetalkChatItem.chatId
                    }
                }.onFailure { exception ->
                    Timber.w(exception, "최신 메시지 API 호출 실패")
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
        likeBatchingJob?.cancel()

        if (pendingLikeCount > 0) {
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
