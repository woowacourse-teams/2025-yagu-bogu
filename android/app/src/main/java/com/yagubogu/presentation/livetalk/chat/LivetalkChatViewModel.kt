package com.yagubogu.presentation.livetalk.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.repository.TalksRepository
import kotlinx.coroutines.launch
import timber.log.Timber

class LivetalkChatViewModel(
    private val talksRepository: TalksRepository,
) : ViewModel() {
    private val _livetalkChatItems = MutableLiveData<List<LivetalkChatItem>>()
    val livetalkChatItems: LiveData<List<LivetalkChatItem>> get() = _livetalkChatItems

    init {
        fetchTalks()
    }

    fun fetchTalks() {
        viewModelScope.launch {
            val talksResult: Result<List<LivetalkChatItem>> =
                talksRepository.getTalks(TOKEN, 1, null, 10)
            talksResult
                .onSuccess { livetalkChatItems: List<LivetalkChatItem> ->
                    _livetalkChatItems.value = livetalkChatItems
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }

    companion object {
        private const val TOKEN =
            "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI1MDIyIiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3NTQ1NTM2ODMsImV4cCI6MTc1NDU1NDU4M30.opoqSwxBb9Eh4bL8c8HiI1hteV_usOPsuwLKcVs2ykA"
    }
}
