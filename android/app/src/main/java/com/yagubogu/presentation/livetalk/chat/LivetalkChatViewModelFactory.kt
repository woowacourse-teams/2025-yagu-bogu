package com.yagubogu.presentation.livetalk.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.GameRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.TalkRepository

class LivetalkChatViewModelFactory(
    private val gameId: Long,
    private val talkRepository: TalkRepository,
    private val memberRepository: MemberRepository,
    private val gameRepository: GameRepository,
    private val isVerified: Boolean,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LivetalkChatViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LivetalkChatViewModel(
                gameId,
                talkRepository,
                memberRepository,
                gameRepository,
                isVerified,
            ) as T
        }
        throw IllegalArgumentException()
    }
}
