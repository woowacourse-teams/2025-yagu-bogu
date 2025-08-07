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
    private val _livetalkChatItems = MutableLiveData<List<LivetalkChatItem>>()
    val livetalkChatItems: LiveData<List<LivetalkChatItem>> get() = _livetalkChatItems

    val messageFormText = ObservableField<String>()

    init {
        fetchTalks(gameId)
    }

    fun fetchTalks(gameId: Long) {
        viewModelScope.launch {
            val talksResult: Result<List<LivetalkChatItem>> =
                talksRepository.getTalks(TOKEN, gameId, null, 10)
            talksResult
                .onSuccess { livetalkChatItems: List<LivetalkChatItem> ->
                    _livetalkChatItems.value = livetalkChatItems
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
                talksRepository.postTalks(TOKEN, gameId, message.trim())
            talksResult
                .onSuccess { fetchTalks(gameId) }
                .onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    companion object {
        private const val TOKEN =
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDIyIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3NTQ1Njk1NDUsImV4cCI6MTc1NDU3MDQ0NX0.yXHJVE1V7BvUmRczkSRAJA1VSxAM-OyU3bJ5tBAIrTU"
    }
}
