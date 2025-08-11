package com.yagubogu.presentation.livetalk.chat

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.TalksRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber

class LivetalkChatViewModel(
    private val gameId: Long,
    private val talksRepository: TalksRepository,
) : ViewModel() {
    private val _livetalkResponseItem = MutableLiveData<LivetalkResponseItem>()
    val livetalkResponseItem: LiveData<LivetalkResponseItem> get() = _livetalkResponseItem

    private val _liveTalkChatBubbleItem = MutableLiveData<List<LivetalkChatBubbleItem>>()
    val liveTalkChatBubbleItem: LiveData<List<LivetalkChatBubbleItem>> get() = _liveTalkChatBubbleItem

    val messageFormText = ObservableField<String>()

    init {
        fetchTalks()
        startChatPolling()
    }

    fun fetchTalks(shouldClearChat: Boolean = false) {
        viewModelScope.launch {
            val talksResult: Result<LivetalkResponseItem> =
                talksRepository.getTalks(gameId, null, 10)
            talksResult
                .onSuccess { livetalkResponseItem: LivetalkResponseItem ->
                    _livetalkResponseItem.value = livetalkResponseItem
                    _liveTalkChatBubbleItem.value =
                        livetalkResponseItem.cursor.chats.map { livetalkChatItem: LivetalkChatItem ->
                            LivetalkChatBubbleItem.of(livetalkChatItem)
                        }
                    if (shouldClearChat) {
                        messageFormText.set("")
                    }
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun sendMessage() {
        val message = messageFormText.get() ?: ""
        Timber.d(message)
        if (message.isBlank()) return

        viewModelScope.launch {
            val talksResult: Result<LivetalkChatItem> =
                talksRepository.postTalks(gameId, message.trim())
            talksResult
                .onSuccess { fetchTalks(true) }
                .onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    fun startChatPolling() {
        viewModelScope.launch {
            while (true) {
                delay(POLLING_INTERVAL_MILLS)
                fetchTalks()
            }
        }
    }

    companion object {
        private const val POLLING_INTERVAL_MILLS = 10_000L
    }
}
