package com.yagubogu.presentation.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.domain.model.Team
import com.yagubogu.presentation.util.livedata.MutableSingleLiveData
import com.yagubogu.presentation.util.livedata.SingleLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoriteTeamViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
    kermitLogger: Logger,
) : ViewModel() {
    private val logger = kermitLogger.withTag("FavoriteTeamViewModel")
    private var selectedTeam: Team? = null

    private val _favoriteTeamUpdateEvent = MutableSingleLiveData<Unit>()
    val favoriteTeamUpdateEvent: SingleLiveData<Unit> get() = _favoriteTeamUpdateEvent

    fun saveFavoriteTeam() {
        viewModelScope.launch {
            selectedTeam?.let { team: Team ->
                memberRepository
                    .updateFavoriteTeam(team.name)
                    .onSuccess {
                        _favoriteTeamUpdateEvent.setValue(Unit)
                    }.onFailure { exception: Throwable ->
                        logger.w(exception) { "API 호출 실패" }
                    }
            }
        }
    }

    fun selectTeam(team: Team) {
        selectedTeam = team
    }
}
