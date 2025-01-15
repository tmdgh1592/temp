@file:OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)

package feature.university

import androidx.lifecycle.viewModelScope
import core.common.CommonViewModel
import core.domain.repository.NoticeRepository
import core.domain.repository.UniversityRepository
import core.domain.repository.UserRepository
import core.model.Department
import core.model.NoticeCategory
import core.model.Response
import core.model.University
import feature.university.model.UniversityUiEvent
import feature.university.model.UniversityUiState
import kotlinx.collections.immutable.toPersistentList
import kotlinx.collections.immutable.toPersistentSet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class UniversityViewModel(
    private val universityRepository: UniversityRepository,
    private val noticeRepository: NoticeRepository,
    private val userRepository: UserRepository,
) : CommonViewModel() {
    private val _uiEvent = MutableSharedFlow<UniversityUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    private val _uiState = MutableStateFlow(UniversityUiState())
    val uiState = _uiState.asStateFlow()

    private val _universitySearchQuery: MutableStateFlow<String> = MutableStateFlow("")
    val universitySearchQuery = _universitySearchQuery.asStateFlow()

    init {
        universitySearchQuery
            .debounce(SEARCH_DEBOUNCE)
            .map { query -> query.trim() }
            .flatMapLatest { query -> flowUniversities(query) }
            .launchIn(viewModelScope)
    }

    private fun flowUniversities(query: String): Flow<Response<List<University>>> = universityRepository
        .flowUniversity(query = query)
        .onEach { universitiesResponse ->
            when (universitiesResponse) {
                is Response.Success -> _uiState.update { state ->
                    state.copy(
                        universities = universitiesResponse.body.toPersistentList(),
                        selectedUniversity = state.selectedUniversity,
                    )
                }

                Response.Failure.NetworkError -> sendNetworkErrorEvent()
                else -> _uiEvent.emit(UniversityUiEvent.UNIVERSITY_LOAD_FAILED)
            }
        }

    private fun fetchNoticeCategories(universityId: Long) {
        noticeRepository.flowNoticeCategory(universityId = universityId)
            .onEach { noticeCategoriesResponse ->
                when (noticeCategoriesResponse) {
                    is Response.Success -> _uiState.update { state ->
                        state.copy(
                            noticeCategories = noticeCategoriesResponse.body,
                            selectedNoticeCategories = noticeCategoriesResponse.body.toPersistentSet(),
                        )
                    }

                    else -> _uiEvent.emit(UniversityUiEvent.UNIVERSITY_LOAD_FAILED)
                }
            }
            .launchIn(viewModelScope)
    }

    fun selectUniversity(university: University) {
        _uiState.update { state ->
            state.copy(selectedUniversity = university)
        }
        fetchNoticeCategories(universityId = university.id)
    }

    fun searchUniversity(query: String) {
        _universitySearchQuery.value = query
    }

    fun selectDepartment(department: Department) {
        _uiState.update { state ->
            state.copy(selectedDepartment = department)
        }
    }

    fun searchDepartment(query: String) {
        _uiState.update { state ->
            state.copy(departmentSearchQuery = query)
        }
    }

    fun toggleNoticeCategory(noticeCategory: NoticeCategory) {
        _uiState.update { state ->
            state.toggleSelectedNoticeCategory(noticeCategory)
        }
    }

    fun saveUser() {
        viewModelScope.launch {
            val selectedUniversity = uiState.value.selectedUniversity
            val selectedDepartment = uiState.value.selectedDepartment

            if (selectedUniversity == null || selectedDepartment == null) {
                sendOtherErrorEvent()
                return@launch
            }

            val userCreateResponse = userRepository.createUser(
                universityId = selectedUniversity.id,
                universityName = selectedUniversity.name,
                departmentId = selectedDepartment.id,
                departmentName = selectedDepartment.name,
                noticeCategoryIds = uiState.value.selectedNoticeCategories.map(NoticeCategory::id)
            )

            when (userCreateResponse) {
                is Response.Success -> _uiEvent.emit(UniversityUiEvent.USER_SAVE_SUCCESS)
                Response.Failure.ClientError -> sendClientErrorEvent()
                Response.Failure.ServerError -> sendServerErrorEvent()
                Response.Failure.NetworkError -> sendNetworkErrorEvent()
                is Response.Failure.OtherError<*> -> sendOtherErrorEvent()
            }
        }
    }

    companion object {
        private const val SEARCH_DEBOUNCE = 300L
    }
}
