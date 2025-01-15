package feature.profile.subscreen.noticeCategory

import androidx.lifecycle.viewModelScope
import core.common.CommonViewModel
import core.domain.repository.NoticeRepository
import core.domain.repository.UserRepository
import core.domain.usecase.SubscribeNoticeCategoriesUseCase
import core.model.NoticeCategory
import core.model.Response
import core.model.User
import feature.profile.subscreen.noticeCategory.model.NoticeCategoryUiEvent
import feature.profile.subscreen.noticeCategory.model.NoticeCategoryUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NoticeCategoryViewModel(
    private val noticeRepository: NoticeRepository,
    private val userRepository: UserRepository,
    private val subscribeNoticeCategories: SubscribeNoticeCategoriesUseCase,
) : CommonViewModel() {
    private val _uiState = MutableStateFlow(NoticeCategoryUiState())
    val uiState: StateFlow<NoticeCategoryUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<NoticeCategoryUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        fetchNoticeCategories()
    }

    fun fetchNoticeCategories() {
        userRepository.flowUser()
            .filterNotNull()
            .flatMapLatest { user ->
                combine(
                    noticeRepository.flowSubscribedNoticeCategories(userId = user.userId),
                    noticeRepository.flowNoticeCategory(universityId = user.universityId)
                ) { subscribedNoticeCategoriesResponse, noticeCategoriesResponse ->
                    handleNoticeCategoriesResponse(subscribedNoticeCategoriesResponse, noticeCategoriesResponse, user)
                }
            }
            .onEach { _uiEvent.emit(NoticeCategoryUiEvent.REFRESH_COMPLETE) }
            .launchIn(viewModelScope)
    }

    private suspend fun NoticeCategoryViewModel.handleNoticeCategoriesResponse(
        subscribedNoticeCategoriesResponse: Response<Set<NoticeCategory>>,
        noticeCategoriesResponse: Response<List<NoticeCategory>>,
        user: User,
    ) {
        when {
            subscribedNoticeCategoriesResponse is Response.Success && noticeCategoriesResponse is Response.Success -> _uiState.update { state ->
                state.copy(
                    subscribedNoticeCategories = subscribedNoticeCategoriesResponse.body,
                    noticeCategories = noticeCategoriesResponse.body,
                    user = user,
                )
            }

            subscribedNoticeCategoriesResponse is Response.Failure.ClientError || noticeCategoriesResponse is Response.Failure.ClientError -> sendClientErrorEvent()
            subscribedNoticeCategoriesResponse is Response.Failure.ServerError || noticeCategoriesResponse is Response.Failure.ServerError -> sendServerErrorEvent()
            subscribedNoticeCategoriesResponse is Response.Failure.NetworkError || noticeCategoriesResponse is Response.Failure.NetworkError -> sendNetworkErrorEvent()
            subscribedNoticeCategoriesResponse is Response.Failure.OtherError<*> || noticeCategoriesResponse is Response.Failure.OtherError<*> -> sendOtherErrorEvent()
        }
    }

    fun toggleNoticeCategory(noticeCategory: NoticeCategory) {
        _uiState.update { uiState ->
            uiState.toggleSelectedNoticeCategory(noticeCategory)
        }
    }

    fun subscribeNoticeCategories() {
        val uiState = uiState.value
        val user = uiState.user ?: return

        viewModelScope.launch {
            subscribeNoticeCategories(
                userId = user.userId,
                allNoticeCategoryIds = uiState.noticeCategories.map(NoticeCategory::id),
                subscribedNoticeCategoryIds = uiState.subscribedNoticeCategories.map(NoticeCategory::id),
            )
        }
    }
}
