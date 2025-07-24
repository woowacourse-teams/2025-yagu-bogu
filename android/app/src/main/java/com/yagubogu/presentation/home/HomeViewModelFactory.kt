package com.yagubogu.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yagubogu.domain.repository.CheckInsRepository
import com.yagubogu.domain.repository.LocationRepository
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.domain.repository.StadiumRepository

class HomeViewModelFactory(
    private val memberRepository: MemberRepository,
    private val locationRepository: LocationRepository,
    private val stadiumRepository: StadiumRepository,
    private val checkInsRepository: CheckInsRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(
                memberRepository,
                locationRepository,
                stadiumRepository,
                checkInsRepository,
            ) as T
        }
        throw IllegalArgumentException()
    }
}
