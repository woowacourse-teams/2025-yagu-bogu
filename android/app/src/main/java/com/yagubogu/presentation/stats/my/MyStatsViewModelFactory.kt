package com.yagubogu.presentation.stats.my

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.StatsRepository

class MyStatsViewModelFactory(
    private val statsRepository: StatsRepository,
    private val memberRepository: MemberRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MyStatsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MyStatsViewModel(statsRepository, memberRepository) as T
        }
        throw IllegalArgumentException()
    }
}
