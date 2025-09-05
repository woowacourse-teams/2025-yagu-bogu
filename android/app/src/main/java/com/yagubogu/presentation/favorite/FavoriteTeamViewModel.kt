package com.yagubogu.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.repository.MemberRepository
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import kotlinx.coroutines.launch
import timber.log.Timber

class FavoriteTeamViewModel(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private var selectedTeam: Team? = null

    private val _favoriteTeamUpdateEvent = MutableSingleLiveData<Unit>()
    val favoriteTeamUpdateEvent: SingleLiveData<Unit> get() = _favoriteTeamUpdateEvent

    fun saveFavoriteTeam() {
        viewModelScope.launch {
            selectedTeam?.let { team: Team ->
                memberRepository
                    .updateFavoriteTeam(team)
                    .onSuccess {
                        _favoriteTeamUpdateEvent.setValue(Unit)
                    }.onFailure { exception: Throwable ->
                        Timber.w(exception, "API 호출 실패")
                    }
            }
        }
    }

    fun selectTeam(team: Team) {
        selectedTeam = team
    }
}
