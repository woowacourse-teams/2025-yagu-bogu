package com.yagubogu.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.CheckInRepository
import com.yagubogu.domain.repository.LocationRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.StadiumRepository
import com.yagubogu.domain.repository.StatsRepository

class HomeViewModelFactory(
    private val memberRepository: MemberRepository,
    private val checkInRepository: CheckInRepository,
    private val statsRepository: StatsRepository,
    private val locationRepository: LocationRepository,
    private val stadiumRepository: StadiumRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                memberRepository,
                checkInRepository,
                statsRepository,
                locationRepository,
                stadiumRepository,
            ) as T
        }
        throw IllegalArgumentException()
    }
}
