package com.yagubogu.ui.favorite

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.domain.model.Team
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class FavoriteTeamViewModel @Inject constructor(
    private val memberRepository: MemberRepository,
) : ViewModel() {
    private val _favoriteTeamUpdateEvent = MutableSharedFlow<Unit>()
    val favoriteTeamUpdateEvent: SharedFlow<Unit> = _favoriteTeamUpdateEvent.asSharedFlow()

    fun saveFavoriteTeam(team: Team) {
        viewModelScope.launch {
            memberRepository
                .updateFavoriteTeam(team.name)
                .onSuccess {
                    _favoriteTeamUpdateEvent.emit(Unit)
                }.onFailure { exception: Throwable ->
                    Timber.w(exception, "API 호출 실패")
                }
        }
    }
}
