package com.yagubogu.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.MemberRepository
import kotlinx.coroutines.launch

class FavoriteTeamViewModel(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    fun saveFavoriteTeam(team: Team) {
        viewModelScope.launch {
            memberRepository.updateFavoriteTeam(team)
        }
    }
}
