package feature.notificationArchive

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import core.common.extensions.collectAsStateMultiplatform
import core.common.extensions.collectUiEvent
import core.common.util.logScreenEvent
import core.designsystem.components.LoadingScreen
import core.designsystem.components.WhatCamPullToRefreshContainer
import core.di.koinViewModel
import core.model.Notification
import feature.notificationArchive.components.NotificationArchiveList
import feature.notificationArchive.components.NotificationArchiveTopBar
import feature.notificationArchive.model.NotificationArchiveUiEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationArchiveScreen(
    modifier: Modifier = Modifier,
    viewModel: NotificationArchiveViewModel = koinViewModel(),
    onClickBack: () -> Unit,
    onClickNewNoticeNotification: (Notification.NewNotice) -> Unit,
) {
    logScreenEvent(screenName = "NotificationArchiveScreen")

    val uiState by viewModel.uiState.collectAsStateMultiplatform()
    viewModel.commonUiEvent.collectUiEvent()

    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        viewModel.fetchNotifications()
    }

    LaunchedEffect(Unit) {
        viewModel.turnOffNewNotification()
        viewModel.uiEvent.collectLatest { uiEvent ->
            when (uiEvent) {
                NotificationArchiveUiEvent.REFRESH_COMPLETE -> refreshState.endRefresh()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = { NotificationArchiveTopBar(onClickBack = onClickBack) }
    ) { paddingValues ->
        if (uiState.isLoading) {
            LoadingScreen(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        NotificationArchiveList(
            modifier = modifier
                .padding(paddingValues)
                .nestedScroll(refreshState.nestedScrollConnection),
            notifications = uiState.notifications,
            onClickNewNoticeNotification = { newNoticeNotification ->
                viewModel.readNotification(notificationId = newNoticeNotification.id)
                onClickNewNoticeNotification(newNoticeNotification)
            },
        )

        WhatCamPullToRefreshContainer(
            modifier = modifier.padding(paddingValues),
            refreshState = refreshState
        )
    }
}
