package feature.profile.subscreen.noticeCategory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.repository.NoticeRepository
import core.domain.repository.UserRepository
import core.domain.usecase.SubscribeNoticeCategoriesUseCase
import core.model.NoticeCategory
import feature.profile.subscreen.noticeCategory.model.NoticeCategoryUiState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class NoticeCategoryViewModel(
    noticeRepository: NoticeRepository,
    userRepository: UserRepository,
    private val subscribeNoticeCategories: SubscribeNoticeCategoriesUseCase,
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoticeCategoryUiState())
    val uiState: StateFlow<NoticeCategoryUiState> = _uiState.asStateFlow()

    init {
        userRepository.flowUser()
            .filterNotNull()
            .flatMapLatest { user ->
                combine(
                    noticeRepository.flowSubscribedNoticeCategories(userId = user.userId),
                    noticeRepository.flowNoticeCategory(universityId = user.universityId)
                ) { subscribedNoticeCategories, noticeCategories ->
                    _uiState.update { state ->
                        state.copy(
                            subscribedNoticeCategories = subscribedNoticeCategories,
                            noticeCategories = noticeCategories,
                            user = user,
                        )
                    }
                }
            }
            .launchIn(viewModelScope)

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
