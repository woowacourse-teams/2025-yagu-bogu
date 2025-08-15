package com.yagubogu.presentation.livetalk.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.TalksRepository
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

    private val fetchLock = Mutex()
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

                            newestMessageCursor = newChats.last().livetalkChatItem.chatId
                        }
                    }.onFailure { exception ->
                        Timber.w(exception, "최신 메시지 API 호출 실패")
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
                    fetchAfterTalks()
                    messageFormText.value = ""
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun startChatPolling() {
        if (pollingJob?.isActive == true) return
        pollingJob =
            viewModelScope.launch {
                while (true) {
                    fetchAfterTalks()
                    delay(POLLING_INTERVAL_MILLS)
                }
            }
    }

    fun stopChatPolling() {
        pollingJob?.cancel()
        pollingJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopChatPolling()
    }

    companion object {
        private const val POLLING_INTERVAL_MILLS = 10_000L
        private const val CHAT_LOAD_LIMIT = 15
    }
}
