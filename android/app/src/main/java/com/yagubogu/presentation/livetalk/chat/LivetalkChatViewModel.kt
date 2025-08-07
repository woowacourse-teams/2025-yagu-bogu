package com.yagubogu.presentation.livetalk.chat

import androidx.databinding.ObservableField
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.TalksRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class LivetalkChatViewModel(
    private val gameId: Long,
    private val talksRepository: TalksRepository,
) : ViewModel() {
    private val _livetalkResponseItem = MutableLiveData<LivetalkResponseItem>()
    val livetalkResponseItem: LiveData<LivetalkResponseItem> get() = _livetalkResponseItem

    private val _livetalkChatItems = MutableLiveData<List<LivetalkChatItem>>()
    val livetalkChatItems: LiveData<List<LivetalkChatItem>> get() = _livetalkChatItems

    val messageFormText = ObservableField<String>()

    init {
        fetchTalks(gameId)
    }

    fun fetchTalks(gameId: Long) {
        viewModelScope.launch {
            val talksResult: Result<LivetalkResponseItem> =
                talksRepository.getTalks(gameId, null, 10)
            talksResult
                .onSuccess { livetalkResponseItem: LivetalkResponseItem ->
                    _livetalkResponseItem.value = livetalkResponseItem
                    _livetalkChatItems.value = livetalkResponseItem.cursor.chats
                    messageFormText.set("")
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
                .onSuccess { fetchTalks(gameId) }
                .onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }
}
