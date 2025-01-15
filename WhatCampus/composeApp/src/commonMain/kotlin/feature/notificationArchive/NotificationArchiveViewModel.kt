package feature.notificationArchive

import androidx.lifecycle.viewModelScope
import core.common.CommonViewModel
import core.domain.repository.NotificationRepository
import core.domain.repository.UserRepository
import core.model.Notification
import core.model.Response
import feature.notificationArchive.model.NotificationArchiveUiEvent
import feature.notificationArchive.model.NotificationArchiveUiState
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificationArchiveViewModel(
    private val userRepository: UserRepository,
    private val notificationRepository: NotificationRepository,
) : CommonViewModel() {
    private val _uiState = MutableStateFlow(NotificationArchiveUiState(isLoading = true))
    val uiState = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<NotificationArchiveUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    init {
        viewModelScope.launch {
            fetchNotifications()
        }
    }

    fun turnOffNewNotification() {
        viewModelScope.launch {
            notificationRepository.updateHasNewNotification(hasNewNotification = false)
        }
    }

    fun readNotification(notificationId: Long) {
        userRepository.flowUser()
            .onEach { user ->
                if (user == null) {
                    sendOtherErrorEvent()
                    _uiEvent.emit(NotificationArchiveUiEvent.REFRESH_COMPLETE)
                }
            }
            .filterNotNull()
            .onEach { user ->
                notificationRepository.readNotification(
                    userId = user.userId,
                    notificationId = notificationId,
                )
            }
            .onEach { fetchNotifications() }
            .launchIn(viewModelScope)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun fetchNotifications() {
        userRepository.flowUser()
            .onEach { user ->
                if (user == null) {
                    sendOtherErrorEvent()
                    _uiEvent.emit(NotificationArchiveUiEvent.REFRESH_COMPLETE)
                }
            }
            .filterNotNull()
            .flatMapLatest { user -> notificationRepository.flowNotifications(userId = user.userId) }
            .onEach(::handleResponse)
            .onEach { _uiEvent.emit(NotificationArchiveUiEvent.REFRESH_COMPLETE) }
            .launchIn(viewModelScope)
    }

    private suspend fun handleResponse(response: Response<PersistentList<Notification>>) {
        when (response) {
            is Response.Success -> _uiState.update { it.copy(isLoading = false, notifications = response.body) }
            Response.Failure.ClientError -> sendClientErrorEvent()
            Response.Failure.ServerError -> sendServerErrorEvent()
            Response.Failure.NetworkError -> sendNetworkErrorEvent()
            is Response.Failure.OtherError<*> -> sendOtherErrorEvent()
        }
    }
}
