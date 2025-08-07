package com.yagubogu.presentation.livetalk.chat

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
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    companion object {
        private const val TOKEN = "액세스 토큰"
    }
}
