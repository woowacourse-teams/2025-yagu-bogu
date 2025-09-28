package com.yagubogu.presentation.livetalk.chat

import LikeDelta
import LikeUpdateRequest
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.util.ApiException
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.TalkRepository
import com.yagubogu.presentation.livetalk.chat.model.LivetalkReportEvent
import com.yagubogu.presentation.util.getEmoji
import com.yagubogu.presentation.util.getTeam
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
    private val memberRepository: MemberRepository,
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

    private val _livetalkResponseItem = MutableLiveData<LivetalkResponseItem>()
    val livetalkResponseItem: LiveData<LivetalkResponseItem> get() = _livetalkResponseItem

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

    private val fetchLock = Mutex()
    private val pollingControlLock = Mutex()
    private var oldestMessageCursor: Long? = null
    private var newestMessageCursor: Long? = null
    private var hasNext: Boolean = true
    private var pollingJob: Job? = null

    private val _myTeam = MutableLiveData<Team>()
    val myTeam: LiveData<Team> get() = _myTeam

    val myTeamHomeOrAwayID = MutableLiveData<Long>()

    val myTeamEmoji =
        MediatorLiveData<String>().apply {
            addSource(myTeam) { value = myTeam.value?.getEmoji() }
        }

    private val _myTeamLikeRealCount = MutableLiveData<Int>()
    val myTeamLikeRealCount: LiveData<Int> get() = _myTeamLikeRealCount

    private val _myTeamLikeShowingCount = MutableLiveData<Int>()
    val myTeamLikeShowingCount: LiveData<Int> get() = _myTeamLikeShowingCount

    private val _myTeamCheerAnimationEvent = MutableSingleLiveData<Int>()
    val myTeamCheerAnimationEvent: SingleLiveData<Int> get() = _myTeamCheerAnimationEvent

    // TODO: 상대팀 응원수 받는 API 추가할 경우 활용
    private val _otherTeam = MutableLiveData<Team>()
    val otherTeam: LiveData<Team> get() = _otherTeam
    val otherTeamEmoji =
        MediatorLiveData<String>().apply {
            addSource(otherTeam) { value = otherTeam.value?.getEmoji() }
        }

    private val likeMutex = Mutex()
    private var pendingLikeCount = 0
    private var likeBatchingJob: Job? = null

    init {
        fetchInitialTalks()
    }

    fun addMyTeamShowingCount(addValue: Int = 1) {
        _myTeamLikeShowingCount.value = _myTeamLikeShowingCount.value?.plus(addValue)
    }

    fun fetchBeforeTalks() {
        if (!hasNext) return

        viewModelScope.launch {
            fetchLock.withLock {
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
            fetchLock.withLock {
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
            likeMutex.withLock {
                _myTeamLikeRealCount.value?.let { currentLikeCount ->
                    _myTeamLikeRealCount.value = currentLikeCount + 1
                }
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

    private fun startPolling() {
        viewModelScope.launch {
            pollingControlLock.withLock {
                if (pollingJob?.isActive == true) return@launch

                pollingJob =
                    launch {
                        while (true) {
                            fetchAfterTalks()
                            getLikeCount()
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
        if (myTeam.value != null) {
            val result =
                gameRepository.likeCounts(
                    gameId,
                )
            result
                .onSuccess { gameLikesResponse ->
                    val newTotalCount = gameLikesResponse.counts[0].totalCount
                    val currentMyTeamCount = _myTeamLikeRealCount.value ?: 0

                    if (currentMyTeamCount == 0) {
                        _myTeamLikeRealCount.value = newTotalCount
                        _myTeamLikeShowingCount.value = newTotalCount
                        return@onSuccess
                    }

                    if (currentMyTeamCount < newTotalCount) {
                        val diffCount = newTotalCount - currentMyTeamCount

                        _myTeamLikeRealCount.value = newTotalCount
                        _myTeamCheerAnimationEvent.setValue(diffCount)
                    } else if (_myTeamLikeRealCount.value == null) {
                        _myTeamLikeRealCount.value = newTotalCount
                    }

                    Timber.d("응원수 로드 성공: ${gameLikesResponse.counts[0].totalCount} 건")
                }.onFailure { exception ->
                    Timber.w(exception, "응원수 로드 실패")
                }
        }
    }

    private suspend fun sendLikeBatch() {
        val countToSend =
            likeMutex.withLock {
                val count = pendingLikeCount
                pendingLikeCount = 0
                count
            }

        if (countToSend > 0 && myTeamHomeOrAwayID.value != null) {
            val result =
                gameRepository.likeBatches(
                    gameId,
                    LikeUpdateRequest(
                        windowStartEpochSec = Instant.now().epochSecond,
                        likeDelta = LikeDelta(myTeamHomeOrAwayID.value!!, countToSend),
                    ),
                )
            result
                .onSuccess {
                    Timber.d("응원 배치 전송 성공: $countToSend 건")
                }.onFailure { exception ->
                    Timber.w(exception, "응원 배치 전송 실패")
                }
        }
    }

    private fun fetchInitialTalks() {
        viewModelScope.launch {
            fetchLock.withLock {
                val result = talkRepository.getBeforeTalks(gameId, null, CHAT_LOAD_LIMIT)
                result
                    .onSuccess { livetalkResponseItem: LivetalkResponseItem ->
                        _livetalkUiState.value = LivetalkUiState.Success
                        _livetalkResponseItem.value = livetalkResponseItem

                        val livetalkChatBubbleItem: List<LivetalkChatBubbleItem> =
                            livetalkResponseItem.cursor.chats.map { LivetalkChatBubbleItem.of(it) }
                        _liveTalkChatBubbleItem.value = livetalkChatBubbleItem

                        hasNext = livetalkResponseItem.cursor.hasNext
                        oldestMessageCursor = livetalkResponseItem.cursor.nextCursorId
                        newestMessageCursor =
                            livetalkChatBubbleItem.firstOrNull()?.livetalkChatItem?.chatId

                        getTeamEmojis()
                        getLikeCount()
                    }.onFailure { exception ->
                        Timber.w(exception, "초기 메시지 API 호출 실패")
                        _livetalkUiState.value = LivetalkUiState.Error
                    }
            }
        }
    }

    private fun getTeamEmojis() {
        viewModelScope.launch {
            val result = memberRepository.getFavoriteTeam()
            result.onSuccess { favoriteTeam ->
                favoriteTeam?.let { favoriteTeam ->
                    if (livetalkResponseItem.value?.homeTeamName == favoriteTeam) {
                        _myTeam.value = livetalkResponseItem.value?.homeTeamName?.getTeam()
                        _otherTeam.value = livetalkResponseItem.value?.awayTeamName?.getTeam()
                        myTeamHomeOrAwayID.value = HOME_TEAM
                    } else if (livetalkResponseItem.value?.awayTeamName == favoriteTeam) {
                        _myTeam.value = livetalkResponseItem.value?.awayTeamName?.getTeam()
                        _otherTeam.value = livetalkResponseItem.value?.homeTeamName?.getTeam()
                        myTeamHomeOrAwayID.value = AWAY_TEAM
                    }
                }
            }
        }
    }

    private fun fetchAfterTalks() {
        viewModelScope.launch {
            fetchLock.withLock {
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
    }

    override fun onCleared() {
        super.onCleared()
        stopPolling()
        likeBatchingJob?.cancel()

        if (pendingLikeCount > 0) {
            GlobalScope.launch {
                sendLikeBatch()
            }
        }
    }

    companion object {
        private const val POLLING_INTERVAL_MILLS = 10_000L

        private const val CHAT_LOAD_LIMIT = 30

        private const val LIKE_BATCH_INTERVAL_MILLS = 5_000L

        private const val HOME_TEAM = 1L
        private const val AWAY_TEAM = 2L
    }
}
