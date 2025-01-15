package feature.notice

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import core.common.extensions.collectAsStateMultiplatform
import core.common.extensions.collectUiEvent
import core.common.util.logScreenEvent
import core.designsystem.components.WhatCamPullToRefreshContainer
import core.di.koinViewModel
import core.model.Notice
import feature.notice.components.NoticeCategoryBar
import feature.notice.components.NoticeList
import feature.notice.components.NoticeTopAppBar
import feature.notice.model.NoticeUiEvent
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoticeScreen(
    viewModel: NoticeViewModel = koinViewModel(),
    onNoticeClick: (Notice) -> Unit,
    onClickNoticeSearch: () -> Unit,
    onClickNotificationArchive: () -> Unit,
    onClickProfile: () -> Unit,
) {
    logScreenEvent(screenName = "NoticeScreen")

    val uiState by viewModel.uiState.collectAsStateMultiplatform()
    viewModel.commonUiEvent.collectUiEvent()

    val refreshState = rememberPullToRefreshState()
    if (refreshState.isRefreshing) {
        viewModel.fetchNotices()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collectLatest { uiEvent ->
            when (uiEvent) {
                NoticeUiEvent.REFRESH_COMPLETE -> refreshState.endRefresh()
            }
        }
    }

    Scaffold(
        topBar = {
            NoticeTopAppBar(
                hasNewNotification = uiState.hasNewNotification,
                onClickSearch = onClickNoticeSearch,
                onClickNotificationArchive = onClickNotificationArchive,
                onClickProfile = onClickProfile,
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .nestedScroll(refreshState.nestedScrollConnection)
        ) {
            val noticeListScrollState = rememberLazyListState()

            val isAtTop by remember {
                derivedStateOf {
                    noticeListScrollState.firstVisibleItemIndex < 2
                }
            }

            NoticeList(
                listState = noticeListScrollState,
                noticesWithBookmark = uiState.notices,
                onClickItem = onNoticeClick,
            )

            AnimatedVisibility(
                visible = isAtTop,
                enter = fadeIn() + slideInVertically { height -> -height },
                exit = fadeOut() + slideOutVertically { height -> -height }
            ) {
                NoticeCategoryBar(
                    noticeCategories = uiState.noticeCategories,
                    selectedCategory = uiState.selectedCategory,
                    onClickCategory = viewModel::selectCategory,
                )
            }

            WhatCamPullToRefreshContainer(refreshState = refreshState)
        }
    }
}
