package com.yagubogu.ui.place

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.touchlab.kermit.Logger
import com.yagubogu.data.dto.response.game.GameWithCheckInDto
import com.yagubogu.data.dto.response.place.PlaceDetailResponse
import com.yagubogu.data.dto.response.place.PlacesResponse
import com.yagubogu.data.repository.game.GameRepository
import com.yagubogu.data.repository.member.MemberRepository
import com.yagubogu.data.repository.place.PlaceRepository
import com.yagubogu.domain.model.Team
import com.yagubogu.domain.model.homeStadiumName
import com.yagubogu.ui.mapper.toApiCategory
import com.yagubogu.ui.mapper.toUiModel
import com.yagubogu.ui.place.model.PlaceCategory
import com.yagubogu.ui.place.model.PlaceDetailUiModel
import com.yagubogu.ui.place.model.PlaceDetailUiState
import com.yagubogu.ui.place.model.PlaceListUiState
import com.yagubogu.ui.place.model.PlaceStadiumItem
import com.yagubogu.ui.util.now
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlin.time.Clock

class PlaceViewModel(
    private val placeRepository: PlaceRepository,
    private val gameRepository: GameRepository,
    private val memberRepository: MemberRepository,
    private val clock: Clock,
) : ViewModel() {
    private val logger = Logger.withTag("PlaceViewModel")

    private val _stadiums = MutableStateFlow<List<PlaceStadiumItem>>(emptyList())
    val stadiums: StateFlow<List<PlaceStadiumItem>> = _stadiums.asStateFlow()

    private val _selectedStadiumId = MutableStateFlow<Long?>(null)
    val selectedStadiumId: StateFlow<Long?> = _selectedStadiumId.asStateFlow()

    private val _selectedCategory = MutableStateFlow(PlaceCategory.STAY)
    val selectedCategory: StateFlow<PlaceCategory> = _selectedCategory.asStateFlow()

    private val _places = MutableStateFlow<PlaceListUiState>(PlaceListUiState.Loading)
    val places: StateFlow<PlaceListUiState> = _places.asStateFlow()

    private val _placeDetail = MutableStateFlow<PlaceDetailUiState>(PlaceDetailUiState.Loading)
    val placeDetail: StateFlow<PlaceDetailUiState> = _placeDetail.asStateFlow()

    private var placesJob: Job? = null
    private var placeDetailJob: Job? = null

    fun loadStadiums() {
        viewModelScope.launch {
            val gamesResult: Result<List<GameWithCheckInDto>> = gameRepository.getGames(LocalDate.now(clock))
            gamesResult
                .onSuccess { games: List<GameWithCheckInDto> ->
                    val stadiumItems: List<PlaceStadiumItem> =
                        games
                            .map { game: GameWithCheckInDto -> PlaceStadiumItem(game.stadium.id, game.stadium.name) }
                            .distinctBy { it.id }
                    _stadiums.value = stadiumItems

                    val defaultStadiumId: Long? = resolveDefaultStadiumId(stadiumItems)
                    if (defaultStadiumId != null) {
                        selectStadium(defaultStadiumId)
                    } else {
                        _places.value = PlaceListUiState.Empty
                    }
                }.onFailure { exception: Throwable ->
                    logger.w(exception) { "오늘 경기 구장 조회 실패" }
                    _places.value = PlaceListUiState.Error(exception.message ?: "")
                }
        }
    }

    private suspend fun resolveDefaultStadiumId(stadiumItems: List<PlaceStadiumItem>): Long? {
        if (stadiumItems.isEmpty()) return null

        val favoriteTeamCode: String? = memberRepository.getFavoriteTeam().getOrNull()
        val favoriteHomeStadiumName: String? = favoriteTeamCode?.let { code: String -> Team.getByCode(code).homeStadiumName }

        val matched: PlaceStadiumItem? = stadiumItems.firstOrNull { it.name == favoriteHomeStadiumName }
        return (matched ?: stadiumItems.first()).id
    }

    fun selectStadium(stadiumId: Long) {
        _selectedStadiumId.value = stadiumId
        fetchPlaces(stadiumId, _selectedCategory.value)
    }

    fun selectCategory(category: PlaceCategory) {
        _selectedCategory.value = category
        val stadiumId: Long = _selectedStadiumId.value ?: return
        fetchPlaces(stadiumId, category)
    }

    private fun fetchPlaces(
        stadiumId: Long,
        category: PlaceCategory,
    ) {
        placesJob?.cancel()
        placesJob =
            viewModelScope.launch {
                _places.value = PlaceListUiState.Loading
                val result: Result<PlacesResponse> = placeRepository.getPlaces(stadiumId, category.toApiCategory())
                result
                    .onSuccess { response: PlacesResponse ->
                        _places.value =
                            if (response.places.isEmpty()) {
                                PlaceListUiState.Empty
                            } else {
                                PlaceListUiState.Success(response.places.map { it.toUiModel() })
                            }
                    }.onFailure { exception: Throwable ->
                        logger.w(exception) { "플레이스 목록 조회 실패" }
                        _places.value = PlaceListUiState.Error(exception.message ?: "")
                    }
            }
    }

    fun loadPlaceDetail(
        id: Long,
        distanceMeters: Int?,
    ) {
        placeDetailJob?.cancel()
        placeDetailJob =
            viewModelScope.launch {
                _placeDetail.value = PlaceDetailUiState.Loading
                val result: Result<PlaceDetailResponse> = placeRepository.getPlaceDetail(id)
                result
                    .onSuccess { response: PlaceDetailResponse ->
                        runCatching { response.toUiModel(distanceMeters) }
                            .fold(
                                onSuccess = { uiModel: PlaceDetailUiModel ->
                                    _placeDetail.value = PlaceDetailUiState.Success(uiModel)
                                },
                                onFailure = { exception: Throwable ->
                                    logger.w(exception) { "플레이스 상세 매핑 실패" }
                                    _placeDetail.value = PlaceDetailUiState.Error(exception.message ?: "")
                                },
                            )
                    }.onFailure { exception: Throwable ->
                        logger.w(exception) { "플레이스 상세 조회 실패" }
                        _placeDetail.value =
                            if (exception.message?.contains("404") == true) {
                                PlaceDetailUiState.NotFound
                            } else {
                                PlaceDetailUiState.Error(exception.message ?: "")
                            }
                    }
            }
    }
}
