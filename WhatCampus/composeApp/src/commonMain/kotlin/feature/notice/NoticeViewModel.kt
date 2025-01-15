package feature.notice

import androidx.lifecycle.viewModelScope
import core.common.CommonViewModel
import core.domain.repository.NoticeRepository
import core.domain.repository.NotificationRepository
import core.domain.repository.UserRepository
import core.domain.usecase.GetAllBookmarkedNoticesUseCase
import core.domain.usecase.GetNoticesByCategoryIdUseCase
import core.model.NoticeCategory
import core.model.Response
import feature.notice.model.NoticeUiEvent
import feature.notice.model.NoticeUiState
import feature.notice.model.NoticeWithBookmark
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class NoticeViewModel(
    noticeRepository: NoticeRepository,
    notificationRepository: NotificationRepository,
    userRepository: UserRepository,
    private val getNoticesByCategoryId: GetNoticesByCategoryIdUseCase,
    private val getAllBookmarkedNotices: GetAllBookmarkedNoticesUseCase,
) : CommonViewModel() {
    private val _uiState: MutableStateFlow<NoticeUiState> = MutableStateFlow(NoticeUiState())
    val uiState: StateFlow<NoticeUiState> = _uiState.asStateFlow()

    private val _uiEvent: MutableSharedFlow<NoticeUiEvent> = MutableSharedFlow()
    val uiEvent: SharedFlow<NoticeUiEvent> = _uiEvent.asSharedFlow()

    init {
        userRepository.flowUser()
            .filterNotNull()
            .flatMapLatest { user ->
                noticeRepository.flowNoticeCategory(universityId = user.universityId)
                    .map { noticeCategoriesResponse ->
                        when (noticeCategoriesResponse) {
                            is Response.Success -> _uiState.update { state ->
                                val noticeCategories = noticeCategoriesResponse.body
                                state.copy(
                                    user = user,
                                    noticeCategories = noticeCategories,
                                    selectedCategory = noticeCategories.first()
                                )
                            }

                            is Response.Failure.ClientError -> sendClientErrorEvent()
                            is Response.Failure.ServerError -> sendServerErrorEvent()
                            is Response.Failure.NetworkError -> sendNetworkErrorEvent()
                            is Response.Failure.OtherError<*> -> sendOtherErrorEvent()
                        }
                    }
            }
            .launchIn(viewModelScope)

        notificationRepository.flowHasNewNotification()
            .onEach { hasNewNotification ->
                _uiState.update { uiState -> uiState.copy(hasNewNotification = hasNewNotification) }
            }
            .launchIn(viewModelScope)


    }

    fun fetchNotices() {
        uiState
            .flatMapLatest { state ->
                val universityId = state.user?.universityId ?: return@flatMapLatest emptyFlow()
                val categoryId = state.selectedCategory?.id ?: return@flatMapLatest emptyFlow()

                getNoticesByCategoryId(universityId, categoryId)
            }
            .combine(getAllBookmarkedNotices()) { notices, bookmarkedNotices ->
                val bookmarkedNoticeIds =
                    bookmarkedNotices.map { notice -> notice.id }.toImmutableSet()
                (notices to bookmarkedNoticeIds)
            }
            .map { (noticesResponse, bookmarkedNoticeIds) ->
                noticesResponse.map { notices ->
                    notices.map { notice ->
                        NoticeWithBookmark(
                            notice = notice,
                            isBookmarked = notice.id in bookmarkedNoticeIds
                        )
                    }
                }
            }
            .map { noticeWithBookmarkResponse ->
                when (noticeWithBookmarkResponse) {
                    is Response.Success -> _uiState.update { uiState ->
                        uiState.copy(notices = noticeWithBookmarkResponse.body)
                    }

                    is Response.Failure.ClientError -> sendClientErrorEvent()
                    is Response.Failure.ServerError -> sendServerErrorEvent()
                    is Response.Failure.NetworkError -> sendNetworkErrorEvent()
                    is Response.Failure.OtherError<*> -> sendOtherErrorEvent()
                }
                _uiEvent.emit(NoticeUiEvent.REFRESH_COMPLETE)
            }
            .launchIn(viewModelScope)
    }

    fun selectCategory(category: NoticeCategory) {
        val state = uiState.value
        if (category == state.selectedCategory) return

        _uiState.update {
            state.copy(selectedCategory = category)
        }
    }
}
