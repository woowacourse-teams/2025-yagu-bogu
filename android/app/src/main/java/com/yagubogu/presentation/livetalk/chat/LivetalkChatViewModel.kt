package com.yagubogu.presentation.livetalk.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.util.BadRequestException
import com.yagubogu.data.util.ForbiddenException
import com.yagubogu.data.util.NotFoundException
import com.yagubogu.domain.repository.TalksRepository
import com.yagubogu.presentation.livetalk.chat.model.LivetalkReportEvent
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber

class LivetalkChatViewModel(
    private val gameId: Long,
    private val talksRepository: TalksRepository,
) : ViewModel() {
    private val _livetalkResponseItem = MutableLiveData<LivetalkResponseItem>()
    val livetalkResponseItem: LiveData<LivetalkResponseItem> get() = _livetalkResponseItem

    private val _liveTalkChatBubbleItem = MutableLiveData<List<LivetalkChatBubbleItem>>()
    val liveTalkChatBubbleItem: LiveData<List<LivetalkChatBubbleItem>> get() = _liveTalkChatBubbleItem

    val messageFormText = MutableLiveData<String>()

    private val _livetalkReportEvent = MutableSingleLiveData<LivetalkReportEvent>()
    val livetalkReportEvent: SingleLiveData<LivetalkReportEvent> get() = _livetalkReportEvent

    var pendingDeleteMessageId: Long? = null
    var pendingReportMessageId: Long? = null

    private val fetchLock = Mutex()
    private val pollingControlLock = Mutex()
    private var oldestMessageCursor: Long? = null
    private var newestMessageCursor: Long? = null
    private var hasNext: Boolean = true
    private var pollingJob: Job? = null

    init {
        fetchInitialTalks()
    }

    fun fetchBeforeTalks() {
        if (!hasNext) return

        viewModelScope.launch {
            fetchLock.withLock {
                val result =
                    talksRepository.getBeforeTalks(gameId, oldestMessageCursor, CHAT_LOAD_LIMIT)
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
        Timber.d(message)
        if (message.isBlank()) return

        viewModelScope.launch {
            val talksResult: Result<LivetalkChatItem> =
                talksRepository.postTalks(gameId, message.trim())
            talksResult
                .onSuccess {
                    stopChatPolling()
                    startChatPolling()
                    messageFormText.value = ""
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun deleteMessage() {
        val chatId = pendingDeleteMessageId ?: return
        viewModelScope.launch {
            fetchLock.withLock {
                talksRepository
                    .deleteTalks(gameId, chatId)
                    .onSuccess {
                        val currentChats = _liveTalkChatBubbleItem.value ?: emptyList()
                        val deletedChats =
                            currentChats.filter { it.livetalkChatItem.chatId != chatId }
                        newestMessageCursor = deletedChats.firstOrNull()?.livetalkChatItem?.chatId
                        oldestMessageCursor = deletedChats.lastOrNull()?.livetalkChatItem?.chatId
                        _liveTalkChatBubbleItem.value = deletedChats
                        Timber.d("현장톡 정상 삭제")
                    }.onFailure { exception: Throwable ->
                        when (exception) {
                            is BadRequestException -> {
                                Timber.d("해당 경기에 존재하지 않는 현장톡 삭제 시도")
                            }

                            is ForbiddenException -> {
                                Timber.d("타인의 현장톡 삭제 시도")
                            }

                            is NotFoundException -> {
                                Timber.d("존재하지 않는 현장톡 삭제 시도")
                            }

                            else -> {
                                Timber.d("기타 네트워크 에러")
                            }
                        }
                    }
            }
        }
    }

    fun reportMessage() {
        val chatId = pendingReportMessageId ?: return
        viewModelScope.launch {
            talksRepository
                .reportTalks(chatId)
                .onSuccess {
                    _livetalkReportEvent.setValue(LivetalkReportEvent.Success)
                    Timber.d("현장톡 정상 신고")
                }.onFailure { exception: Throwable ->
                    when (exception) {
                        is BadRequestException -> {
                            _livetalkReportEvent.setValue(LivetalkReportEvent.DuplicatedReport)
                            Timber.d("스스로 신고하거나 중복 신고인 경우")
                        }

                        is ForbiddenException -> {
                            Timber.d("회원이 존재하지 않거나 존재하지 않는 현장톡 신고 시도")
                        }

                        else -> {
                            Timber.d("기타 네트워크 에러")
                        }
                    }
                }
        }
    }

    fun startChatPolling() {
        viewModelScope.launch {
            pollingControlLock.withLock {
                if (pollingJob?.isActive == true) return@launch

                pollingJob =
                    launch {
                        while (true) {
                            fetchAfterTalks()
                            delay(POLLING_INTERVAL_MILLS)
                        }
                    }
            }
        }
    }

    fun stopChatPolling() {
        viewModelScope.launch {
            pollingControlLock.withLock {
                pollingJob?.cancel()
                pollingJob = null
            }
        }
    }

    private fun fetchInitialTalks() {
        viewModelScope.launch {
            fetchLock.withLock {
                val result = talksRepository.getBeforeTalks(gameId, null, CHAT_LOAD_LIMIT)
                result
                    .onSuccess { livetalkResponseItem: LivetalkResponseItem ->
                        _livetalkResponseItem.value = livetalkResponseItem
                        val livetalkChatBubbleItem: List<LivetalkChatBubbleItem> =
                            livetalkResponseItem.cursor.chats.map { LivetalkChatBubbleItem.of(it) }
                        _liveTalkChatBubbleItem.value = livetalkChatBubbleItem

                        hasNext = livetalkResponseItem.cursor.hasNext
                        oldestMessageCursor = livetalkResponseItem.cursor.nextCursorId
                        newestMessageCursor =
                            livetalkChatBubbleItem.firstOrNull()?.livetalkChatItem?.chatId
                    }.onFailure { exception ->
                        Timber.w(exception, "초기 메시지 API 호출 실패")
                    }
            }
        }
    }

    private fun fetchAfterTalks() {
        viewModelScope.launch {
            fetchLock.withLock {
                val result =
                    talksRepository.getAfterTalks(gameId, newestMessageCursor, CHAT_LOAD_LIMIT)
                result
                    .onSuccess { response ->
                        val newChats = response.cursor.chats.map { LivetalkChatBubbleItem.of(it) }
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
        stopChatPolling()
    }

    companion object {
        private const val POLLING_INTERVAL_MILLS = 10_000L
        private const val CHAT_LOAD_LIMIT = 30
    }
}
