package feature.bookmark

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import core.domain.repository.NoticeRepository
import core.domain.usecase.GetAllBookmarkedNoticesUseCase
import core.model.Notice
import feature.bookmark.model.BookmarkUiEvent
import feature.bookmark.model.BookmarkUiState
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookmarkViewModel(
    private val getAllBookmarkedNotices: GetAllBookmarkedNoticesUseCase,
    private val noticeRepository: NoticeRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(BookmarkUiState())
    val uiState: StateFlow<BookmarkUiState> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<BookmarkUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        fetchBookmarkedNotices()
    }

    fun fetchBookmarkedNotices() {
        getAllBookmarkedNotices()
            .map { bookmarkedNotices -> _uiState.update { uiState -> uiState.copy(notices = bookmarkedNotices) } }
            .onEach { _uiEvent.emit(BookmarkUiEvent.REFRESH_COMPLETE) }
            .launchIn(viewModelScope)
    }

    fun markBookmarkForDelete(bookmarkedNotice: Notice) {
        if (bookmarkedNotice in uiState.value.markedNoticesForDelete) {
            _uiState.update { uiState ->
                uiState.copy(markedNoticesForDelete = uiState.markedNoticesForDelete - bookmarkedNotice)
            }
        } else {
            _uiState.update { uiState ->
                uiState.copy(markedNoticesForDelete = uiState.markedNoticesForDelete + bookmarkedNotice)
            }
        }
    }

    fun clearMarkedNoticesForDelete() {
        _uiState.update { uiState ->
            uiState.copy(markedNoticesForDelete = persistentSetOf())
        }
    }

    fun markAllNoticesForDelete() {
        _uiState.update { uiState ->
            uiState.copy(markedNoticesForDelete = uiState.notices.toPersistentSet())
        }
    }

    fun unbookmarkNotices() {
        viewModelScope.launch {
            val markedNoticesForDelete = uiState.value.markedNoticesForDelete.toList()
            noticeRepository.unbookmarkNotices(notices = markedNoticesForDelete)
        }
    }
}
